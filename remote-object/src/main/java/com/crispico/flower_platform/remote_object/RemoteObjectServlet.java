package com.crispico.flower_platform.remote_object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;

/**
 * @author Cristian Spiescu
 */
public class RemoteObjectServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private DateFormat df = new SimpleDateFormat("HH:mm:ss.S");

	private RemoteObjectProcessor processor;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String securityToken = config.getInitParameter("securityToken");
		if (securityToken == null || securityToken.length() != 8) {
			throw new ServletException("Configuration error: invalid security token");
		}
		processor = new RemoteObjectProcessor(securityToken, RemoteObjectServiceInvoker.getInstance());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (processor == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "RemoteObjectProcessor not initialized. Is servlet configured?");
			return;
		}
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);

		System.out.println(String.format("[%s] %s -> %s", df.format(new Date()), request.getRemoteAddr(), rawPacket));

		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		FlowerPlatformRemotingProtocolPacket res = processor.processPacket(packet);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");		
		OutputStream out = response.getOutputStream();
		out.write(res.getRawData().getBytes());
		out.flush();
		System.out.println(String.format("[%s] %s <- %s", df.format(new Date()), request.getRemoteAddr(), res.getRawData()));
	}

}
