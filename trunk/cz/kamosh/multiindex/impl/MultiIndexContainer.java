/*
 *  Main authors:
 *     Fekete Kamosh <fekete.kamosh@gmail.com> 
 * 
 *  Copyright:
 *     LOGIS a.s., 2008 - 2010 
 *     
 *  Last modified:
 *     $Date$ by $Author$
 *     $Revision$
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package cz.kamosh.multiindex.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import cz.kamosh.multiindex.criterion.Expression;
import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.impl.Junction.Conjunction;
import cz.kamosh.multiindex.impl.Junction.Disjunction;
import cz.kamosh.multiindex.interf.IMultiIndexContainer;
import cz.kamosh.multiindex.interf.IMultiIndexed;
import cz.kamosh.multiindex.utils.NullKeyMap;

/**
 * Abstract class as support for all implementations of MultiIndexContainer.
 * Multiindex serves as collection with ability for fast seeking data in it.
 * <p/>
 * Each inserted record must implement {@link IMultiIndexed} which must return
 * unique identifier of record!.
 * <p/>
 * Indexed attributes do not have to be unique.
 * 
 * @param <E> Type of record which will should be multiindexed
 * @param <K> Type of primary key
 * @param <L> Type of index
 * @param <R> Type of lookup rule
 * @param <C> Type of criterion (more general than expression)
 */
public abstract class MultiIndexContainer<E extends IMultiIndexed<K>, K extends Object, L>
		implements IMultiIndexContainer<E, K, L> {

	final static private Logger logger = Logger
			.getLogger(MultiIndexContainer.class.getName());

	private Map<L, DataGetter> cachedDataGetters;;

	protected final Collection<E> EMPTY_RESULT = Collections.<E> emptySet();

	/**
	 * Object used as locking object used for synchronization. Each new read
	 * lock increments lock for 1. Each end of usage of read lock decrease lock
	 * for 1. Write lock is expressed by value -1 and could be set only if there
	 * is not any perting
	 */
	private static class Lock {
		int counter = 0;
		/**
		 * Boolean to express that write lock is just being required
		 */
		boolean writeLockRequired = false;
	}

	/**
	 * Lock used for synchronization
	 */
	protected Lock lock;

	/**
	 * Plain data of all records, each record has its unique <code>key</code>
	 * value. It should be instantiated in inherited class
	 */
	protected Map<K, E> data;

	/**
	 * Could be indexed column values changed
	 */
	protected boolean indexedAttributeCanChange;

	/**
	 * Map of all indexes. Each index has its UNIQUE name that also identified
	 * attribute name of record.
	 * <P>
	 * NOTE: There is used NavigableMap interface because it has methods
	 * <UL>
	 * <LI>{@link java.util.NavigableMap#tailMap(Object, boolean)}
	 * <LI>{@link java.util.NavigableMap#headMap(Object, boolean)}
	 * <LI>
	 * {@link java.util.NavigableMap#subMap(Object, boolean, Object, boolean)}
	 * </UL>
	 * that allows usage of <code>inclusive</code> parameter
	 * 
	 * @see java.util.NavigableMap
	 */
	protected final Map<L, NavigableMap<Object, Collection<E>>> indexes;

	protected MultiIndexContainer() {
		this.indexes = new HashMap<L, NavigableMap<Object, Collection<E>>>();
		this.lock = new Lock();
		this.cachedDataGetters = new HashMap<L, DataGetter>();
		this.data = new HashMap<K, E>();
	}

	// ============================ Synchronizing methods
	// ==============================/
	protected void acquireReadLock() {
		synchronized (lock) {
			do {
				if (lock.counter == -1 || lock.writeLockRequired) {
					// Write lock is just being held
					// or write lock is waiting to get
					// Lets wait for lock's release
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					// Lets acquire read lock -> increase number of readers
					lock.counter++;
					break;
				}
			} while (true);
		}
	}

	protected void releaseReadLock() {
		synchronized (lock) {
			// Release read lock -> decrease number of readers
			lock.counter--;
			lock.notifyAll(); // Tell everybody that lock has been released and
								// might be acquired by waiters
		}
	}

	protected void acquireWriteLock() {
		synchronized (lock) {
			do {
				if (lock.counter != 0) {
					// Read (or write) lock is just being held
					// Mark that write lock is waiting
					// Lets wait for lock's release
					lock.writeLockRequired = true;
					try {
						lock.wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				} else {
					// Write lock is not waiting now
					lock.writeLockRequired = false;
					// Lets acquire write lock -> changes its value to -1
					lock.counter = -1;
					break;
				}
			} while (true);
		}
	}

	protected void releaseWriteLock() {
		synchronized (lock) {
			// Release read lock -> set value to 0
			lock.counter = 0;
			lock.notifyAll(); // Tell everybody that lock has been released and
								// might be acquired by waiters
		}
	}

	// =============================== Writer
	// methods==================================== //

	protected void recalculateIndexes() {
		for (Map.Entry<L, NavigableMap<Object, Collection<E>>> index : indexes
				.entrySet()) {
			recalculateIndex(index.getKey(), index.getValue());
		}
	}

	public void addIndex(L... index) {
		// Do nothing if none index specified
		if (index == null || index.length == 0) {
			return;
		}

		acquireWriteLock();
		try {
			for (L ind : index) {
				NavigableMap<Object, Collection<E>> indexedData;
				// If index already exists, do not create it again
				if ((indexedData = indexes.get(ind)) == null) {
					// Check whether it is possible to get data getter for index
					// If not, exception is thrown
					DataGetter dataGetter = getCachedDataGetter(ind);

					// Create new indexed values map
					indexedData = new NullKeyMap<Object, Collection<E>>(
							new TreeMap<Object, Collection<E>>(
									NullKeyMap.nullLowOrder()));
					indexes.put(ind, indexedData);
					// If there is already any data, lets index them
					recalculateIndex(dataGetter, indexedData);
				}
			}
		} finally {
			releaseWriteLock();
		}
	}

	public void clear() {
		acquireWriteLock();

		try {
			data.clear();
			indexes.clear();
		} finally {
			releaseWriteLock();
		}
	}

	@Override
	public void removeIndex(L index) {
		acquireWriteLock();

		try {
			// If index already exists, do not create it again
			indexes.remove(index);
		} finally {
			releaseWriteLock();
		}
	}

	@Override
	public void removeIndexes() {
		acquireWriteLock();

		try {
			indexes.clear();
		} finally {
			releaseWriteLock();
		}
	}

	@Override
	public void addAll(Collection<E> c) {
		if (c == null) {
			return;
		}
		acquireWriteLock();
		try {
			// Add all data
			for (E oldInstance : c) {
				data.put((K) oldInstance.getMultiIndexPk(), oldInstance);
			}
			recalculateIndexes();
		} finally {
			releaseWriteLock();
		}
	}

	@Override
	public void removeAll() {
		acquireWriteLock();

		try {
			// Remove all data
			data.clear();
			recalculateIndexes();
		} finally {
			releaseWriteLock();
		}
	}

	@Override
	public void removeAll(Collection<E> c) {
		if (c == null || c.isEmpty()) {
			return;
		}
		acquireWriteLock();

		try {
			// Remove all param data
			for (E record : c) {
				data.remove(record.getMultiIndexPk());
			}
			recalculateIndexes();
		} finally {
			releaseWriteLock();
		}
	}

	public void add(E obj) {
		if (obj == null) {
			return;
		}
		acquireWriteLock();

		try {
			// If indexed attribute can be changed, we have to get its old
			// instance
			E oldInstance = null;
			if (indexedAttributeCanChange) {
				oldInstance = data.get(obj.getMultiIndexPk());
			}

			data.put((K) obj.getMultiIndexPk(), obj);
			Iterator<Map.Entry<L, NavigableMap<Object, Collection<E>>>> i = indexes
					.entrySet().iterator();

			// Loop over all indexes
			while (i.hasNext()) {
				Map.Entry<L, NavigableMap<Object, Collection<E>>> entry = i
						.next();
				L index = entry.getKey();

				// Get new value of indexed attribute
				Object newAttribValue = getCachedDataGetter(index).getData(obj);

				// If indexed attribute can be changed and we try to add object
				// with the same
				// primary key twice, lets remove it first from indexes
				if (oldInstance != null) {
					// Get old value of indexed attribute
					Object oldAttribValue = getCachedDataGetter(index).getData(
							oldInstance);
					deleteIndexedRecord(entry.getValue(), oldAttribValue,
							(E) obj);
				}

				// Get indexed values
				NavigableMap<Object, Collection<E>> mm = entry.getValue();
				// Insert found indexed column value -> Primary key of record
				Collection<E> records = mm.get(newAttribValue);
				if (records == null) {
					records = new HashSet<E>();
					mm.put(newAttribValue, records);
				}
				records.add((E) obj);
			}
		} finally {
			releaseWriteLock();
		}
	}

	public void remove(E obj) {
		if (obj == null) {
			return;
		}
		acquireWriteLock();
		try {

			E actualObj;
			// Get and also remove old (already actual) value of contained
			// object
			// NOTE: PK is unchangeable so it is the same all the time even if
			// indexed attributes change
			actualObj = data.remove(obj.getMultiIndexPk());
			if (actualObj == null) {
				// Object does not exist in data, nothing should be done
				return;
			}

			// Remove object from all indexes
			Iterator<Map.Entry<L, NavigableMap<Object, Collection<E>>>> i = indexes
					.entrySet().iterator();
			// Loop over all indexes
			while (i.hasNext()) {
				Map.Entry<L, NavigableMap<Object, Collection<E>>> entry = i
						.next();
				L index = entry.getKey();

				// Get actual (already indexed) value of indexed attribute
				Object actualAttribValue = getCachedDataGetter(index).getData(
						actualObj);
				deleteIndexedRecord(entry.getValue(), actualAttribValue,
						(E) obj);
			}
		} finally {
			releaseWriteLock();
		}
	}

	private void deleteIndexedRecord(
			NavigableMap<Object, Collection<E>> indexedValues,
			Object indexedValue, E recordInstance) {
		// Remove recordInstances for specified indexed value
		Collection<E> recordInstances = indexedValues.get(indexedValue);
		recordInstances.remove(recordInstance);
		if (recordInstances.isEmpty()) {
			// If no record has indexed value, remove also indexed value from
			// indexes
			indexedValues.remove(indexedValue);
		}
	}

	// =============================== Reader methods
	// =========================================/

	@Override
	public Collection<E> getAll() {
		acquireReadLock();

		try {
			return new ArrayList<E>(data.values());
		} finally {
			releaseReadLock();
		}
	}

	@Override
	public E get(K pk) {
		acquireReadLock();

		try {
			return data.get(pk);
		} finally {
			releaseReadLock();
		}
	}

	public Set<L> getIndexes() {
		acquireReadLock();

		try {
			return new HashSet<L>(indexes.keySet());
		} finally {
			releaseReadLock();
		}
	}

	public Collection<Object> getDistinctIndexedValues(L index) {
		acquireReadLock();

		try {
			NavigableMap<Object, Collection<E>> indexedData = indexes
					.get(index);
			if (indexedData == null) {
				throw new UnsupportedOperationException("Index " + index
						+ " not established");
			}
			return new ArrayList<Object>(indexedData.keySet());
		} finally {
			releaseReadLock();
		}
	}

	public Collection<E> get(Collection<K> pks) {
		if (pks == null || pks.isEmpty()) {
			return EMPTY_RESULT;
		}
		acquireReadLock();

		try {
			Collection<E> res = new ArrayList<E>(pks.size());
			for (K pk : pks) {
				res.add(data.get(pk));
			}
			return res;
		} finally {
			releaseReadLock();
		}
	}

	@Override
	public boolean isEmpty() {
		acquireReadLock();

		try {
			return data.size() == 0;
		} finally {
			releaseReadLock();
		}
	}

	@Override
	public int size() {
		acquireReadLock();

		try {
			return data.size();
		} finally {
			releaseReadLock();
		}
	}

	public java.util.Collection<E> find(ICriterion<E, K, L> criterion) {
		if (criterion == null) {
			return EMPTY_RESULT;
		}
		acquireReadLock();
		try {
			return criterion.getRecordInstances(this);
		} finally {
			releaseReadLock();
		}
	};

	public Collection<E> getRecordInstances(Expression<E, K, L> lookupRule) {
		if (lookupRule == null) {
			return EMPTY_RESULT;
		}
		// Check that index specified in lookup rule exists
		NavigableMap<Object, Collection<E>> index = indexes.get(lookupRule
				.getIndex());
		if (index == null) {
			throw new UnsupportedOperationException("Index with name "
					+ lookupRule.getIndex() + " not established");
		}

		// Note: There is used HashSet List implementation due to frequent usage
		// of methods retainAll on result set.
		Collection<E> recordInstances = new HashSet<E>();

		// Attempt to solve ClassCastException if bad indexedValueFrom or
		// indexedValueTo has been passsed
		try {
			switch (lookupRule.getOperator()) {
			// NOTE: For all callings of method
			// AbstractCollection.retainAll(Collection<?> c)
			// are parameter values wrapped into HashSet, because there is
			// called method
			// contains(...) inside retainAll
			// For HashSet passed as parameter is contains(...) much faster than
			// lists

			case EQUAL:
			case IN:
			case IS_NULL:
				// There might be more possible equal values
				for (Object x : lookupRule.getValues()) {
					recordInstances.addAll(firstNotNull(index.get(x),
							EMPTY_RESULT));
				}
				break;
			case BETWEEN:
				for (Collection<E> records : index.subMap(
						lookupRule.getValueFrom(), true,
						lookupRule.getValueTo(), true).values()) {
					recordInstances.addAll(records);
				}
				break;
			case GREATER:
				for (Collection<E> records : index.tailMap(
						lookupRule.getValueFrom(), true).values()) {
					recordInstances.addAll(records);
				}
				break;
			case LESS:
				for (Collection<E> records : index.headMap(
						lookupRule.getValueTo(), true).values()) {
					recordInstances.addAll(records);
				}
				break;
			case IS_NOT_NULL:
				// Because we know that null values are first in index, they
				// should be bypassed
				// Find first not null key in indexes and put the rest as result
				for (Object key : index.keySet()) {
					if (key != null) {
						for (Collection<E> records : index.tailMap(key)
								.values()) {
							recordInstances.addAll(records);
						}
						break;
					}
				}
				break;
			default:
				throw new UnsupportedOperationException("Operator '"
						+ lookupRule.getOperator() + "' not implemented");
			}

		} catch (ClassCastException cce) {
			logger.severe("Index " + lookupRule.getIndex()
					+ " does not support specified values ["
					+ lookupRule.getValueFrom() + ", "
					+ lookupRule.getValueTo() + "]");
			return new ArrayList<E>();
		}
		return recordInstances;
	}

	/**
	 * Class to serve as getter for particular attribute from record. Instance
	 * of class is returned from method {@link #getDataGetter(Object)} which
	 * joins getter with index instance
	 */
	abstract class DataGetter {
		abstract Object getData(E record) throws UnsupportedOperationException;
	}

	/**
	 * Class serving as getter for data for specified index
	 * 
	 * @param index
	 *            Data indexed by this index should be get
	 * @return Getter for data for specific index
	 */
	abstract DataGetter getDataGetter(L index);

	// ------ Implementation of IMultiIndexContainer ---------- /
	public final Conjunction<E, K, L> conjunction() {
		return new Junction.Conjunction<E, K, L>();
	}

	public final Disjunction<E, K, L> disjunction() {
		return new Junction.Disjunction<E, K, L>();
	}

	// ----------- Helper methods -----------------/

	private DataGetter getCachedDataGetter(L index) {
		DataGetter dataGetter = cachedDataGetters.get(index);
		if (dataGetter == null) {
			dataGetter = getDataGetter(index);
			cachedDataGetters.put(index, dataGetter);
		}
		return dataGetter;
	}

	private void recalculateIndex(final DataGetter dataGetter,
			NavigableMap<Object, Collection<E>> indexedData) {
		// Remove multimap for index, indexed data values could be outdated
		indexedData.clear();

		// Loop over all values in data and index all their values on specified
		// attribute
		try {
			// int i = 0;
			for (Map.Entry<K, E> entry : data.entrySet()) {
				// Get indexed field value
				Object indexedValue = dataGetter.getData(entry.getValue());
				// Get records with this value or create new set of such records
				Collection<E> recordInstances = indexedData.get(indexedValue);
				if (recordInstances == null) {
					recordInstances = new HashSet<E>();
					indexedData.put(indexedValue, recordInstances);
				}
				// Add record to be indexed by particular value
				recordInstances.add((E) entry.getValue());
				// logger.info(""+i++);
			}
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}

	private void recalculateIndex(final L index,
			NavigableMap<Object, Collection<E>> indexedData) {
		// Check whether it is possible to get data getter for index
		// If not, exception is thrown
		DataGetter dataGetter = getCachedDataGetter(index);
		recalculateIndex(dataGetter, indexedData);
	}

	// ---------- Helper methods -----------------/
	/**
	 * If first argument is null then, second is returned as result value,
	 * otherwise first is returned
	 * <p/>
	 * 
	 * @param canBeNull
	 *            Object that can be null,
	 * @param defaultValue
	 *            Object that is returned if first argument is null
	 * @return Returns non null first argument, otherwise returns defaultValue
	 */
	protected static <T extends Object> T firstNotNull(T canBeNull,
			T defaultValue) {
		return canBeNull == null ? defaultValue : canBeNull;
	}

}
