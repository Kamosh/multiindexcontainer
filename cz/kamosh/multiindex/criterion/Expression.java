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
package cz.kamosh.multiindex.criterion;

import java.util.Collection;

import cz.kamosh.multiindex.interf.IMultiIndexContainer;
import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Basic class to describe expression 
 * @param <E> Type of record
 * @param <K> Type of primary key of record 
 * @param <L> Type of index used for container. Now there is possibility to use <code>String</code> or <code>Indexable</code>
 */
public abstract class Expression<E extends IMultiIndexed<K>, K extends Object, L> implements ICriterion<E, K, L> {

	/**
	 * Supported lookup operations on indexed data
	 */
	public enum LOOKUP_OPERATOR {
		EQUAL, BETWEEN, LESS, GREATER, IS_NOT_NULL, IS_NULL, IN 
	}

	/**
	 * Instance of index used in this lookup rule
	 */
	private L index; 
	
	public L getIndex() {
		return index;
	}

	/**
	 * Resulting lookup operator
	 */
	private LOOKUP_OPERATOR operator;
	private Object valueFrom;
	private boolean inclusiveFrom;
	private Object valueTo;
	private boolean inclusiveTo;	
	// For multiple seeked values for lookup operator EQUAL/IN/IS_NULL
	private Object[] values;

	protected Expression(L index, LOOKUP_OPERATOR operator, Object valueFrom, boolean inclusiveFrom, Object valueTo, boolean inclusiveTo) {
		this.index = index;
		//TODO check for between operator
    	this.operator = operator;
    	this.valueFrom = valueFrom;
    	this.inclusiveFrom = inclusiveFrom;
    	this.valueTo = valueFrom;
    	this.inclusiveTo = inclusiveTo;
	}	    
    
    protected Expression(L index, LOOKUP_OPERATOR operator, Object... values) {
    	this.index = index;
    	this.operator = operator;
    	this.values = values;
    }
	
	final public LOOKUP_OPERATOR getOperator() {
		return operator;
	}

	final public Object getValueFrom() {
		return valueFrom;
	}

	final public Object getValueTo() {
		return valueTo;
	}

	final public Object[] getValues() {
		return values;
	}

	final public boolean isInclusiveFrom() {
		return inclusiveFrom;
	}

	final public boolean isInclusiveTo() {
		return inclusiveTo;
	}			

	@Override
	public Collection<E> getRecordInstances(IMultiIndexContainer<E, K, L> container) {
		return container.getRecordInstances(this);		
	}
	
}
