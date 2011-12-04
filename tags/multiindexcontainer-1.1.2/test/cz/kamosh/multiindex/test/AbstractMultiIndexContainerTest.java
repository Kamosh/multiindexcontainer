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

package cz.kamosh.multiindex.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import cz.kamosh.multiindex.criterion.ICriterion;
import cz.kamosh.multiindex.impl.Junction;
import cz.kamosh.multiindex.impl.Junction.Conjunction;
import cz.kamosh.multiindex.impl.MultiIndexSetting;
import cz.kamosh.multiindex.interf.IMultiIndexContainer;

public abstract class AbstractMultiIndexContainerTest<L, T extends IMultiIndexContainer<Person, Integer, L>> {
	private static final Logger LOGGER = Logger
			.getLogger(AbstractMultiIndexContainerTest.class.getName());

	// All methods needed for all tests to be implemented
	protected abstract T createMultiIndexContainer();

	protected abstract T createMultiIndexContainer(Collection<Person> people);

	protected abstract void addIndexForBirthYear(T mic);

	protected abstract void addIndexForSurname(T mic);

	protected abstract void addIndexForSex(T mic);
	
	protected abstract void addIndexForBMI(T mic);
	
	protected abstract void addIndexForBirtYearAndSurname(T mic);

	protected abstract Collection<Person> findEqBirthYear(T mic, int birthYear);

	protected abstract Collection<Person> findInBirthYear(T mic,
			Integer[] birthYears);

	protected abstract ICriterion<Person, Integer, L> createBetweenBirthYear(
			T mic, int minBirthYear, int maxBirthYear);

	protected abstract ICriterion<Person, Integer, L> createEqSex(T mic,
			boolean shouldBeMan);

	protected abstract ICriterion<Person, Integer, L> createEqBirthYear(T mic,
			int birthYear);

	protected abstract ICriterion<Person, Integer, L> createIsNullSurname(T mic);

	protected abstract ICriterion<Person, Integer, L> createLTBirthYear(T mic,
			int birthYear);
	
	protected abstract ICriterion<Person, Integer, L> createLTBMI(T mic, 
			double bmi);

	public AbstractMultiIndexContainerTest() {

	}

	
	@After
	public void clearMemory() {
		System.gc();
	}
	
	@Test
	public void testCreateMultiIndexContainerNonParallel() throws IOException {	
		// 3276 ms in average for 50 loops (with gc)
		// 6480 in 10 loops (with gc)
		LOGGER.info("testCreateMultiIndexContainerNonParallel");
		innerCreateMultiIndexContainer(false);
	}
	
	@Test
	public void testCreateMultiIndexContainerParallel() {
		// 1811 ms in average for 50 loops (with gc)
		// 5098 in 10 loops (without gc)
		LOGGER.info("testCreateMultiIndexContainerNonParallel");
		innerCreateMultiIndexContainer(true);
	}
	
	/**
	 * Test to decide whether parallel indexing increases performance 
	 */	
	private void innerCreateMultiIndexContainer(boolean useParallel) {
		final int LOOPS = 1;
		// Number of create people
		int count = 1000000; //1M
		Collection<Person> people = Person.generatePeople(count);
		
		long allTimes = 0;
		long[] distinctTimes = new long[LOOPS];
		MultiIndexSetting.getInstance().setUseParalellization(useParallel);
		for (int i = 0; i < LOOPS; i++) {					
			TimeElapser te = new TimeElapser();
			T mic = createMultiIndexContainer();
			addIndexForBirthYear(mic);
			addIndexForBMI(mic);
			addIndexForSurname(mic);
			addIndexForSex(mic);
			mic.addAll(people);
			allTimes += te.end();
			distinctTimes[i] = te.end();
			LOGGER.info("Elapsed time to add " + count + " people is "
					+ te.end() + " ms");
		}
		LOGGER.info("All times: " + Arrays.toString(distinctTimes));
		LOGGER.info("Average time to add " + count + " people is " + allTimes/LOOPS);
	}

	
	@Test
	public void testConstructor() {
		LOGGER.info("testConstructor");
		T mic = createMultiIndexContainer();
		Assert.assertTrue("MultiIndexContainer constructor has not passed",
				true);
	}

	@Test
	public void testConstructorWithValues10() {
		LOGGER.info("testConstructorWithValues");
		// Number of created people
		int count = 10;
		Collection<Person> people = Person.generatePeople(count);

		T mic = createMultiIndexContainer(people);

		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	}

	/**
	 * Test for bulk data addition. Without any index
	 * <P>
	 * There is also measured time to add all people into multiindex main
	 * collection
	 */
	@Test
	public void testBulkData1M() {
		LOGGER.info("testBulkData1M");
		// Number of created people
		int count = 1000000; // 1 M
		Collection<Person> people = Person.generatePeople(count);

		TimeElapser te = new TimeElapser();

		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");
		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	} 
	
	/**
	 * Test for creating index for <code>birthYear</code> attribute
	 */
	@Test
	public void testIndexBirthYear100K() {
		LOGGER.info("testIndexBirthYear");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		LOGGER.info("Elapsed time to create index birthYear for " + count
				+ " people is " + te.end() + " ms");

		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	}

	/**
	 * Test for creating index for <code>surname</code> attribute
	 */
	@Test
	public void testIndexSurname100K() {
		LOGGER.info("testIndexSurname");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "surname"
		te.start();
		String attributeName = "surname";
		addIndexForSurname(mic);
		LOGGER.info("Elapsed time to create index \"" + attributeName
				+ "\" for " + count + " people is " + te.end() + " ms");

		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	}

	/**
	 * Test for creating index first and then add data
	 */
	@Test
	public void testIndexBeforeData100K() {
		LOGGER.info("testAheadIndex");
		// Number of created people
		int count = 100000;

		// Lets create multi index container for Person class
		T mic = createMultiIndexContainer();
		LOGGER.info("Created emtpy MultiIndexContainerEnum");

		// Create index for "birthYear"
		addIndexForBirthYear(mic);
		LOGGER.info("Established index for birthYear");

		// Create index for "surname"
		addIndexForSurname(mic);
		LOGGER.info("Established index for surname");

		Collection<Person> people = Person.generatePeople(count);
		TimeElapser te = new TimeElapser();
		// Adding all people
		mic.addAll(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	}

	/**
	 * Method to test find by index on birthYear column
	 */
	@Test
	public void testFindByIndex100K() {
		LOGGER.info("testFindByIndex");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets create multi index container for Person class
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Created filled MultiIndexContainer");

		// Create index for "birthYear"
		addIndexForBirthYear(mic);
		LOGGER.info("Established index for birthYear");

		int birthYear = 1977;
		Collection<Person> peopleBorn1977 = findEqBirthYear(mic, birthYear);
		LOGGER.info("There are " + peopleBorn1977.size() + " people born in "
				+ birthYear);

		Assert.assertTrue("There should be at least one person born in "
				+ birthYear, peopleBorn1977.size() > 0);
	}

	/**
	 * Method to test find by index on birthYear in multiple values
	 */
	@Test
	public void testFindByIndexWithMultipleValues100K() {
		LOGGER.info("testFindByIndex");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets create multiindex container for Person class
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Created emtpy MultiIndexContainerEnum");

		// Create index for "birthYear"
		addIndexForBirthYear(mic);

		int birthYear1977 = 1977;
		int birthYear1978 = 1978;
		int birthYear1979 = 1979;
		Integer[] birthYears = { birthYear1977, birthYear1978, birthYear1979 };
		Collection<Person> peopleBorn1977_9 = findInBirthYear(mic, birthYears);

		LOGGER.info("There are " + peopleBorn1977_9.size() + " people born in "
				+ birthYear1977 + "," + birthYear1978 + "," + birthYear1979);

		Assert.assertTrue("There should be at least one person born in "
				+ birthYear1977 + "," + birthYear1978 + "," + birthYear1979,
				peopleBorn1977_9.size() > 0);
	}

	/**
	 * Method to test find without index and with index possibility of
	 * MultiIndexContainerPureIndexes
	 */
	@Test
	public void testFindByWithAndWithoutIndex100K() {
		LOGGER.info("testFindByWithAndWithoutIndex");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets create multi index container for Person class
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Created empty MultiIndexContainerEnum");

		int birthYear = 1977;
		TimeElapser te = new TimeElapser();
		int countFound = 0;
		for (Person p : mic.getAll()) {
			if (p.getBirthYear() == birthYear) {
				countFound++;
			}
		}
		LOGGER.info("There are " + countFound + " people born in " + birthYear);
		LOGGER.info("Elapsed time to find people is " + te.end() + " ms");

		// Create index for "birthYear"
		addIndexForBirthYear(mic);
		LOGGER.info("Established index for birthYear");

		te.start();
		Collection<Person> peopleBorn1977 = findEqBirthYear(mic, birthYear);
		LOGGER.info("There are " + peopleBorn1977.size() + " people born in "
				+ birthYear);
		LOGGER.info("Elapsed time to find people is " + te.end() + " ms");

		Assert.assertTrue("There should be at least one person born in "
				+ birthYear, peopleBorn1977.size() > 0);
	}

	/**
	 * Test for creating index for <code>birthYear</code> attribute and greater
	 * values for specified year
	 */
	@Test
	public void testIndexBirthYearBetween100K() {
		LOGGER.info("testIndexBirthYearBetween");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		System.out.println("Elapsed time to add " + count + " people is "
				+ te.end() + " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		LOGGER.info("Elapsed time to create index for birthYear for " + count
				+ " people is " + te.end() + " ms");

		// Try to find records with people whose birthYear is between
		// minBirthYear ... maxBirthYear
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;

		ICriterion<Person, Integer, L> yearBetween = createBetweenBirthYear(
				mic, minBirthYear, maxBirthYear);
		Conjunction<Person, Integer, L> conjunction = mic.conjunction();
		conjunction.add(yearBetween);

		Collection<Person> personTests = mic.find(conjunction);

		LOGGER.info("Found " + personTests.size() + " people for birthYear ["
				+ minBirthYear + ", " + maxBirthYear + "]");
		LOGGER.info("Elapsed time to find people " + te.end() + " ms");

		Assert.assertTrue("There should be found at least one person, but "
				+ personTests.size() + " returned", personTests.size() > 0);
	}

	/**
	 * Test for creating index for <code>birthYear</code> attribute and greater
	 * values for specified year
	 */
	@Test
	public void testIndexBirthYearBetweenWithoutIndex1M() {
		LOGGER.info("testIndexBirthYearBetweenWithoutIndex");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);

		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// Try to find records with people whose birthYear is greater than ...
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;
		Collection<Person> personTests = new ArrayList<Person>();
		for (Person p : mic.getAll()) {
			if (p.getBirthYear() >= minBirthYear
					&& p.getBirthYear() <= maxBirthYear) {
				personTests.add(p);
			}
		}
		LOGGER.info("Found " + personTests.size() + " people for birthYear ["
				+ minBirthYear + ", " + maxBirthYear + "]");
		LOGGER.info("Elapsed time to find people " + te.end() + " ms");

		Assert.assertTrue("There should be found at least one person, but "
				+ personTests.size() + " returned", personTests.size() > 0);
	}

	/**
	 * Test for creating index for <code>birthYear</code> attribute and greater
	 * values for specified year
	 */
	@Test
	public void testIndexBirthYearBetween1_1M() {
		LOGGER.info("testIndexBirthYearBetween1");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);

		LOGGER.info("Elapsed time to create index for birthDate for " + count
				+ " people is " + te.end() + " ms");

		// Try to find records with people whose birthYear is between
		// minBirthYear ... maxBirthYear
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;
		Collection<Person> personTests = new ArrayList<Person>();
		for (int year = minBirthYear; year < maxBirthYear; year++) {
			personTests.addAll(findEqBirthYear(mic, year));
		}
		LOGGER.info("Found " + personTests.size() + " people for birthYear ["
				+ minBirthYear + "-" + maxBirthYear + "]");
		LOGGER.info("Elapsed time to find people " + te.end() + " ms");

		Assert.assertTrue("There should be found at least one person, but "
				+ personTests.size() + " returned", personTests.size() > 0);
	}

	/**
	 * Test to find people between specified birthYears and with specified sex
	 */
	@Test
	public void testIndexBirthYearAndSex1M() {
		LOGGER.info("testIndexBirthYearAndSex");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		System.out.println("Elapsed time to add " + count + " people is "
				+ te.end() + " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		LOGGER.info("Elapsed time to create index for birthYear for " + count
				+ " people is " + te.end() + " ms");

		// and create index for sex of person
		te.start();
		addIndexForSex(mic);
		LOGGER.info("Elapsed time to create index for sex  for " + count
				+ " people is " + te.end() + " ms");

		// Try to find records with people whose birthYear is between
		// minBirthYear ... maxBirthYear and are men
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;
		boolean shouldBeMan = true;

		Junction<Person, Integer, L> lookupRules = mic.conjunction()
				.add(createBetweenBirthYear(mic, minBirthYear, maxBirthYear))
				.add(createEqSex(mic, shouldBeMan));

		Collection<Person> personTests = mic.find(lookupRules);

		LOGGER.info("Found " + personTests.size() + " people for birthYear ["
				+ minBirthYear + ", " + maxBirthYear + "] and" + " isMan="
				+ shouldBeMan);
		LOGGER.info("Elapsed time to find people " + te.end() + " ms");

		Assert.assertTrue("There should be found at least one person, but "
				+ personTests.size() + " returned", personTests.size() > 0);
	}

	/**
	 * Test to find people between specified birthYears and with specified sex
	 */
	@Test
	public void testIndexBirthYearAndSexWithoutIndex_1M() {
		LOGGER.info("testIndexBirthYearAndSexWithoutIndex");
		// Number of created people
		int count = 1000000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// Try to find records with people whose birthYear is between
		// minBirthYear ... maxBirthYear and are men
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;
		boolean shouldBeMan = true;

		Collection<Person> personTests = new ArrayList<Person>();
		for (Person p : mic.getAll()) {
			if (p.getBirthYear() >= minBirthYear
					&& p.getBirthYear() < maxBirthYear
					&& p.isMan() == shouldBeMan) {
				personTests.add(p);
			}
		}
		LOGGER.info("Found " + personTests.size() + " people for birthYear ["
				+ minBirthYear + ", " + maxBirthYear + "] and" + " isMan="
				+ shouldBeMan);
		LOGGER.info("Elapsed time to find people " + te.end() + " ms");

		Assert.assertTrue("There should be found at least one person, but "
				+ personTests.size() + " returned", personTests.size() > 0);
	}

	@Test
	public void testConcurrency() {
		LOGGER.info("testConcurrency");
		// Number of created people
		int count = 1000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		final T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		LOGGER.info("Elapsed time to create index for birthYear  for " + count
				+ " people is " + te.end() + " ms");

		// and create index for "man"
		te.start();
		addIndexForSex(mic);
		LOGGER.info("Elapsed time to create index for sex for " + count
				+ " people is " + te.end() + " ms");

		// Try to find records with people whose birthYear is between
		// minBirthYear ... maxBirthYear and are men
		te.start();
		int minBirthYear = 1960;
		int maxBirthYear = 1980;
		boolean shouldBeMan = true;

		final Junction<Person, Integer, L> lookupRules = mic.conjunction()
				.add(createBetweenBirthYear(mic, minBirthYear, maxBirthYear))
				.add(createEqSex(mic, shouldBeMan));

		// Create threads to get results from multiindex, all are executed
		// concurrently
		int howManyThreads = 100;
		final int howManyPeopleToAdd = 1000;
		Thread[] threads = new Thread[howManyThreads];
		for (int i = 0; i < howManyThreads; i++) {
			final int y = i;
			// Each second adds new people
			if (y % 2 == 0) {
				threads[y] = new Thread(new Runnable() {
					public void run() {
						LOGGER.info("Thread " + y + " (writer) started");
						mic.addAll(Person.generatePeople(howManyPeopleToAdd));
						LOGGER.info("Thread " + y + " (writer) "
								+ howManyPeopleToAdd + " people added");
						LOGGER.info("Thread " + y + " (writer) ended");
					}
				}, "Writer-" + y);
			} else {
				threads[y] = new Thread(new Runnable() {
					public void run() {
						LOGGER.info("Thread " + y + " (reader) started");
						LOGGER.info("Thread " + y + " (reader) ... "
								+ mic.find(lookupRules).size()
								+ " people found");
						LOGGER.info("Thread " + y + " (reader) ended");
					}
				}, "Reader-" + y);
			}
		}

		for (int i = 0; i < howManyThreads; i++) {
			threads[i].start();
		}
		LOGGER.info("Threads finished");

		try {
			Thread.sleep(15 * 1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(true);
	}

	/**
	 * Test for creating index for <code>birthYear</code> attribute
	 */
	@Test
	public void testIndexBirthYearPureIndexes100K() {
		LOGGER.info("testIndexBirthYearPureIndexes");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);

		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		LOGGER.info("Elapsed time to create index for birthYear  for " + count
				+ " people is " + te.end() + " ms");

		int birthYear = 1977;
		Collection<Person> peopleBorn1977 = mic.find(createEqBirthYear(mic,
				birthYear));
		LOGGER.info("There are " + peopleBorn1977.size() + " people born in "
				+ birthYear);

		Assert.assertTrue("There should be " + count
				+ " people in MultiIndexContainerEnum, but only " + mic.size()
				+ " to be present", count == mic.size());
	}

	/**
	 * Test for finding records with null indexed value
	 */
	@Test
	public void testIndexWithNullValue100() {
		LOGGER.info("testIndexWithNullValue");
		// Number of created people
		int count = 100;

		Collection<Person> people = Person.generatePeople(count);

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);

		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		addIndexForSurname(mic);
		te.end();
		LOGGER.info("Elapsed time to create indexes for birthYear and surname for "
				+ count + " people is " + te.end() + " ms");

		int birthYear = 1950;

		Junction<Person, Integer, L> lookupRules = mic.conjunction()
				.add(createEqBirthYear(mic, birthYear))
				.add(createIsNullSurname(mic));

		Collection<Person> born1950 = mic.find(lookupRules);
		LOGGER.info("There are " + born1950.size() + " people born in "
				+ birthYear + " and named null");

		Assert.assertTrue(
				"There should be found at least 1 people in MultiIndexContainerEnum, but only "
						+ born1950.size() + " found", born1950.size() > 0);
	}

	/**
	 * Test for finding records with null indexed value
	 * @throws IOException 
	 */
	@Test
	public void testIndexWithNullValueForBirthDate_100() throws IOException {
		LOGGER.info("testIndexWithNullValueForBirthDate");
		// Number of created people
		int count = 100000;

		Collection<Person> people = Person.generatePeople(count);
		// Lets half of people have birthYear unknown
		for (Person p : people) {
			if (p.getBirthYear() != null && p.getBirthYear() < 1975) {
				p.setBirthYear(null);
			}
		}

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirtYearAndSurname(mic);
		LOGGER.info("Elapsed time to create index ["
				+ PersonTest_Indexes.BIRTH_YEAR + ", "
				+ PersonTest_Indexes.SURNAME + "]  for " + count
				+ " people is " + te.end() + " ms");

		int birthYear = 1980;

		Junction<Person, Integer, L> lookupRules = mic.conjunction()
				.add(createLTBirthYear(mic, birthYear))
				.add(createIsNullSurname(mic));

		Collection<Person> bornBefore1980 = mic.find(lookupRules);
		LOGGER.info("There are " + bornBefore1980.size()
				+ " people born before " + birthYear + " with surname null");

		Assert.assertTrue(
				"There should be found at least 1 people in MultiIndexContainerEnum, but only "
						+ bornBefore1980.size() + " found",
				bornBefore1980.size() > 0);
	}

	/**
	 * Test for finding records with null indexed value
	 */
	@Test
	public void testIndexNotNull() {
		LOGGER.info("testIndexNotNull");
		// Number of created people
		int count = 100;

		Collection<Person> people = Person.generatePeople(count);
		// Lets half of people have birthYear unknown
		for (Person p : people) {
			if (p.getBirthYear() != null && p.getBirthYear() < 1975) {
				p.setBirthYear(null);
			}
		}

		// Lets add people
		// attribute
		TimeElapser te = new TimeElapser();
		T mic = createMultiIndexContainer(people);
		LOGGER.info("Elapsed time to add " + count + " people is " + te.end()
				+ " ms");

		// and create index for "birthYear"
		te.start();
		addIndexForBirthYear(mic);
		addIndexForSurname(mic);
		LOGGER.info("Elapsed time to create indexes birthYear and surname  for "
				+ count + " people is " + te.end() + " ms");

		int birthYear = 1980;

		Junction<Person, Integer, L> lookupRules = mic.conjunction()
				.add(createLTBirthYear(mic, birthYear))
				.add(createIsNullSurname(mic));

		Collection<Person> knownBirthYear = mic.find(lookupRules);
		LOGGER.info("There are " + knownBirthYear.size()
				+ " people with knowh birth year ");

		Assert.assertTrue(
				"There should be found at least 1 people in MultiIndexContainerEnum, but only "
						+ knownBirthYear.size() + " found",
				knownBirthYear.size() > 0);
	}
	
	/**
	 * Testing for find underweight persons. They have BMI < 16
	 */
	@Test
	public void testFindByBMIIndex() {
		// Generate 10000 persons
		Collection<Person> people = Person.generatePeople(10000);
		
		T mic = createMultiIndexContainer(people);
		addIndexForBMI(mic);		
		
		Collection<Person> underweightPersons = mic.find(createLTBMI(mic, 16d));
		LOGGER.info("There are " + underweightPersons.size() + " underweight people ");
		
		Assert.assertTrue(
				"There should be found at least 1 underweigt person found, but only " +underweightPersons.size() + " found",
				underweightPersons.size() > 0);
	}
}
