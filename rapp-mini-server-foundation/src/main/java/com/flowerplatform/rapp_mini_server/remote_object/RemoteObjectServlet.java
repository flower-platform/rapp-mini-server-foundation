package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
		
		if (packet.getCommand() != 'I') {
			return;
		}
		
		packet.nextField(); // hasNext (ignore)
		packet.nextField(); // rappInstanceId (ignore)
		String callbackId =	packet.nextField(); // callbackId
		
		Object result = serviceInvoker.invoke(packet);

		PrintWriter out = response.getWriter();
		FlowerPlatformRemotingProtocolPacket res = new FlowerPlatformRemotingProtocolPacket(securityToken, 'R');
		res.addField("0"); // hasNext
		res.addField(callbackId); // callbackId
		res.addField(result.toString());
		out.write(res.getRawData());
	}

	
}
