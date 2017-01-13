package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

/**
 * @author Cristian Spiescu
 */
public class NewProtocolRemoteObjectsServlet extends AbstractRemoteObjectsServlet {

	private static final long serialVersionUID = 1L;

	private String securityToken;
	
	public NewProtocolRemoteObjectsServlet(Map<String, Object> serviceRegistry, String securityToken) {
		super(serviceRegistry);
		this.securityToken = securityToken;
	}

	@Override
	protected RemoteObjectInfo createServiceInfo(HttpServletRequest request, String path) throws Exception {
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);
		
		System.out.println("-> " + rawPacket);
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		
//		packet.nextField(); // hasNext (ignore)
		packet.nextField(); // rappInstanceId (ignore)
//		String callbackId = 
				packet.nextField(); // callbackId
		String functionCall = packet.nextField();
		String instanceName = functionCall.substring(0, functionCall.lastIndexOf('.'));
		String method = functionCall.substring(instanceName.length() + 1);
		
		RemoteObjectInfo serviceInfo = new RemoteObjectInfo();
		findInstanceAndMethod(serviceInfo, instanceName, method);
		
		Parameter[] parameters = serviceInfo.getMethod().getParameters();
		if (parameters.length != packet.availableFieldCount()) {
			throw new IllegalArgumentException("Illegal number of arguments for: " + request.getRequestURI() + "; needed: " + parameters.length + "; actual: " + packet.availableFieldCount());			
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
	
	@Override
	protected void writeResponse(Object result, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();
		FlowerPlatformRemotingProtocolPacket res = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
//		res.addField("0"); // hasNext
		res.addField(""); // callbackId
		res.addField(result.toString());
		out.write(res.getRawData());
	}
	
}
