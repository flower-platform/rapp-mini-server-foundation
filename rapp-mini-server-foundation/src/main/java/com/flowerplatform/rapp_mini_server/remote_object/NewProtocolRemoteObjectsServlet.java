package com.flowerplatform.rapp_mini_server.remote_object;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
		FlowerPlatformRemotingProtocolScanner scanner = new FlowerPlatformRemotingProtocolScanner(request.getInputStream());
		return null;
	}

}
