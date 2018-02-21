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

	private RemoteObjectProcessor processor;
	
	public RemoteObjectServlet(RemoteObjectServiceInvoker serviceInvoker, String securityToken) {
		processor = new RemoteObjectProcessor(securityToken, serviceInvoker);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);

		System.out.println("-> " + rawPacket);

		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		FlowerPlatformRemotingProtocolPacket res = processor.processPacket(packet);

		PrintWriter out = response.getWriter();
		out.write(res.getRawData());
	}


}
