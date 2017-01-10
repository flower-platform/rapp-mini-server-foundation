package com.flowerplatform.rapp_mini_server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectHubServlet;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServiceInvoker;
import com.flowerplatform.rapp_mini_server.remote_object.RemoteObjectServlet;

/**
 * @author Cristian Spiescu
 */
public abstract class AbstractRappMiniServerMain {

	protected int port = 9000;
	
	protected boolean threadJoin = false;
	
	protected RemoteObjectServiceInvoker serviceInvoker;
	
	protected void run() throws Exception {
        Server server = new Server(port);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        populateServletHandler(handler);
        server.start();

        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        if (threadJoin) {
            server.join();
        }
	}
	
	protected void populateServletHandler(ServletHandler handler) {
        FilterHolder corsFilterHandler = handler.addFilterWithMapping(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD));
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
        corsFilterHandler.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        
        addRemoteObjectsServlet(handler);
	}
	
	protected void addRemoteObjectsServlet(ServletHandler handler) {
		handler.addServletWithMapping(new ServletHolder(new RemoteObjectServlet(serviceInvoker, "12345678")), "/remoteObject/*");
		handler.addServletWithMapping(new ServletHolder(new RemoteObjectHubServlet()), "/hub/*");
	}

}
