package com.crispico.flower_platform.remote_object.samples.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Resources packaged with the module. 
 * 
 * <p/>
 * NOTE: resources of type {@link TextResource} are embedded within the JS compiled by GWT.<br/>
 * NOTE: JS resources are minified by the build process. 
 * 
 * @author Cristian Spiescu
 */
public interface Resources extends ClientBundle {

	@Source("com/crispico/flower_platform/remote_object/samples/client/messages.tsv")
	TextResource messages();
	
	@Source("com/crispico/flower_platform/remote_object/samples/client/plain-js-code.js")
	TextResource plainJsCode();
	
}
