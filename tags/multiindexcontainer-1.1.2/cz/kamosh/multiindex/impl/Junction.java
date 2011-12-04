/*
 *  Main authors:
 *     Fekete Kamosh <fekete.kamosh@gmail.com> 
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.interf.IMultiIndexContainer;
import cz.kamosh.multiindex.interf.IMultiIndexed;
import cz.kamosh.parallel.Parallel;

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
		public Collection<E> getRecordInstances(final IMultiIndexContainer container) {
			Collection<E> result = Collections.<E> emptySet(); // Only to assure
																// local
																// initialization
			// Optimization - applying criterions start from those which
			// have likely least records for one indexed value!
			// TODO Try to think out how to have sorted criterions automatically
			List<ICriterion<E, K, L>> tempCriterions = new ArrayList<ICriterion<E, K, L>>();
			tempCriterions.addAll(children);
			Collections.sort(tempCriterions,
					new java.util.Comparator<ICriterion<E, K, L>>() {
						@Override
						public int compare(ICriterion<E, K, L> o1,
								ICriterion<E, K, L> o2) {
							return container.getCountOfIndexedValues(o1) - container.getCountOfIndexedValues(o2);
						}
					});

			// Loop over all children and perform retain all among all
			boolean firstPassed = false;
			for (ICriterion<E, K, L> criterion : tempCriterions) {
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
		public Collection<E> getRecordInstances(
				final IMultiIndexContainer container) {
			final Collection<E> result = new HashSet<E>(); // Only to assure
															// local
															// initialization
			// Loop over all children and add all records fulfilling criterion
			if (MultiIndexSetting.getInstance().isUseParalellization()) {
				Parallel.For(children,
						new Parallel.Operation<ICriterion<E, K, L>>() {
							@Override
							public void perform(ICriterion<E, K, L> criterion) {
								result.addAll(criterion
										.getRecordInstances(container));

							}
						});
			} else {
				for (ICriterion<E, K, L> criterion : children) {
					// OR operator applied
					result.addAll(criterion.getRecordInstances(container));
				}
			}
			return result;
		}
	}

}
