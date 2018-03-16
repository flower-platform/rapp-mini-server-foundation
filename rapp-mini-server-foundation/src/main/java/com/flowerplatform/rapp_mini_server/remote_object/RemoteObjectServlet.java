package com.flowerplatform.rapp_mini_server.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	private DateFormat df = new SimpleDateFormat("HH:mm:ss.S");

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

		System.out.println(String.format("[%s] %s -> %s", df.format(new Date()), request.getRemoteAddr(), rawPacket));

		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		FlowerPlatformRemotingProtocolPacket res = processor.processPacket(packet);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");		
		PrintWriter out = response.getWriter();
		out.write(res.getRawData());
//		System.out.println(String.format("[%s] %s <- %s", df.format(new Date()), request.getRemoteAddr(), res.getRawData()));
	}


}
