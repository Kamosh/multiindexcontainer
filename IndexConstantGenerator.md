# MultiIndex index constant generator #
Is based on Java's Annotation processor http://www.javabeat.net/articles/14-java-60-features-part-2-pluggable-annotation-proce-1.html

Since Java 1.6 Java annotation processor is part of Java compiler _javac_.

MultiIndexContainer makes use of it to generate Classes with indexes enums deduced from getters which values should be indexed.

## Example of MultiIndex constant generator result ##
Original class:
```
public class Person implements IMultiIndexed<Integer> {
  ...
  @MultiIndexed
  public Integer getBirthYear() {    	
      return birthYear;
  }   
}
```

Generated class of index constants:
```
public enum Person_Indexes implements Indexable<Person> {
    BirthYear {
        public Object getIndexedValue(Person record) {
              return record.getBirthYear();
        }
    },
}
```

Usage of index constants should lead to type safe usage of MultiIndexContainer.

NOTE: Even if MultiIndexContainer supports also identifying of indexed data by property name ("birthDate" for _getBirthDate_ indexed getter), usage of generated index constants should be the prefered way.

# How to setup index constant generator within Eclipse #

![http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_1.png](http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_1.png)

![http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_2.png](http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_2.png)

### Custom settings of annotation processor ###
MultiIndex index constant generator supports optional settings of processor:
  * classSuffix=VALUE\_SUFFIX, where VALUE\_SUFFIX is value which will be annexed to original file name. If not specified **"Indexes"** is used as default
  * trimGet=[true|false], decides whether **is|get** prefixes in resulting index constants are trimmed or not

For example this setting:

![http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_3.png](http://multiindexcontainer.googlecode.com/svn/wiki/img/eclipse_annotation_processing_3.png)

is resulting into generated file
```
public enum Person_SpecialSuffix implements Indexable<Person> {

	GetBirthYear {
        public Object getIndexedValue(Person record) {
              return record.getBirthYear();
        }
    },   
}
```

# MultiIndex index constant generator using _javac_ #
You will need _javac_ version 1.6, so check it on your command prompt:

_>javac -version_

You should get something like:

_javac 1.6.0\_14_


Because annotation processor makes use of service loader (see http://java.sun.com/developer/technicalArticles/javase/extensible/index.html) the only syntax needed to generate index constants for your indexable getters (in this example in Person.java class) is:

_>javac -classpath multiindexcontainer-1.1.1.jar Person.java_