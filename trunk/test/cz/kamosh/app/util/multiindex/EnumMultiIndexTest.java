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
import java.util.logging.Logger;

import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.impl.MultiIndexContainerEnum;
import cz.kamosh.multiindex.interf.IMultiIndexContainerEnum;
import cz.kamosh.multiindex.interf.Indexable;

public class EnumMultiIndexTest
		extends
		AbstractMultiIndexContainerTest<Indexable<Person>, IMultiIndexContainerEnum<Person, Integer>> {

	private static final Logger logger = Logger
			.getLogger(EnumMultiIndexTest.class.getName());

	@Override
	protected IMultiIndexContainerEnum<Person, Integer> createMultiIndexContainer() {
		return new MultiIndexContainerEnum<Person, Integer>();
	}

	@Override
	protected IMultiIndexContainerEnum<Person, Integer> createMultiIndexContainer(
			Collection<Person> people) {
		return new MultiIndexContainerEnum<Person, Integer>(people);
	}

	@Override
	protected void addIndexForBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic) {
		mic.addIndex(PersonTest_Indexes.BIRTH_YEAR);
	}

	@Override
	protected void addIndexForSurname(
			IMultiIndexContainerEnum<Person, Integer> mic) {
		mic.addIndex(PersonTest_Indexes.SURNAME);
		logger.info("Established index for birthYear");
	}

	@Override
	protected void addIndexForSex(IMultiIndexContainerEnum<Person, Integer> mic) {
		mic.addIndex(PersonTest_Indexes.SEX);
		logger.info("Established index for sex");
	}

	@Override
	protected Collection<Person> findEqBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic, int birthYear) {
		return mic.find(mic.eq(PersonTest_Indexes.BIRTH_YEAR, birthYear));
	}

	@Override
	protected Collection<Person> findInBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic, Integer[] birthYears) {
		return mic.find(mic.in(PersonTest_Indexes.BIRTH_YEAR, birthYears));
	}

	@Override
	protected ICriterion<Person, Integer, Indexable<Person>> createBetweenBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic, int minBirthYear,
			int maxBirthYear) {
		return mic.between(PersonTest_Indexes.BIRTH_YEAR, minBirthYear,
				maxBirthYear);
	}

	@Override
	protected ICriterion<Person, Integer, Indexable<Person>> createEqBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic, int birthYear) {
		return mic.eq(PersonTest_Indexes.BIRTH_YEAR, birthYear);
	}

	@Override
	protected ICriterion<Person, Integer, Indexable<Person>> createEqSex(
			IMultiIndexContainerEnum<Person, Integer> mic, boolean shouldBeMan) {
		return mic.eq(PersonTest_Indexes.SEX, shouldBeMan);
	}

	@Override
	protected ICriterion<Person, Integer, Indexable<Person>> createIsNullSurname(
			IMultiIndexContainerEnum<Person, Integer> mic) {
		return mic.isNull(PersonTest_Indexes.SURNAME);
	}

	@Override
	protected ICriterion<Person, Integer, Indexable<Person>> createLTBirthYear(
			IMultiIndexContainerEnum<Person, Integer> mic, int birthYear) {
		return mic.lt(PersonTest_Indexes.BIRTH_YEAR, birthYear);
	}
}
