/*
The Tea Software License, Version 1.1.1

Copyright (c) 2002 Walt Disney Internet Group. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. The end-user documentation included with the redistribution,
   if any, must include the following acknowledgment:
      "This product includes software developed by the
       Walt Disney Internet Group."
   Alternately, this acknowledgment may appear in the software itself,
   if and wherever such third-party acknowledgments normally appear.

4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "BeanDoc" must
   not be used to endorse or promote products derived from this
   software without prior written permission. For written
   permission, please contact opensource@dig.com.

5. Products derived from this software may not be called "Tea",
   "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
   "Kettle", "Trove" or "BeanDoc" appear in their name, without prior
   written permission of the Walt Disney Internet Group.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE WALT DISNEY INTERNET GROUP OR ITS
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.kamosh.multiindex.utils;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

/**
 * *************************************************************************** A
 * Map supporting null keys that wraps a TreeMap that doesn't support null keys.
 * NullKeyMap substitutes null keys with a special placeholder object.
 * 
 * NullKeyMap borrowed from project http://teatrove.sourceforge.net/
 */
public class NullKeyMap<K, V> extends TreeMap<K, V> implements Serializable {

	private static final Serializable DUMMY_NULL = new Serializable() {
	};

	// Instead of using null as a key, use this placeholder.
	private final K NULL = (K) DUMMY_NULL;

	// private TreeMap<K, V> mMap;

	private transient Set mKeySet;
	private transient Set mEntrySet;

	/**
	 * @param map
	 *            The map to wrap.
	 */
	public NullKeyMap(TreeMap<K, V> map) {
		super(map);
	}

	public boolean containsKey(Object key) {
		return (key == null) ? super.containsKey(NULL) : super.containsKey(key);
	}

	public V get(Object key) {
		return (key == null) ? super.get(NULL) : super.get(key);
	}

	public V put(K key, V value) {
		return super.put((key == null) ? NULL : key, value);
	}

	public V remove(Object key) {
		return (key == null) ? super.remove(NULL) : super.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		// Need to call own method put
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<K, V> entry = (Map.Entry) it.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	private Set keySetInner() {
		return super.keySet();
	}

	@Override
	public Set<K> keySet() {
		if (mKeySet == null) {
			mKeySet = new AbstractSet() {
				public Iterator iterator() {
					final Iterator it = keySetInner().iterator();

					return new Iterator() {
						public boolean hasNext() {
							return it.hasNext();
						}

						public Object next() {
							Object key = it.next();
							return (key == NULL) ? null : key;
						}

						public void remove() {
							it.remove();
						}
					};
				}

				public boolean contains(Object key) {
					return containsKey((key == null) ? NULL : key);
				}

				public boolean remove(Object key) {
					if (key == null) {
						key = NULL;
					}
					if (containsKey(key)) {
						NullKeyMap.this.remove(key);
						return true;
					} else {
						return false;
					}
				}

				public int size() {
					return NullKeyMap.this.size();
				}

				public void clear() {
					NullKeyMap.this.clear();
				}
			};
		}

		return mKeySet;
	}

	private Set entrySetInner() {
		return super.entrySet();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (mEntrySet == null) {
			mEntrySet = new AbstractSet() {
				public Iterator iterator() {
					final Iterator it = entrySetInner().iterator();

					return new Iterator() {
						public boolean hasNext() {
							return it.hasNext();
						}

						public Object next() {
							final Map.Entry entry = (Map.Entry) it.next();
							if (entry.getKey() == NULL) {
								return new NullKeyAbstractMapEntry() {
									public Object getKey() {
										return null;
									}

									public Object getValue() {
										return entry.getValue();
									}

									public Object setValue(Object value) {
										return entry.setValue(value);
									}
								};
							} else {
								return entry;
							}
						}

						public void remove() {
							it.remove();
						}
					};
				}

				public boolean contains(Object obj) {
					if (!(obj instanceof Map.Entry)) {
						return false;
					}
					Map.Entry entry = (Map.Entry) obj;
					Object key = entry.getKey();
					Object value = entry.getValue();
					if (key == null) {
						key = NULL;
					}
					Object lookup = get(key);
					if (lookup == null) {
						return value == null;
					} else {
						return lookup.equals(value);
					}
				}

				public boolean remove(Object obj) {
					if (!(obj instanceof Map.Entry)) {
						return false;
					}
					Map.Entry entry = ((Map.Entry) obj);
					Object key = entry.getKey();
					if (key == null) {
						key = NULL;
					}
					if (containsKey(key)) {
						NullKeyMap.this.remove(key);
						return true;
					} else {
						return false;
					}
				}

				public int size() {
					return NullKeyMap.this.size();
				}

				public void clear() {
					NullKeyMap.this.clear();
				}
			};
		}

		return mEntrySet;
	}

	/**
	 * *************************************************************************
	 * ** Abstract Map.Entry implementation that makes it easier to define new
	 * Map entries.
	 * 
	 * @author Brian S O'Neill
	 * @version <!--$$Revision:--> 13 <!-- $-->, <!--$$JustDate:--> 1/23/01 <!--
	 *          $-->
	 */
	public abstract class NullKeyAbstractMapEntry implements Map.Entry {
		/**
		 * Always throws UnsupportedOperationException.
		 */
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Map.Entry)) {
				return false;
			}

			Map.Entry e = (Map.Entry) obj;

			Object key = getKey();
			Object value = getValue();

			return (key == null ? e.getKey() == null : key.equals(e.getKey()))
					&& (value == null ? e.getValue() == null : value.equals(e
							.getValue()));
		}

		public int hashCode() {
			Object key = getKey();
			Object value = getValue();

			return (key == null ? 0 : key.hashCode())
					^ (value == null ? 0 : value.hashCode());
		}

		public String toString() {
			return String.valueOf(getKey()) + '=' + String.valueOf(getValue());
		}
	}

	// Required comparators

	private static final Comparator NULL_LOW_ORDER = new NullLowOrder();

	private static final Comparator NULL_HIGH_ORDER = new NullHighOrder();

	/**
	 * Returns a Comparator that uses a Comparable object's natural ordering,
	 * except null values are always considered low order. This Comparator
	 * allows naturally ordered TreeMaps to support null values.
	 */
	public static Comparator nullLowOrder() {
		return NULL_LOW_ORDER;
	}

	/**
	 * Returns a Comparator that wraps the given Comparator except null values
	 * are always considered low order. This fixes Comparators that don't
	 * support comparisons against null and allows them to be used in TreeMaps.
	 */
	public static Comparator nullLowOrder(Comparator c) {
		return new NullLowOrderC(c);
	}

	/**
	 * Returns a Comparator that uses a Comparable object's natural ordering,
	 * except null values are always considered high order. This Comparator
	 * allows naturally ordered TreeMaps to support null values.
	 */
	public static Comparator nullHighOrder() {
		return NULL_HIGH_ORDER;
	}

	/**
	 * Returns a Comparator that wraps the given Comparator except null values
	 * are always considered high order. This fixes Comparators that don't
	 * support comparisons against null and allows them to be used in TreeMaps.
	 */
	public static Comparator nullHighOrder(Comparator c) {
		return new NullHighOrderC(c);
	}

	private static class NullLowOrder implements Comparator, Serializable {
		public int compare(Object obj1, Object obj2) {
			if (obj1 == DUMMY_NULL && obj2 == DUMMY_NULL) {
				return 0;
			} else if (obj1 == DUMMY_NULL) {
				return -1;
			} else if (obj2 == DUMMY_NULL) {
				return 1;
			} else {
				return ((Comparable) obj1).compareTo(obj2);
			}
		}

		// Serializable singleton classes should always do this.
		private Object readResolve() throws ObjectStreamException {
			return NULL_LOW_ORDER;
		}
	}

	private static class NullLowOrderC implements Comparator, Serializable {
		private Comparator c;

		public NullLowOrderC(Comparator c) {
			this.c = c;
		}

		public int compare(Object obj1, Object obj2) {
			if (obj1 == DUMMY_NULL && obj2 == DUMMY_NULL) {
				return 0;
			} else if (obj1 == DUMMY_NULL) {
				return -1;
			} else if (obj2 == DUMMY_NULL) {
				return 1;
			} else {
				return c.compare(obj1, obj2);
			}
		}
	}

	private static class NullHighOrder implements Comparator, Serializable {
		public int compare(Object obj1, Object obj2) {
			if (obj1 == DUMMY_NULL && obj2 == DUMMY_NULL) {
				return 0;
			} else if (obj1 == DUMMY_NULL) {
				return +1;
			} else if (obj2 == DUMMY_NULL) {
				return -1;
			} else {
				return ((Comparable) obj1).compareTo(obj2);
			}
		}

		// Serializable singleton classes should always do this
		private Object readResolve() throws ObjectStreamException {
			return NULL_HIGH_ORDER;
		}
	}

	private static class NullHighOrderC implements Comparator, Serializable {
		private Comparator c;

		public NullHighOrderC(Comparator c) {
			this.c = c;
		}

		public int compare(Object obj1, Object obj2) {
			if (obj1 == DUMMY_NULL && obj2 == DUMMY_NULL) {
				return 0;
			} else if (obj1 == DUMMY_NULL) {
				return +1;
			} else if (obj2 == DUMMY_NULL) {
				return -1;
			} else {
				return c.compare(obj1, obj2);
			}
		}
	}

}