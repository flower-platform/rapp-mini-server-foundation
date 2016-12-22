package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

/**
 * @author Cristian Spiescu
 */
public class NewProtocolRemoteObjectsServlet extends AbstractRemoteObjectsServlet {

	private static final long serialVersionUID = 1L;

	public NewProtocolRemoteObjectsServlet(Map<String, Object> serviceRegistry) {
		super(serviceRegistry);
	}

	@Override
	protected RemoteObjectInfo createServiceInfo(HttpServletRequest request, String path) throws Exception {
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);
		
		System.out.println("-> " + rawPacket);
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		
		packet.nextField(); // hasNext (ignore)
		packet.nextField(); // rappInstanceId (ignore)
//		String callbackId = 
				packet.nextField(); // callbackId
		String instanceName = packet.nextField();
		String method = packet.nextField();
		
		RemoteObjectInfo serviceInfo = new RemoteObjectInfo();
		findInstanceAndMethod(serviceInfo, instanceName, method);
		
		Parameter[] parameters = serviceInfo.getMethod().getParameters();
		if (parameters.length != packet.availableFields()) {
			throw new IllegalArgumentException("Illegal number of arguments for: " + request.getRequestURI() + "; needed: " + parameters.length + "; actual: " + packet.availableFields());			
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int i = 0;
		for (Parameter param : parameters) {
			if (String.class.equals(param.getType())) {
				sb.append('"');
			}
			// +2 because of service/method
			sb.append(packet.nextField());
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

	
	
}
