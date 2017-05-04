package com.flowerplatform.rapp_mini_server;

/**
 * @author Cristian Spiescu
 */
public class StringConversionUtil {
	
	public static final String NULL = "<<null>>";
	
	public static Object fromString(Class<?> expectedParameterType, String string) {
		if (String.class.equals(expectedParameterType)) {
			return string;
		} else if (int.class.equals(expectedParameterType) || Integer.class.equals(expectedParameterType)) {
			return Integer.parseInt(string);
		} else if (boolean.class.equals(expectedParameterType) || Boolean.class.equals(expectedParameterType)) {
			return Boolean.parseBoolean(string);
		} else if (long.class.equals(expectedParameterType) || Long.class.equals(expectedParameterType)) {
			return Long.parseLong(string);
		} else if (float.class.equals(expectedParameterType) || Float.class.equals(expectedParameterType)) {
			return Float.parseFloat(string);
		} else if (double.class.equals(expectedParameterType) || Double.class.equals(expectedParameterType)) {
			return Double.parseDouble(string);
		} else if (char.class.equals(expectedParameterType) || Character.class.equals(expectedParameterType)) {
			return string == null || string.isEmpty() ? '\0' : string.charAt(0);
		} else if (byte.class.equals(expectedParameterType) || Byte.class.equals(expectedParameterType)) {
			return Byte.parseByte(string);
		} else if (short.class.equals(expectedParameterType) || Short.class.equals(expectedParameterType)) {
			return Short.parseShort(string);
		} else {
			throw new UnsupportedOperationException("Don't know how to convert to: " + expectedParameterType + " string: "+ string);
		}
	}
	
	public static String toString(Object object) {
		if (object == null) {
			return NULL;
		} else {
			return object.toString();
		}
	}
}
