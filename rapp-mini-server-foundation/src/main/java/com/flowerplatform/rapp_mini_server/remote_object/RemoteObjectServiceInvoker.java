package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;
import com.flowerplatform.rapp_mini_server.shared.IRemoteObjectServiceInvoker;

/**
 * @author Claudiu Matei
 */
public class RemoteObjectServiceInvoker implements IRemoteObjectServiceInvoker {

	/**
	 * Object whose own or successors' methods are invoked. For nested calls (on successors), this is the starting point for looking up the method to be invoked. 
	 */
	private Object serviceInstance;

	private JsonFactory jsonFactory;
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public RemoteObjectServiceInvoker(Object serviceInstance) {
		this.serviceInstance = serviceInstance;
		jsonFactory = new JsonFactory();
		jsonFactory.setCodec(objectMapper);
	}
	
	private Object findInstance(String methodPath) {
		// lookup actual instance whose method must be invoked
		Object instance = serviceInstance;
		int k;
		while ((k = methodPath.indexOf('.')) > 0) {
			String instanceStr = methodPath.substring(0,  k);
			try {
				instance = instance.getClass().getField(instanceStr).get(instance);
			} catch (ReflectiveOperationException e) {
				throw new IllegalArgumentException(methodPath, e);
			}
			methodPath = methodPath.substring(k + 1);
		}
		return instance;
	}
	
	private Method findMethod(Object instance, String methodPath) {
		String methodName = methodPath.substring(methodPath.lastIndexOf('.') + 1);

		// lookup matching method
		Method method = null;
		for (Method m : instance.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		if (method == null) {
			throw new IllegalArgumentException("Couldn't find matching method for " + methodPath);
		}
		return method;
	}

	private Object invoke(Object instance, Method method, String argumentsAsJsonArray) {
		// get typed arguments from methodInvocation string
		Object[] args;
		try {
			args = parseParameters(method, jsonFactory.createParser(argumentsAsJsonArray));
		} catch (IOException e) {
			throw new IllegalArgumentException("Invalid JSON arguments array: " + argumentsAsJsonArray, e);
		}
		
		// invoke function
		Object result;
		try {
			result = method.invoke(instance, args);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}
	
	private Object[] parseParameters(Method method, JsonParser parametersJsonParser) {
		Parameter[] parameters = method.getParameters();
		Object[] res = new Object[parameters != null ? parameters.length : 0];

		try (JsonParser parser = parametersJsonParser) {
			JsonToken token = parser.nextToken();
			if (token == null) {
			    // empty request content; do nothing, just return
				return res;
			}
			
			if (!JsonToken.START_ARRAY.equals(token)) {
			    throw new IllegalArgumentException("Invalid request format. Expected an array of objects, but found token: " + token);
			}

			// iterate through the content of the array
			for (int i = 0; i < parameters.length; i++) {
				res[i] = jsonParseSingleParam(parser, parameters[i]);
			}
			token = parser.nextToken();
			if (!JsonToken.END_ARRAY.equals(token)) {
			    throw new IllegalArgumentException("Invalid request format. Expected an end of array of objects, but found token: " + token);
			}
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Error parsing the request body.", ioe);
		}
		return res;
	}

	private Object jsonParseSingleParam(JsonParser argParser, Parameter parameter) throws IOException {
		final Class<?> expectedParameterType = parameter.getType();
		
		// force the parser to advance to the next token.
		argParser.nextToken();
		
		if (java.util.Collection.class.isAssignableFrom(expectedParameterType)) {
			// Try to deserialize as collection
			final JsonTypeInfo jsonTypeInfo = parameter.getAnnotation(JsonTypeInfo.class);
			if (jsonTypeInfo == null || jsonTypeInfo.defaultImpl() == null) {
				throw new IllegalArgumentException("Cannot deserialize parameter \"" + parameter.getName() + "\". No @" + JsonTypeInfo.class.getName() + " annotation present or no defaultImpl attribute.");
			}

			return argParser.readValueAs(
					// A hackish TypeReference : create any kind of TypeReference, but override its type towards what we actually need.
					new TypeReference<Collection<?>>() {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						@Override
						public Type getType() {
							return TypeFactory.defaultInstance().constructCollectionType((Class<? extends Collection>)expectedParameterType, jsonTypeInfo.defaultImpl());
						}
					}
			);
		} else {
			if (String.class.equals(expectedParameterType)) {
				return argParser.getValueAsString();
			} else if (int.class.equals(expectedParameterType) || Integer.class.equals(expectedParameterType)) {
				return argParser.getIntValue();
			} else if (boolean.class.equals(expectedParameterType) || Boolean.class.equals(expectedParameterType)) {
				return argParser.getBooleanValue();
			} else if (long.class.equals(expectedParameterType) || Long.class.equals(expectedParameterType)) {
				return argParser.getLongValue();
			} else if (float.class.equals(expectedParameterType) || Float.class.equals(expectedParameterType)) {
				return argParser.getFloatValue();
			} else if (double.class.equals(expectedParameterType) || Double.class.equals(expectedParameterType)) {
				return argParser.getDoubleValue();
			} else if (char.class.equals(expectedParameterType) || Character.class.equals(expectedParameterType)) {
				return (char)argParser.getByteValue();
			} else if (byte.class.equals(expectedParameterType) || Byte.class.equals(expectedParameterType)) {
				return argParser.getByteValue();
			} else if (short.class.equals(expectedParameterType) || Short.class.equals(expectedParameterType)) {
				return argParser.getShortValue();
			}
		}

		return argParser.readValueAs(expectedParameterType);
	}

	
	public Object invoke(String methodPath, String argumentsAsJsonArray) {
		Object instance = findInstance(methodPath);
		Method method = findMethod(instance, methodPath);
		Object result = invoke(instance, method, argumentsAsJsonArray);
		return result;
	}

	@Override
	public Object invoke(FlowerPlatformRemotingProtocolPacket packet) {
		String methodPath = packet.nextField();
		Object instance = findInstance(methodPath);
		Method method = findMethod(instance, methodPath);
		
		// build JSON array containing arguments 
		Parameter[] parameters = method.getParameters();
		StringBuilder argumentsAsJsonArray = new StringBuilder();
		argumentsAsJsonArray.append("[");
		int i = 0;
		for (Parameter param : parameters) {
			if (String.class.equals(param.getType())) {
				argumentsAsJsonArray.append('"');
			}
			argumentsAsJsonArray.append(packet.nextField());
			if (String.class.equals(param.getType())) {
				argumentsAsJsonArray.append('"');
			}
			if (i < parameters.length - 1) {
				// i.e. not last one
				argumentsAsJsonArray.append(',');
			}
			i++;
		}
		argumentsAsJsonArray.append(']');
		
		Object result = invoke(instance, method, argumentsAsJsonArray.toString());
		return result;
	}
	

	/**
	 * Test
	 */
	public static void main(String[] args) throws Exception {
		RemoteObjectServiceInvoker invoker = new RemoteObjectServiceInvoker(new TestService());
		invoker.invoke("sayHello", "[\"Mumu1\",2]");
		invoker.invoke("testService.sayHello", "[\"Mumu2\",2]");
	}
	
}

/**
 * Used for testing
 */
class TestService {
	
	public TestService testService;
	
	public void sayHello(String name, int times) {
		for (int i = 0; i < times; i++) {
			System.out.println(name);;
		}
		testService = new TestService();
	}
	
}
