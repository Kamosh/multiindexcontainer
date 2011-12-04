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
 * General criterion used to specify conditions how to find records in container
 * <p/>
 * Parameters used to preserve link proper criterion to proper container
 * 
 * @param <E>
 *            Record type in container
 * @param <K>
 *            Key type of record
 * @param <L>
 *            Type of index used for container. Now there is possibility to use
 *            <code>String</code> or <code>Indexable</code>
 */
public interface ICriterion<E extends IMultiIndexed<K>, K extends Object, L> {

	/**
	 * Method to find records resulting from all <code>lookupRules</code> in
	 * parameter
	 * 
	 * @param What
	 *            container to use get values?
	 */
	public Collection<E> getRecordInstances(
			IMultiIndexContainer<E, K, L> container);	
}
