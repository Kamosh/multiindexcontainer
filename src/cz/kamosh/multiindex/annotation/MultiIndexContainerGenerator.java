package cz.kamosh.multiindex.annotation;

import static javax.lang.model.SourceVersion.RELEASE_6;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementKindVisitor6;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import cz.kamosh.multiindex.impl.MultiIndexContainerFields;
import cz.kamosh.multiindex.interf.IMultiIndexed;

/**
 * Generator to generate indexable objects that might be used as records in
 * {@link MultiIndexContainerFields}.
 * <p>
 * Possible optional options:
 * <ul>
 * <li>{@link MultiIndexContainerGenerator#GENERATED_CLASS_SUFFIX} ... what
 * suffix for generated classes should be used. Default is "_Indexes".
 * <li>{@link MultiIndexContainerGenerator#TRIM_GET} ... "true" or "false" -
 * decides whether "get" or "is" prefix should be removed from resulting
 * constant names. Default is "true".
 * </ul>
 * Example (key=value):
 * <pre>
 * classSuffix=_MyIndexes
 * trimGet=false
 * </pre>
 * 
 * <p>
 * Usage with compiler
 * <code>javac -AclassSuffix=_MyIndexes -AtrimGet=false -processorpath=multiindexcontainer.jar  </code> 
 * 
 * <p>
 * <UL>
 * <LI>Each record class must comply
 * {@link cz.logis.app.data.multiindex.MultiIndexed} interface.
 * <LI>Each record class must have be annotated with
 * {@link cz.kamosh.multiindex.generator.logis.app.data.multiindex.MultiIndexed}
 * <LI>Indexable attributes are marked on their getter method also with
 * annotation
 * {@link cz.kamosh.multiindex.generator.logis.app.data.multiindex.MultiIndexed}
 * <LI>Getter method should not be have any parameter
 * <LI>Do not forget to override {@link Object#equals(Object)} and
 * {@link Object#hashCode()} on each class used as record!!!
 * </UL>
 * 
 */
@SupportedAnnotationTypes({ "cz.kamosh.multiindex.annotation.MultiIndexed" })
@SupportedSourceVersion(RELEASE_6)
@SupportedOptions({ MultiIndexContainerGenerator.GENERATED_CLASS_SUFFIX,
		MultiIndexContainerGenerator.TRIM_GET })
public class MultiIndexContainerGenerator extends AbstractProcessor {

	/**
	 * Constant for setting what suffix of generated files should be used
	 */
	public final static String GENERATED_CLASS_SUFFIX = "classSuffix";
	public final static String TRIM_GET = "trimGet";

	private static final String NEW_LINE = "\n";
	private static final String TAB = "    ";
	// The most common getter prefixes
    private static final String[] prefixes = new String[]{"get", "is"};


	// TODO What about to Change to TypeElement -> ExecutableElement ?
	// Enclosing class element -> Getters annotated with @MultiIndexed
	private Map<Element, Set<ExecutableElement>> indexableElements = new HashMap<Element, Set<ExecutableElement>>();

	private ProcessingEnvironment env;

	// What class suffix should be used?
	private String classSuffix = "_Indexes";
	// Should be get/is trimmed out?
	private boolean trimGet = true;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		env = processingEnv;

		// Parse setting of default class suffix and check that it is acceptable
		// for new file file if added to original class
		String userSuffix = processingEnv.getOptions().get(
				GENERATED_CLASS_SUFFIX);
		if ("".equals(userSuffix)) {
			logWarning("You cannot use empty suffix for generated classes");
		} else {
			boolean validSuffix = (userSuffix != null && !"".equals(userSuffix));
			for (int i = 0; validSuffix && i < userSuffix.length(); i++) {
				if (!Character.isJavaIdentifierPart(userSuffix.charAt(i))) {
					logWarning("You cannot use character '"
							+ userSuffix.charAt(i) + "' in suffix '"
							+ userSuffix
							+ "' of generated classes. Used default suffix.");
					validSuffix = false;
					break;
				}
			}
			classSuffix = (validSuffix ? userSuffix : classSuffix);
		}
		log("MultiIndexContainerGenerator uses suffix '" + classSuffix + "'");

		// Parse setting of trimGet settings
		String userTrimGet = processingEnv.getOptions().get(TRIM_GET);
		if (userTrimGet != null) {
			if (userTrimGet.equalsIgnoreCase("false")) {
				trimGet = false;
			} else if (userTrimGet.equalsIgnoreCase("true")) {
				trimGet = true;
			} else {
				logWarning("Accepted values for parameter '" + TRIM_GET
						+ "' are 'true' or 'false', specified parameter: '"
						+ userTrimGet + "'");
			}
		}
		log("MultiIndexContainerGenerator will " + (trimGet ? "" : "not ")
				+ "trim get/is prefix in generated constant names");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {

		for (Element e : roundEnv.getElementsAnnotatedWith(MultiIndexed.class)) {
			addIndexedElement(e);
		}

		log("Indexable classes:");
		StringBuilder sb = new StringBuilder();
		for (Entry<Element, Set<ExecutableElement>> x : indexableElements
				.entrySet()) {
			sb.append(x);
			sb.append("; ");
			createConstantClass(x);
		}
		log("Classes: " + sb.toString());
		return true;
	}

	private void createConstantClass(Entry<Element, Set<ExecutableElement>> data) {
		// Class java name is formed as
		Element indexedClass = data.getKey();
		Set<ExecutableElement> indexedAttributes = data.getValue();
		String className = indexedClass + classSuffix;
		try {
			JavaFileObject createSourceFile = env.getFiler().createSourceFile(
					className);
			Writer openWriter = createSourceFile.openWriter();
			openWriter.append(getPackageAndImports(indexedClass,
					Collections.EMPTY_LIST));
			openWriter.append(getCommentForClass(indexedClass));
			openWriter.append(getClassHeader(indexedClass));

			openWriter.append(getIndexableConstants(indexedClass,
					indexedAttributes));
			openWriter.append("}");
			openWriter.close();
		} catch (IOException e) {
			logError(e.getMessage());
		}
	}

	private void addIndexedElement(Element e) {
		ExecutableElement method = checkIndexedElement(e);
		if (method == null) {
			return;
		}
		// Add method to indexableElements along with its enclosing class
		Element enclosingElement = checkClassElement(e.getEnclosingElement());

		if (enclosingElement != null) {
			// Try to find whether element records has been already used
			Set<ExecutableElement> elements = indexableElements
					.get(enclosingElement);
			if (elements == null) {
				elements = new HashSet<ExecutableElement>();
				indexableElements.put(enclosingElement, elements);
			}
			elements.add(method);
		}
	}

	/**
	 * Checking method for possible getter which should be transformed to
	 * constant enum in generated class Mandatory validations:
	 * <ul>
	 * <li>Check that passed element is getter method
	 * <li>Check that method does not return void and is public
	 * <li>Check that method does not expect parameter
	 * </ul>
	 * 
	 * Warning validations:
	 * <ul>
	 * <li>Create warning if method is not prefixed get/is
	 * <li>Create warning if method modifier is static
	 * </ul>
	 * 
	 * @param e
	 *            Possible getter method element used to be converted to
	 *            constant of index
	 * @return Element cast converted to executable element if it passed all
	 *         validations
	 */
	private ExecutableElement checkIndexedElement(Element e) {
		// Enclosing element used in each message
		final String enclosingElement = "(" + e.getEnclosingElement() + ") ";

		// Check that passed element is getter method
		if (e.getKind() != ElementKind.METHOD) {
			logError(enclosingElement
					+ "Annotation @MultiIndexed must be specified only on non parametrized public getter/is methods not returning void. It was found on '"
					+ e + "' which is " + e.getKind());
			return null;
		}
		ExecutableElement method = (ExecutableElement) e;
		// Check that method does not return void and is public
		if (method.getReturnType().getKind() == TypeKind.VOID
				|| !method.getModifiers().contains(Modifier.PUBLIC)) {
			logError(enclosingElement
					+ "Annotation @MultiIndexed must be specified only on non parametrized public getter/is methods not returning void. It was found on '"
					+ method + "'");
			return null;
		}
		// Check that method does not expect parameter
		if (!method.getParameters().isEmpty()) {
			logError(enclosingElement
					+ "Annotation @MultiIndexed must be specified only on non parametrized public getter/is methods not returning void. It was found on '"
					+ method + "'");
		}

		// Create warning if method is not prefixed get/is
		if (!method.getSimpleName().toString().startsWith("is")
				&& !method.getSimpleName().toString().startsWith("get")) {
			logWarning(enclosingElement
					+ "It is weird but method '"
					+ method
					+ "' annotated @MultiIndexed seems not to be JavaBean getter");
		}

		// Create warning if method modifier is static
		if (method.getModifiers().contains(Modifier.STATIC)) {
			logWarning(enclosingElement + "It is weird but method '" + method
					+ "' annotated @MultiIndexed is static");
		}

		return method;
	}

	/**
	 * Cache for checked classes in method {@link #checkClassElement(Element)}
	 */
	private Map<Element, Element> checkedClasses = new HashMap<Element, Element>();

	/**
	 * Checks whether enclosing class could be used to host indexable attributes
	 * Warning message is created if enclosing class cannot is not valid
	 * 
	 * Class must implement interface {@link IMultiIndexed}
	 * 
	 * @param enclosingClass
	 *            Element of class
	 * @return The same element of class if it is valid, null otherwise.
	 */
	private Element checkClassElement(Element enclosingClass) {
		// Use cached checked classes to faster method
		if (checkedClasses.containsKey(enclosingClass)) {
			return checkedClasses.get(enclosingClass);
		}
		Element res = null;
		if (ElementKind.CLASS == enclosingClass.getKind()) {
			// Check whether class implements IMultiIndexed interface
			boolean implIMultiIndexed = enclosingClass.accept(
					new InterfaceIMultiIndexedChecker(), null);
			if (!implIMultiIndexed) {
				logError("Enclosing class '"
						+ enclosingClass
						+ "' should implement IMultiIndexed interface if constants for its MultiIndexed getters should be generated");
			} else {
				res = enclosingClass;
			}
		}
		checkedClasses.put(enclosingClass, res);
		return res;
	}

	/**
	 * Visitor checking whether class element implements interface IMultiIndexed
	 */
	private class InterfaceIMultiIndexedChecker extends
			ElementKindVisitor6<Boolean, Void> {
		@Override
		public Boolean visitTypeAsClass(TypeElement e, Void p) {
			for (TypeMirror tp : e.getInterfaces()) {
				if (TypeKind.DECLARED == tp.getKind()) {
					DeclaredType dt = (DeclaredType) tp;
					Element asElement = dt.asElement();
					if (!IMultiIndexed.class.getName().equals(
							asElement.getSimpleName())) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private void log(String message) {
		processingEnv.getMessager().printMessage(Kind.NOTE, message);
	}

	private void logError(String message) {
		processingEnv.getMessager().printMessage(Kind.ERROR, message);
	}

	private void logWarning(String message) {
		processingEnv.getMessager().printMessage(Kind.WARNING, message);
	}

	private String getClassHeader(Element classElement) {
		StringBuilder sb = new StringBuilder();
		sb.append("public enum " + classElement.getSimpleName() + classSuffix
				+ " implements Indexable<" + classElement + "> {");
		sb.append(NEW_LINE);
		sb.append(NEW_LINE);
		return sb.toString();
	}

	private String getCommentForClass(Element classElement) {
		StringBuilder sb = new StringBuilder();
		sb.append("/**" + NEW_LINE);
		sb.append(" * Do not edit this file. It is generated using generator"
				+ NEW_LINE);
		sb.append(" * {@link " + MultiIndexContainerGenerator.class.getName()
				+ "}" + NEW_LINE);
		sb.append(" * based on annotated file {@link "
				+ classElement + "}" + NEW_LINE);
		sb.append(" */");
		sb.append(NEW_LINE);
		return sb.toString();
	}

	private String getIndexableConstants(Element indexedClass,
			Set<ExecutableElement> indexedAttributes) {
		StringBuilder sb = new StringBuilder();
		for (ExecutableElement indexedAttribute : indexedAttributes) {
			sb.append(TAB + formIndexConstantName(indexedAttribute) + " {" + NEW_LINE);
			// TODO proper return type

			sb.append(TAB + TAB + "public Object getIndexedValue("
					+ indexedClass + " record) {" + NEW_LINE);
			sb.append(TAB + TAB + TAB + "  return record."
					+ indexedAttribute.getSimpleName() + "();" + NEW_LINE);
			sb.append(TAB + TAB + "}" + NEW_LINE);
			sb.append(TAB + "}," +NEW_LINE);
		}
		return sb.toString();
	}
	
	/**
	 * Method to form name of constant. Setting {@link #trimGet} is
	 * considered whether to trim first get/is or not.
	 * Regardless trimming setting resulting constant starts with upper case. 
	 * 
	 * @param indexedAttribute Method element
	 * @return Name of constant
	 */
	private String formIndexConstantName(ExecutableElement indexedAttribute) {
		String methodName = indexedAttribute.getSimpleName().toString();
		if(trimGet) {
			for (String prefix : prefixes) {
	            if (methodName.startsWith(prefix)) {
	                methodName = methodName.substring(prefix.length());	                
	                break;
	            }
	        }
		} 
		// Convert first letter to uppercase
		methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);	
		return methodName;
	}
	
	/**
	 * Generates string for package and imports
	 * @param indexedClass Original data class
	 * @param allUsedClasses All classes for which imports should be created
	 * @return Generated string for package and imports
	 */
	private String getPackageAndImports(Element indexedClass,
			Collection<Class> allUsedClasses) {
		StringBuffer sb = new StringBuffer();

		sb.append("package " + getPackageName(indexedClass) + ";");
		sb.append(NEW_LINE);
		sb.append("import cz.kamosh.multiindex.interf.Indexable;" + NEW_LINE);
		// Now import all used classes
		for (Class clz : allUsedClasses) {
			sb.append("import " + clz.getName() + ";" + NEW_LINE);
		}
		sb.append(NEW_LINE);
		return sb.toString();
	}

	private String getPackageName(Element indexedClass) {
		String className = indexedClass.toString();
		return className.substring(0, className.lastIndexOf('.'));
	}
}
