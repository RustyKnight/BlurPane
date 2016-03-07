package org.kaizen.ui.blurred;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This a reflections based series of utilities
 * @author swhitehead
 */
public class ReflectionToolbox {

	public static Field findField(Class parent, String name) {

		Field field = null;
		try {
			field = parent.getDeclaredField(name);
		} catch (NoSuchFieldException noSuchFieldException) {
			try {
				field = parent.getField(name);
			} catch (NoSuchFieldException nsf) {
				if (parent.getSuperclass() != null) {
					field = findField(parent.getSuperclass(), name);
				}
			}
		}

		return field;
	}

	public static Object getFieldValue(Object obj, String name) {
		Object value = null;
		Field field = findField(obj.getClass(), name);
		field.setAccessible(true);
		try {
			value = field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			ex.printStackTrace();
		}
		return value;
	}

	private Object parent;
	private Class parentClass;
	private String methodName;
	private List<Parameter> lstParameters;

	protected ReflectionToolbox(Object parent, Class parentClass, String methodName) {
		this.parent = parent;
		this.parentClass = parentClass;
		this.methodName = methodName;

		lstParameters = new ArrayList<Parameter>(5);
	}

	protected ReflectionToolbox(Object parent, String methodName) {
		this(parent, parent.getClass(), methodName);
	}

	protected ReflectionToolbox(Class clazz, String methodName) {
		this(null, clazz, methodName);
	}

	public static ReflectionToolbox invoke(Object parent, String methodName) {
		return new ReflectionToolbox(parent, methodName);
	}

	public static ReflectionToolbox invoke(Class parent, String methodName) {
		return new ReflectionToolbox(parent, methodName);
	}

	public static ReflectionToolbox invoke(Object parent, Class clazz, String methodName) {
		return new ReflectionToolbox(parent, clazz, methodName);
	}

	public ReflectionToolbox setParent(Object parent) {
		this.parent = parent;
		if (parent != null) {
			setParentClass(parentClass.getClass());
		}

		return this;
	}

	public ReflectionToolbox setParentClass(Class parent) {
		this.parentClass = parent;
		return this;
	}

	public ReflectionToolbox setMethodName(String method) {
		this.methodName = method;
		return this;
	}

	public <T> ReflectionToolbox with(Class<T> type, T value) {
		with(new Parameter(value, type));
		return this;
	}

	public ReflectionToolbox with(Class[] types, Object[] parameters) {
		if (types == null || parameters == null) {
		} else if (types.length != parameters.length) {
		} else {
			for (int index = 0; index < types.length; index++) {
				with(types[index], parameters[index]);
			}
		}

		return this;
	}

	public ReflectionToolbox with(Parameter parameter) {
		lstParameters.add(parameter);
		return this;
	}

	public Object getParent() {
		return parent;
	}

	public Class getParentClass() {
		return parentClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class[] getParameterTypes() {
		List<Class> lstTypes = new ArrayList<Class>(lstParameters.size());
		for (Parameter parameter : lstParameters) {
			lstTypes.add(parameter.getType());
		}
		return lstTypes.toArray(new Class[lstTypes.size()]);
	}

	public Object[] getParameterValues() {
		List<Object> lstTypes = new ArrayList<Object>(lstParameters.size());
		for (Parameter parameter : lstParameters) {
			lstTypes.add(parameter.getValue());
		}
		return lstTypes.toArray(new Object[lstTypes.size()]);
	}

	public Object execute() {
		Object result = null;

		Class type = getParentClass();
		String methodName = getMethodName();
		Class[] lstTypes = getParameterTypes();
		Object[] lstValues = getParameterValues();
		Object parent = getParent();

		try {
			Method method = findMethod(type, methodName, lstTypes);
			if (method == null) {
				throw new NoSuchMethodException(getMethodDescription(type, methodName, lstTypes));
			}
			method.setAccessible(true);

			result = method.invoke(parent, lstValues);
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("parent = ").append(parent).append("\n");
			sb.append("type = ").append(type).append("\n");
			sb.append("methodName = ").append(methodName).append("\n");
			for (int index = 0; index < lstTypes.length; index++) {
				sb.append("[").append(index).append("] ").append(lstTypes[index].getName()).append(lstValues[index]).append("\n");
			}
			System.err.println("Called by\n" + sb.toString());
			throw new InvocationException("Failed to invoke " + methodName, ex);
		}

		return result;
	}

	/**
	 * This method basically walks the class hierarchy looking for a matching
	 * method.
	 *
	 * The issue is getXMethod only returns a list of the methods for the current
	 * class and not the inherited methods. This method basically over comes that
	 * limitation.
	 *
	 * If no method can be found matching the criteria, then this method returns a
	 * null value.
	 *
	 * This makes this method not only very powerful, but also very dangerous, as
	 * it has the power of finding ANY declared method within the hierarchy,
	 * public, protected or private.
	 *
	 * @param parent
	 * @param name
	 * @param lstTypes
	 * @return
	 */
	public static Method findMethod(Class parent, String name, Class[] lstTypes) {
		Method method = null;
		try {
			method = parent.getDeclaredMethod(name, lstTypes);
		} catch (NoSuchMethodException noSuchMethodException) {
			try {
				method = parent.getMethod(name, lstTypes);
			} catch (NoSuchMethodException nsm) {
				if (parent.getSuperclass() != null) {
					method = findMethod(parent.getSuperclass(), name, lstTypes);
				}
			}
		}

		return method;
	}

	/**
	 * Builds up a description of the method call in the format of
	 * [package.class.method([{package.class}{, package.class}{...}])
	 *
	 * This is typically used to throw a NoMethodFound exception.
	 *
	 * @param clazz
	 * @param name
	 * @param lstTypes
	 * @return
	 */
	public static String getMethodDescription(Class clazz, String name, Class[] lstTypes) {
		StringBuilder sb = new StringBuilder();
		sb.append(clazz.getName()).append(".").append(name).append("(");

		for (Class type : lstTypes) {
			sb.append(type.getName()).append(", ");
		}

		if (lstTypes.length > 0) {
			sb.delete(sb.length() - 2, sb.length());
		}

		sb.append(")");
		return sb.toString();
	}

	public class Parameter<T> {

		private T value;
		private Class<T> clazz;

		public Parameter(T value, Class<T> clazz) {
			this.value = value;
			this.clazz = clazz;
		}

		public T getValue() {
			return value;
		}

		public Class<T> getType() {
			return clazz;
		}
	}

	public class InvocationException extends Error {

		public InvocationException(String message) {
			super(message);
		}

		public InvocationException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static boolean hasMethod(Object instance, String methodName) {
		return hasMethod(instance.getClass(), methodName);
	}

	public static boolean hasMethod(Object instance, String methodName, Class[] types) {
		return hasMethod(instance.getClass(), methodName, types);
	}

	public static boolean hasMethod(Class clazz, String methodName) {
		return hasMethod(clazz, methodName, new Class[0]);
	}

	public static boolean hasMethod(Class clazz, String methodName, Class[] types) {
		return findMethod(clazz, methodName, types) != null;
	}

}
