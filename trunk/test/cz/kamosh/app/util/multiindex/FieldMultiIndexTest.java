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
package cz.kamosh.app.util.multiindex;

import java.util.Collection;

import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.impl.MultiIndexContainerFields;
import cz.kamosh.multiindex.interf.IMultiIndexContainerFields;

public class FieldMultiIndexTest
		extends
		AbstractMultiIndexContainerTest<String, IMultiIndexContainerFields<Person, Integer>> {

	@Override
	protected IMultiIndexContainerFields<Person, Integer> createMultiIndexContainer() {
		return new MultiIndexContainerFields<Person, Integer>(Person.class);
	}

	@Override
	protected IMultiIndexContainerFields<Person, Integer> createMultiIndexContainer(
			Collection<Person> people) {
		return new MultiIndexContainerFields<Person, Integer>(Person.class,
				people);
	}

	@Override
	protected void addIndexForBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic) {
		mic.addIndex("birthYear");
	}

	@Override
	protected void addIndexForSurname(
			IMultiIndexContainerFields<Person, Integer> mic) {
		mic.addIndex("surname");
	}

	@Override
	protected void addIndexForSex(
			IMultiIndexContainerFields<Person, Integer> mic) {
		mic.addIndex("man");
	}

	@Override
	protected Collection<Person> findEqBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic, int birthYear) {
		return mic.find(mic.eq("birthYear", birthYear));
	}

	@Override
	protected Collection<Person> findInBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic,
			Integer[] birthYears) {
		return mic.find(mic.in("birthYear", birthYears));
	}

	@Override
	protected ICriterion<Person, Integer, String> createBetweenBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic, int minBirthYear,
			int maxBirthYear) {
		return mic.between("birthYear", minBirthYear, maxBirthYear);
	}

	@Override
	protected ICriterion<Person, Integer, String> createEqBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic, int birthYear) {
		return mic.eq("birthYear", birthYear);
	}

	@Override
	protected ICriterion<Person, Integer, String> createEqSex(
			IMultiIndexContainerFields<Person, Integer> mic, boolean shouldBeMan) {
		return mic.eq("man", shouldBeMan);
	}

	@Override
	protected ICriterion<Person, Integer, String> createIsNullSurname(
			IMultiIndexContainerFields<Person, Integer> mic) {
		return mic.isNull("surname");
	}

	@Override
	protected ICriterion<Person, Integer, String> createLTBirthYear(
			IMultiIndexContainerFields<Person, Integer> mic, int birthYear) {
		return mic.lt("birthYear", birthYear);
	}

}
