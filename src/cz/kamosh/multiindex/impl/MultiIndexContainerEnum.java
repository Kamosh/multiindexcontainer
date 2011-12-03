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

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import cz.kamosh.multiindex.criterion.Expression.LOOKUP_OPERATOR;
import cz.kamosh.multiindex.interf.IMultiIndexContainerEnum;
import cz.kamosh.multiindex.interf.IMultiIndexed;
import cz.kamosh.multiindex.interf.Indexable;

/**
 * Multiindex serves as collection for unique records that could be indexed by
 * specified index constant. Index constants should be created for data which
 * should be indexed.
 * 
 * @param <E>
 *            Record type in container
 * @param <K>
 *            Key type of record
 */
public class MultiIndexContainerEnum<E extends IMultiIndexed<K>, K extends Object>
		extends MultiIndexContainer<E, K, Indexable<E>> implements
		IMultiIndexContainerEnum<E, K> {

	private static final Logger LOGGER = Logger
			.getLogger(MultiIndexContainerEnum.class.getName());

	/**
	 * Constructor for MultiIndexContainer, empty after creation
	 */
	public MultiIndexContainerEnum() {
		// No data are added in fact
		this(Collections.<E> emptySet());
	}

	/**
	 * @param records
	 *            Instances of records to be added into container
	 */
	public MultiIndexContainerEnum(Collection<E> records) {
		super();
		addAll(records);
	}

	// ------ Implementation of MultiIndexContainer ---------- /
	@Override
	EnumDataGetter getDataGetter(Indexable<E> index) {
		return new EnumDataGetter(index);
	}

	class EnumDataGetter extends DataGetter {
		Indexable<E> indexedAttribute;

		EnumDataGetter(Indexable<E> indexedAttribute) {
			this.indexedAttribute = indexedAttribute;
		}

		Object getData(E record) {
			return indexedAttribute.getIndexedValue(record);
		};
	}

	// ---------- Methods to create rules to find data -----/
	@Override
	public ExpressionEnum<E, K> eq(Indexable<E> index, Object value) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.EQUAL, index,
				new Object[] { value });
	}

	@Override
	public ExpressionEnum<E, K> gt(Indexable<E> index, Object value) {
		return gt(index, value, false);
	}

	@Override
	public ExpressionEnum<E, K> gt(Indexable<E> index, Object value,
			boolean inclusive) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.GREATER, index, value,
				inclusive);
	}

	@Override
	public ExpressionEnum<E, K> lt(Indexable<E> index, Object value) {
		return lt(index, value, false);
	}

	@Override
	public ExpressionEnum<E, K> lt(Indexable<E> index, Object value,
			boolean inclusive) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.LESS, index, value,
				inclusive);
	}

	@Override
	public ExpressionEnum<E, K> between(Indexable<E> index, Object valueFrom,
			Object valueTo) {
		return between(index, valueFrom, false, valueTo, false);
	}

	@Override
	public ExpressionEnum<E, K> between(Indexable<E> index, Object valueFrom,
			boolean inclusiveFrom, Object valueTo, boolean inclusiveTo) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.BETWEEN, index,
				valueFrom, inclusiveFrom, valueTo, inclusiveTo);
	}

	@Override
	public ExpressionEnum<E, K> isNull(Indexable<E> index) {
		return eq(index, null);
	}

	@Override
	public ExpressionEnum<E, K> isNotNull(Indexable<E> index) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.IS_NOT_NULL, index,
				null, false);
	}

	@Override
	public ExpressionEnum<E, K> in(Indexable<E> index, Object[] values) {
		return new ExpressionEnum<E, K>(LOOKUP_OPERATOR.IN, index, values);
	}

}