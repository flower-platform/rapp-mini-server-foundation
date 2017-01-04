package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Andrei Taras
 * @author Cristian Spiescu
 */
public abstract class AbstractRemoteObjectsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected static final Logger logger = Logger.getLogger(AbstractRemoteObjectsServlet.class.getCanonicalName());
	
	protected Map<String, Object> serviceRegistry;

	protected JsonFactory jsonFactory;

	public AbstractRemoteObjectsServlet(Map<String, Object> serviceRegistry) {
		super();
		this.serviceRegistry = serviceRegistry;
		jsonFactory = new JsonFactory();
		jsonFactory.setCodec(new ObjectMapper());
	}

	protected void writeResponse(Object result, HttpServletResponse response) throws IOException {
		new ObjectMapper().writeValue(response.getWriter(), result);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Prepare the path first
		String path = trimAndRemoveFirstSlash(request.getPathInfo());

		if (!validateUrl(response, path)) {
			return;
		}

		Object result = null;
		try {
			RemoteObjectInfo serviceInfo = createServiceInfo(request, path);
			result = invokeService(serviceInfo, request);
		} catch (Throwable se) {
			logger.log(Level.SEVERE, se.getMessage(), se);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, se.getMessage());
			return;
		}
		
		response.setContentType("text/plain; charset=utf-8");

		// Declare response status code
		response.setStatus(HttpServletResponse.SC_OK);

		// Write back response
		writeResponse(result, response);
	}
	
	protected boolean validateUrl(HttpServletResponse response, String path) {
		return true;
	}
	
	protected abstract RemoteObjectInfo createServiceInfo(HttpServletRequest request, String path) throws Exception;
	
	protected void findInstanceAndMethod(RemoteObjectInfo serviceInfo, String classNamePart, String methodNamePart) {
		Object service = serviceRegistry.get(classNamePart); 
		if (service == null) {
			throw new IllegalArgumentException("No such service registered: " + classNamePart);
		}
		
		Method method = null;
		Method[] methods = service.getClass().getMethods();
		if (methods != null) {
			for (Method cMethod : methods) {
				if (methodNamePart.equals(cMethod.getName())) {
					method = cMethod;
					break;
				}
			}
		}

		if (method == null) {
			throw new IllegalArgumentException("Can't find method with the name: " + methodNamePart);
		}
		
		serviceInfo.setRemoteObject(service);
		serviceInfo.setMethod(method);
	}

	protected void parseParameters(RemoteObjectInfo serviceInfo, JsonParser parametersJsonParser) {
		Parameter[] parameters = serviceInfo.getMethod().getParameters();
		serviceInfo.setArguments(new Object[parameters != null ? parameters.length : 0]);

		try (JsonParser parser = parametersJsonParser) {
			JsonToken token = parser.nextToken();
			if (token == null) {
			    // empty request content; do nothing, just return
				return;
			}
			
			if (!JsonToken.START_ARRAY.equals(token)) {
			    throw new IllegalArgumentException("Invalid request format. Expected an array of objects, but found token: " + token);
			}

			// iterate through the content of the array
			for (int i = 0; i < parameters.length; i++) {
				serviceInfo.getArguments()[i] = jsonParseSingleParam(parser, parameters[i]);
			}
			token = parser.nextToken();
			if (!JsonToken.END_ARRAY.equals(token)) {
			    throw new IllegalArgumentException("Invalid request format. Expected an end of array of objects, but found token: " + token);
			}
		} catch (IOException ioe) {
			throw new IllegalArgumentException("Error parsing the request body.", ioe);
		}
	}
	
	protected Object jsonParseSingleParam(JsonParser argParser, Parameter parameter) throws IOException {
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
	
	/**
	 * Invokes the service identified by the given {@link RemoteObjectInfo} class and returns
	 * the invocation's result (if any).
	 */
	protected Object invokeService(RemoteObjectInfo serviceInfo, HttpServletRequest request) {
		try {
			return serviceInfo.getMethod().invoke(serviceInfo.getRemoteObject(), serviceInfo.getArguments());
		} catch (Throwable th) {
			throw new RuntimeException(th);
		}
	}
	
	private String trimAndRemoveFirstSlash(String target) {
		if (target != null) {
			target = target.trim();
			if (target.length() > 0 && target.charAt(0) == '/') {
				target = target.substring(1);
			}
		}
		return target;
	}
	
	/**
	 * Sends an error code + message back to the user.
	 */
	protected void sendError(HttpServletResponse response, int statusCode, String message) {
		// Declare response encoding and types
		response.setContentType("text/plain; charset=utf-8");

		// Declare response status code
		response.setStatus(statusCode);

		// Write back response
		try {
			response.getWriter().println(message);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
