package com.flowerplatform.rapp_mini_server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.crispico.flower_platform.remote_object.RemoteObjectServiceInvoker;
import com.crispico.flower_platform.remote_object.RemoteObjectServlet;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectHub;

/**
 * @author Cristian Spiescu
 */
public abstract class AbstractRappMiniServerMain {

	protected int port = 9000;
	
	protected boolean threadJoin = false;
	
	protected RemoteObjectServiceInvoker serviceInvoker;

	protected RemoteObjectHub hub = new RemoteObjectHub();
	
	protected void run()  {
        try {
			Server server = new Server(port);

			ServletContextHandler handler = new ServletContextHandler();
			handler.setContextPath("/");
			server.setHandler(handler);
			populateServletHandler(handler);
			server.start();

			
			// The use of server.join() the will make the current thread join and
			// wait until the server is done executing.
			// See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
			if (threadJoin) {
			    server.join();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void populateServletHandler(ServletContextHandler handler) {
        FilterHolder corsFilterHandler = handler.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        
        addRemoteObjectServlets(handler);
	}
	
	protected void addRemoteObjectServlets(ServletContextHandler handler) {
		handler.addServlet(new ServletHolder(new RemoteObjectServlet(serviceInvoker, "12345678")), "/remoteObject/*");
//		handler.addServlet(new ServletHolder(new RemoteObjectWebSocketServlet(hub)), "/remoteObjectWs/*");
//		handler.addServlet(new ServletHolder(new RemoteObjectHubServlet(hub)), "/hub/*");
	}

}
