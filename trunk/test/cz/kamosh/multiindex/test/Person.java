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


import java.util.Arrays;
import java.util.Collection;

import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Data object used for testing MultiIndexContainer
 * 
 */
public class Person implements IMultiIndexed<Integer> {
    static int counter;
    int id;
    private Integer birthYear;
    private String name;
    private String surname;
    private boolean man;

    public Person() {
    }

    Person(Integer birthYear, String name, String surname, boolean isMan) {
        this.id = counter++;
        this.birthYear = birthYear;
        this.name = name;
        this.surname = surname;
        this.man = isMan;
    }

    public Integer getMultiIndexPk() {
        return id;
    }


    static Person createPerson(int id) {
        return new Person(
                // birthYear
                1950 + (id % 50),
                // name, there are 30 names
                "name_" + (id % 30),
                // each 50th surname is unknown
                // surname, there are 20 surnames
                (id % 50 == 0 ? null : "surname_" + (id % 20))
                ,
                // Sex, man/woman
                id % 2 == 0
        );
    }

    static Collection<Person> generatePeople(int count) {
        Person[] personTests = new Person[count];
        for (int i = 0; i < count; i++) {
            personTests[i] = Person.createPerson(i);
        }
        return Arrays.asList(personTests);
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
    	this.birthYear = birthYear;
    }
        
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public boolean isMan() {
        return man;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person personTest = (Person) o;

        if (id != personTest.id) return false;

        return true;
    }

    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "id = " + id + "\t" +
                "birthYear = " + birthYear + "\t" +
                " name = " + name + "\t" +
                " surname = " + surname;
    }
}
