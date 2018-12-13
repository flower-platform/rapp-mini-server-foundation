package com.crispico.flower_platform.remote_object.samples.client;

import com.crispico.foundation.annotation.definition.GenAggregatedGinModule;
import com.crispico.foundation.annotation.definition.TriggerFoundationAnnotationProcessor;
import com.crispico.foundation.client.AbstractMainGinModule;

/**
 * GIN injection configuration: (bind + GIN module install).
 * 
 * @author Cristian Spiescu
 */
@TriggerFoundationAnnotationProcessor
@GenAggregatedGinModule(includeAggregatedServerModule = false)
public class MainGinModule extends AbstractMainGinModule {
    
	@Override
    protected void configure() {
    	super.configure();
	}
}
