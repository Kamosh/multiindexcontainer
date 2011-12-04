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

import cz.kamosh.multiindex.criterion.Expression;
import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Generic parameters specified only to imply strongly typed usage of LookupRuleFields
 *
 * @param <E> Record type
 * @param <K> Primary key type
 */
public final class ExpressionFields<E extends IMultiIndexed<K>, K extends Object> extends Expression<E, K, String> {
		
	ExpressionFields(LOOKUP_OPERATOR operator, String indexName,
			Object value, boolean inclusive) {
		super(indexName, operator, value, true, null, false);		
	}

	ExpressionFields(LOOKUP_OPERATOR operator, String indexName,
			Object valueFrom, boolean inclusiveFrom, Object valueTo,
			boolean inclusiveTo) {
		super(indexName, operator, valueFrom, inclusiveFrom, valueTo, inclusiveTo);		
	}

	ExpressionFields(LOOKUP_OPERATOR operator, String indexName,
			Object... values) {
		super(indexName, operator, values);		
	}	
			

		
}