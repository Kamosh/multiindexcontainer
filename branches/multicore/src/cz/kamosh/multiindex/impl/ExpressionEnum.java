package cz.kamosh.multiindex.impl;

/*
 *  Main authors:
 *     Fekete Kamosh  <fekete.kamosh@gmail.com>
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

import cz.kamosh.multiindex.criterion.Expression;
import cz.kamosh.multiindex.interf.IMultiIndexed;
import cz.kamosh.multiindex.interf.Indexable;

/**
 * Cannot be converted to static class as then it is not possible to make use of annotation <E>
 * in MultiIndexContainerEnum 
 * @param <E> Record type in container
 * @param <K> Key type of record
 */
public final class ExpressionEnum<E extends IMultiIndexed<K>, K extends Object> extends Expression<E, K, Indexable<E>> {
    

	ExpressionEnum(LOOKUP_OPERATOR operator, Indexable<E> index,
			Object value, boolean inclusive) {
		super(index, operator, value, true, null, false);		
	}
	
	ExpressionEnum(LOOKUP_OPERATOR operator, Indexable<E> index, Object[] values) {
		super(index, operator, values);		
	}

	ExpressionEnum(LOOKUP_OPERATOR operator, Indexable<E> index,
			Object valueFrom, boolean inclusiveFrom, Object valueTo,
			boolean inclusiveTo) {
		super(index, operator, valueFrom, inclusiveFrom, valueTo, inclusiveTo);
			
	}		
		       
}