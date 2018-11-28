package com.crispico.flower_platform.remote_object;

import static com.crispico.flower_platform.remote_object.RemoteObjectHubClientData.CLIENT_TYPE_HTTP_PULL;
import static com.crispico.flower_platform.remote_object.RemoteObjectHubClientData.CLIENT_TYPE_HTTP_PUSH;

import java.io.DataInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crispico.flower_platform.remote_object.shared.FlowerPlatformRemotingProtocolPacket;

public class RemoteObjectHubServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private DateFormat df = new SimpleDateFormat("HH:mm:ss.S");
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getContentLength() == 0) {
			return;
		}
		System.out.println("ROHS: Request from " + request.getRemoteAddr());
		byte[] buf = new byte[request.getContentLength()];
		DataInputStream in = new DataInputStream(request.getInputStream());
		in.readFully(buf);
		String rawPacket = new String(buf);
		System.out.println(String.format("[%s] %s -> %s", df.format(new Date()), request.getRemoteAddr(), rawPacket));
		
		FlowerPlatformRemotingProtocolPacket packet = new FlowerPlatformRemotingProtocolPacket(rawPacket);
		
		String res;
		if (packet.getCommand() == 'A') {
			String nodeId = packet.nextField();
			String portStr = packet.nextField();
			RemoteObjectHubClientData client = new RemoteObjectHubClientData(portStr.length() > 0 && !portStr.equals("0") ? CLIENT_TYPE_HTTP_PUSH : CLIENT_TYPE_HTTP_PULL,  nodeId, packet.getSecurityToken());
			client.setRemoteIPAddress(request.getRemoteAddr());
			if (portStr.length() > 0) {
				client.setRemoteHttpServerPort(Integer.parseInt(portStr));
			}
			res = RemoteObjectHub.getInstance().registerClient(client);
		} else {
			res = RemoteObjectHub.getInstance().processPacket(packet);
		}
		System.out.print(String.format("[%s] %s <- %s", df.format(new Date()), request.getRemoteAddr(), res));
		response.setHeader("Content-Type", "text/plain");
		response.getWriter().print(res);
		System.out.println("\t[SENT]");
	}
	
}
