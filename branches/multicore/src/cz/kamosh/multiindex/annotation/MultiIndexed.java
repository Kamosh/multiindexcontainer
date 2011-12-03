package cz.kamosh.multiindex.annotation;

import cz.kamosh.multiindex.interf.IMultiIndexContainerEnum;
import cz.kamosh.multiindex.interf.IMultiIndexed;
import cz.kamosh.multiindex.interf.Indexable;

/**
 * Annotation to mark getters of data classes for which 
 * should be created enum constants.
 * Each of constant will implement interface {@link Indexable}
 * so that it could be used in {@link IMultiIndexContainerEnum}
 * 
 * Annotated getter must be non parametrized getter of data class. 
 * Data class hosting annotated attributes must either
 * <br>implement interface {@link IMultiIndexed}
 * or
 * <br>has one getter method annotated with // TODO
 * 
 */
@java.lang.annotation.Target(value = {java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})
@java.lang.annotation.Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface MultiIndexed {
	
}