package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Parameter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.flowerplatform.rapp_mini_server.shared.FlowerPlatformRemotingProtocolPacket;

/**
 * @author Cristian Spiescu
 */
public class RemoteObjectServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private RemoteObjectServiceInvoker serviceInvoker;
	
	private String securityToken;

	public RemoteObjectServlet(RemoteObjectServiceInvoker serviceInvoker, String securityToken) {
		this.serviceInvoker = serviceInvoker;
		this.securityToken = securityToken;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		String functionPath = packet.nextField();
		
		RemoteObjectInfo serviceInfo;
		try {
			serviceInfo = serviceInvoker.findInstanceAndMethod(functionPath);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);	
		}
		
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
		
		Object result;
		try {
			result = serviceInvoker.invoke(serviceInfo, sb.toString());
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}

		PrintWriter out = response.getWriter();
		FlowerPlatformRemotingProtocolPacket res = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
		res.addField("0"); // hasNext
		res.addField(""); // callbackId
		res.addField(result.toString());
		out.write(res.getRawData());
	}

	
}
