/*
 *  Main authors:
 *     Fekete Kamosh <fekete.kamosh@gmail.com> 
 * 
 *  Copyright:
 *     Fekete Kamosh, 2010 
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
import java.util.HashSet;
import java.util.Set;

import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.interf.IMultiIndexContainer;
import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Class to allow logical operators AND and OR
 * 
 * @param <E>
 *            Class of record which will should be multiindexed
 * @param <L>
 *            Type of index used for container. Now there is possibility to use
 *            <code>String</code> or <code>Indexable</code>
 */
public abstract class Junction<E extends IMultiIndexed<K>, K extends Object, L>
		implements ICriterion<E, K, L> {

	// TODO perform checking for infinite loop!
	protected Set<ICriterion<E, K, L>> children = new HashSet<ICriterion<E, K, L>>();

	public Junction() {
	}

	/**
	 * Add child criterion under specified junction.
	 * 
	 * @param child
	 *            What criterion to add?
	 * @return Junction with already added child criterion
	 */
	public final Junction<E, K, L> add(ICriterion<E, K, L> child) {
		children.add(child);
		return this;
	}

	/**
	 * Operator AND applied as operator among all children criterions
	 */
	public static class Conjunction<E extends IMultiIndexed<K>, K extends Object, L>
			extends Junction<E, K, L> {
		@Override
		public Collection<E> getRecordInstances(IMultiIndexContainer container) {
			Collection<E> result = Collections.<E> emptySet(); // Only to assure
																// local
																// initialization
			// Loop over all children and perform retain all among all
			boolean firstPassed = false;
			for (ICriterion<E, K, L> criterion : children) {
				if (!firstPassed) {
					result = criterion.getRecordInstances(container);
					firstPassed = true;
				} else {
					// AND operator applied
					result.retainAll(criterion.getRecordInstances(container));
				}
				// It has no sense to continue if we have no records to perform
				// AND operator
				if (result.isEmpty()) {
					break;
				}
			}
			return result;
		}
	}

	/**
	 * Operator OR applied as operator among all children criterions // TODO
	 * could be used multithread approach
	 */
	public static class Disjunction<E extends IMultiIndexed<K>, K extends Object, L>
			extends Junction<E, K, L> {
		@Override
		public Collection<E> getRecordInstances(IMultiIndexContainer container) {
			Collection<E> result = new HashSet<E>(); // Only to assure local
														// initialization
			// Loop over all children and perform retain all among all
			for (ICriterion<E, K, L> criterion : children) {
				// OR operator applied
				result.addAll(criterion.getRecordInstances(container));
			}
			return result;
		}
	}

}
