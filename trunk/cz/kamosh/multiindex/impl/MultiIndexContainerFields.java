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
package cz.kamosh.multiindex.impl;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import cz.kamosh.multiindex.criterion.Expression.LOOKUP_OPERATOR;
import cz.kamosh.multiindex.interf.IMultiIndexContainerFields;
import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Multiindex serves as container for records that could be indexed by
 * specified attribute names.
 * <p/>
 * Indexable are all public fields which getters , index must be of the same name as field
 * <p/>
 * 
 * 
 * @see <a href="http://en.wikipedia.org/wiki/JavaBean"/>JavaBean
 *      specification</a>
 */
public class MultiIndexContainerFields<E extends IMultiIndexed<K>, K extends Object>
		extends MultiIndexContainer<E, K, String> implements
		IMultiIndexContainerFields<E, K> {

	private static final Logger log = Logger
			.getLogger(MultiIndexContainerFields.class.getName());

	/**
	 * Class thats instances will occur in {@link #data}. It is used to get
	 * Field instance
	 */
	private Class<E> clz;

	/**
	 * Map to hold pairs <code>attributeValue</code>...
	 * <code>Getter method</code> instance
	 */
	private Map<String, Method> getterMethods;

	/**
	 * Constructor for MultiIndexContainer, empty after creation
	 * 
	 * @param clz
	 *            What class instances is record of this container?
	 */
	public MultiIndexContainerFields(Class<E> clz) {
		this(clz, Collections.<E> emptySet());
	}

	/**
	 * @param clz
	 *            What class instances will occur in primary data?
	 * @param records
	 *            Instances of data
	 */
	public MultiIndexContainerFields(Class<E> clz, Collection<E> records) {
		super();
		this.clz = clz;
		// Initialize map which will hold all getters for indexes
		// (attribute_name -> method_of_getter)
		this.getterMethods = new HashMap<String, Method>();
		addAll(records);
	}

	// ------ Implementation of MultiIndexContainer ---------- /
	@Override
	FieldDataGetter getDataGetter(String index) {
		// Try to get method for attribute index
		Method getterMethod = getMethod(index);
		return new FieldDataGetter(getterMethod);
	}

	class FieldDataGetter extends DataGetter {
		Method getterMethod;

		FieldDataGetter(Method m) {
			this.getterMethod = m;
		}

		Object getData(E record) throws UnsupportedOperationException {
			try {
				return getterMethod.invoke(record);
			} catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		};
	}

	// Methods to create rules to find data
	@Override
	public ExpressionFields<E, K> eq(String indexName, Object value) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.EQUAL, indexName,
				new Object[] { value });
	}

	@Override
	public ExpressionFields<E, K> gt(String indexName, Object value) {
		return gt(indexName, value, false);
	}

	@Override
	public ExpressionFields<E, K> gt(String indexName, Object value,
			boolean inclusive) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.GREATER, indexName,
				value, inclusive);
	}

	@Override
	public ExpressionFields<E, K> lt(String indexName, Object value) {
		return lt(indexName, value, false);
	}

	@Override
	public ExpressionFields<E, K> lt(String indexName, Object value,
			boolean inclusive) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.LESS, indexName,
				value, inclusive);
	}

	@Override
	public ExpressionFields<E, K> between(String indexName, Object valueFrom,
			Object valueTo) {
		return between(indexName, valueFrom, false, valueTo, false);
	}

	@Override
	public ExpressionFields<E, K> between(String indexName, Object valueFrom,
			boolean inclusiveFrom, Object valueTo, boolean inclusiveTo) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.BETWEEN, indexName,
				valueFrom, inclusiveFrom, valueTo, inclusiveTo);
	}

	@Override
	public ExpressionFields<E, K> isNull(String indexName) {
		return eq(indexName, null);
	}

	@Override
	public ExpressionFields<E, K> isNotNull(String indexName) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.IS_NOT_NULL,
				indexName, null, false);
	}

	@Override
	public ExpressionFields<E, K> in(String indexName, Object[] values) {
		return new ExpressionFields<E, K>(LOOKUP_OPERATOR.IN, indexName, values);
	}

	// ----------- Helper methods -----------------/

	/**
	 * Method to find getter of specified attribute
	 * <p/>
	 * Introspector is used to find proper method as only JavaBeans could be
	 * inserted as records into MultiIndexContainer
	 * 
	 * @param attribute
	 *            Identifier of attribute name
	 * @return Getter method for attribute to get proper value from object
	 *         instance
	 * @throws UnsupportedOperationException
	 *             In case there cannot be get Method instance from
	 *             attributeName
	 */
	private Method getMethod(String attribute) {
		Method method = getterMethods.get(attribute);
		if (method == null) {
			try {
				BeanInfo beanInfo = Introspector.getBeanInfo(clz);
				PropertyDescriptor[] propertyDescriptors = beanInfo
						.getPropertyDescriptors();
				for (PropertyDescriptor pd : propertyDescriptors) {
					if (attribute.equals(pd.getName())) {
						method = pd.getReadMethod();
						// Remember getter method
						getterMethods.put(attribute, method);
						return method;
					}
				}
			} catch (IntrospectionException e) {
				throw new UnsupportedOperationException(
						"Cannot reach getter for attribute '" + attribute + "'",
						e);
			}
			throw new UnsupportedOperationException(
					"Cannot reach getter for attribute '" + attribute + "'");
		}
		return method;
	}

}