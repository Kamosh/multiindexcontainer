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

package cz.kamosh.multiindex.interf;

import java.util.Collection;
import java.util.Set;

import cz.kamosh.multiindex.criterion.Expression;
import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.impl.Junction.Conjunction;
import cz.kamosh.multiindex.impl.Junction.Disjunction;

/**
 * Basic definition of behaviour of any MultiIndex container
 * @param <E>Type of record
 * @param <K>Type of primary key of record
 * @param <L>Type of index used for container. Now there is possibility to use <code>String</code> or <code>Indexable</code>
 */
public interface IMultiIndexContainer<E extends IMultiIndexed<K>, K extends Object, L> {

		
	/**
	 * Method to add/update record into container
	 * If null passed, nothing happens
	 * @param obj Record instance
	 */
	public void add(E obj);

	/**
	 * Method to remove instance of already indexed object from container
	 * If null passed, nothing happens
	 * @param obj Instance of (maybe) contained object
	 */
	public void remove(E obj);

	/**
	 * Remove all indexes
	 */
	public void removeIndexes();
	
	/**
	 * Adds index into container. Data are also automatically reindexed. 
	 * If index already exists, data are reindexed from the scratch
	 * @param index Identifier of index
	 */
	public void addIndex(L... index);

	/**
	 * Removes specified index from container if it exists there. 
	 * @param index Index identifier
	 */
	public void removeIndex(L index);

	/**
	 * Adds all records to container. All already established indexes are also recalculated to take into consideration new data.
	 * If null passed, nothing happens
	 * @param c What data to add?
	 */
	public void addAll(Collection<E> c);

	/**
	 * Removes all data but not indexes
	 */
	public void removeAll();

	/**
	 * Removes all specified records from container. All already established indexes are also recalculated to take into consideration removed data.
	 * If null passed, nothing happens
	 * @param c What data to remove?
	 */
	public void removeAll(Collection<E> c);

	/**
	 * Method to clear data and also indexes
	 */
	public void clear();

	/**
	 * Return all records in container.
	 * NOTE: Defensive copy of all contained objects is returned
	 * @return All contained data. Empty collection if case of none data.
	 */
	public Collection<E> getAll();

	/**
	 * Method to return records for specified primary keys
	 * 
	 * @param pks Primary keys of all resulting records
	 * @return Found records. If null passed or pks is empty, empty collection is returned
	 */
	public Collection<E> get(Collection<K> pks);

	public boolean isEmpty();

	public int size();	
	
	/**
	 * @return Gets identifiers of all indexes of container 
	 */
	public Set<L> getIndexes();	

	/**
	 * Get collection of all records that comply with specified criterion. 
	 * 	 
	 * @param criterion What criterion to fulfill?
	 * @return Collection of records fulfilling criterion. Empty collection if null passed
	 */
	public Collection<E> find(ICriterion<E, K, L> criterion);
	
	
	/**
	 * Method to return collection of all indexed values for
	 * specified index.
	 * <P>NOTE: Indexed values are returned as defensive copy
	 *
	 * @param indexName What index are you interested in?
	 * @return Attribute values indexed by specified index
	 */
	public Collection<Object> getDistinctIndexedValues(L index);
	
	/**
	 * Method to return collection of all indexed values for specified expression
	 * @param expression What expression to use to find data
	 * @return Records that suits specified expression. Empty collection if null passed
	 */
	public Collection<E> getRecordInstances(Expression<E, K, L> expression);
	
	
	/**
	 * Method to get record with specified primary key
	 * 
	 * @param pk Identifier of primary key
	 * @return Found record if primary key points to some record, null otherwise
	 */
	public E get(K pk);
	
	// Methods to create finding rules
	/**
	 * EQUALS - indexed should be equal to specified value (including null).
	 * Apply an "equals" constraint to the indexed value  
	 *   
	 * @param index What index to use to find value?
	 * @param value What value to be equal to? Might be null as nulls are also indexed values 
	 * @return Equals expression 
	 */
	public Expression<E, K, L> eq(L index, Object value);
	
	/**
	 * GREATER THAN Not inclusive
	 * Apply an "greater than" constraint to the index
	 *  
	 * @param index What index to use to find value?
	 * @param value Minimum value. Value is not inclusive.
	 * @return Expression of greater than not inclusive
	 * @see {@link #gt(Object, Object, boolean)}  
	 */
	public Expression<E, K, L> gt(L index, Object value);	
	
	/**
     * GREATER THAN
	 * Apply a "greater than" constraint to the index
	 *  
	 * @param index What index to use to find value?
	 * @param value Minimum value
	 * @param inclusive Could be <code>value</code> inclusive for greater than  
	 * @return Expression of greater than
	 */
	public Expression<E, K, L> gt(L index, Object value, boolean inclusive);
	
	/**
	 * LESS THAN Not inclusive
	 * 
	 * @param index What index to use to find value?
	 * @param value Maximum value. Value is not inclusive
	 * @return Expression of less than not inclusive
	 */
	public Expression<E, K, L> lt(L index, Object value);
	
	/**
	 * LESS THAN Not inclusive
	 * 
	 * @param index What index to use to find value?
	 * @param value Maximum value. Value is not inclusive
	 * @param inclusive Could be <code>value</code> inclusive for less than?
	 * @return Expression of less than not inclusive
	 */
	public Expression<E, K, L> lt(L index, Object value, boolean inclusive);
	
	/**
	 * BETWEEN Not inclusive
	 * 
	 * @param index What index to use to find value?
	 * @param valueFrom Minimum value
	 * @param valueTo Maximum value
	 * @return Expression of between not inclusive
	 */
	public Expression<E, K, L> between(L index, Object valueFrom, Object valueTo);
	
	/**
	 * BETWEEN 
	 * 
	 * @param index What index to use to find value?
	 * @param valueFrom Minimum value
	 * @param inclusiveFrom Could be minimum value inclusive?
	 * @param valueTo Maximum value
	 * @param inclusiveTo Could be maximum value inclusive?
	 * @return Expression of between not inclusive
	 */
	public Expression<E, K, L> between(L index, Object valueFrom, boolean inclusiveFrom, Object valueTo, boolean inclusiveTo);
	
	/**
	 * IS NULL
	 * 
	 * @param index What index to use to find value?
	 * @return Expression for is null
	 */
	public Expression<E, K, L> isNull(L index);
	
	/**
	 * IS NOT NULL
	 * @param index What index to use to find value?
	 * @return Expression for is not null
	 */
	public Expression<E, K, L> isNotNull(L index);			
	
	/**
	 * IN clause
	 * 
	 * @param index What index to use to find value?
	 * @param values What values might be included?
	 * @return Expression for in clause
	 */
	public Expression<E, K, L> in(L index, Object[] values);
	
	/**
	 * Creates AND operator. All constraints to apply AND operator 
	 * should be added using method {@link Conjunction#add(ICriterion)}
	 * @return AND operator for criterions
	 */
	public Conjunction<E, K, L> conjunction();
	
	/**
	 * Creates OR operator. All constraints to apply AND operator 
	 * should be added using method {@link Disjunction#add(ICriterion)}
	 * @return OR operator for criterions
	 */
	public Disjunction<E, K, L> disjunction();
	
	/**
	 * Gets how many values are indexed by specified criterion.
	 * For expressions count of values is returned, for Junctions //TODO how this should behave for junctions?  
	 *  
	 * @param criterion For what criterion 
	 * @return Count of distinct indexed vales
	 */
	public int getCountOfIndexedValues(ICriterion<E, K, L> criterion);
}
