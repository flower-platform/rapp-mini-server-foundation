package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO CS/RASP: de sters
 * @author Cristian Spiescu
 */
public class OldProtocolRemoteObjectsServlet extends AbstractRemoteObjectsServlet {

	private static final long serialVersionUID = 1L;

	public OldProtocolRemoteObjectsServlet(Map<String, Object> serviceRegistry) {
		super(serviceRegistry);
	}

	/**
	 * The initial code was written to process a json array (i.e. [a, b, ...]). It has lots of
	 * cases, etc. In order to reuse this logic, the simplest way is to recreate a json array 
	 * starting from the arguments that we parse from the request.
	 */
	@Override
	protected RemoteObjectInfo createServiceInfo(HttpServletRequest request, String path) throws Exception {
		String[] pathComponents = path.split("/");
		if (pathComponents.length < 2) {
			// Should not happen as this should get caught at the previous step, but just in case...
			throw new RuntimeException("Your request is not valid. The request needs to be in the format <myService>/<myMethod>.");
		}
		
		RemoteObjectInfo serviceInfo = new RemoteObjectInfo();
		findInstanceAndMethod(serviceInfo, pathComponents[0], pathComponents[1]);
		
		Parameter[] parameters = serviceInfo.getMethod().getParameters();
		if (parameters.length != pathComponents.length - 2) {
			throw new IllegalArgumentException("Illegal number of arguments for: " + request.getRequestURI() + "; needed: " + parameters.length + "; actual: " + (pathComponents.length - 2));			
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int i = 0;
		for (Parameter param : parameters) {
			if (String.class.equals(param.getType())) {
				sb.append('"');
			}
			// +2 because of service/method
			sb.append(pathComponents[i + 2]);
			if (String.class.equals(param.getType())) {
				sb.append('"');
			}
			if (i < parameters.length - 1) {
				// i.e. not last one
				sb.append(',');
			}
			i++;
		}
		sb.append(']');
		
		parseParameters(serviceInfo, jsonFactory.createParser(sb.toString()));
		
		return serviceInfo;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
}
