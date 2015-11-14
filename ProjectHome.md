# Serves as container for records which data could be indexed by multiple indexes. #
All data are stored in memory and due to created indexes all searching among these data is fast.
Container provides mechanisms to combine criterions to find data with required properties.
Multiindexcontainer requires Java 1.6 or higher as it requires NavigableMap.

"This product includes software developed by the Walt Disney Internet Group." See acknowledgement in class cz.kamosh.multiindex.utils.NullKeyMap.java


## Snippet example ##
JavaBean data class
```
public class Person implements IMultiIndexed<Integer> { 
    private int id; // Unique id
    private Integer birthYear;
    private String name;
    private String surname;
    private boolean man;

  ...
}
```

Code using JavaBean records
```
public static void main(String[] args) {
        Collection<Person> allPersons = findAllPersons(); // get many Person records 

        // Create multiindex filled by data
        IMultiIndexContainerEnum<Person, Integer> mic = new MultiIndexContainerEnum<Person, Integer>(allPersons);

        // Add index for birth year, man/woman and surname properties
        mic.addIndex(Person_Indexes.BirthYear, Person_Indexes.Man, Person_Indexes.Surname);

        // Find women born in 1970 with surname 'Surname81'
        Collection<Person> res = mic.find(mic.conjunction()
                        .add(mic.eq(Person_Indexes.BirthYear, 1970))
                        .add(mic.eq(Person_Indexes.Man, false))
                        .add(mic.eq(Person_Indexes.Surname, "Smith")));

        // res variable contains all seeked data
        System.out.println("Found " + res.size()
                        + " women born in 1970 with surname 'Smith");
}
```

For more see http://code.google.com/p/multiindexcontainer/wiki/MainPage