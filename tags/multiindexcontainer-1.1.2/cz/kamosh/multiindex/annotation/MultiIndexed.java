/*
 *  Main authors:
 *     Fekete Kamosh  <fekete.kamosh@gmail.com>
 *
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