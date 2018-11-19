package com.crispico.flower_platform.remote_object.samples.client;

import javax.inject.Inject;

import com.crispico.client.AbstractClientBootstrapper;
import com.crispico.client.ClientUtils;
import com.crispico.foundation.shared.i18n.Messages;

/**
 * Initialization logic, that runs at module startup.
 * 
 * @author Cristian Spiescu
 */
public class ClientBootstrapper extends AbstractClientBootstrapper {
	
	@Inject
	public ClientBootstrapper(Resources resources) {
		ClientUtils.injectScript(resources.plainJsCode().getText());
		Messages.getInstance().parseTsvContent(resources.messages().getText());
	}
	
}
