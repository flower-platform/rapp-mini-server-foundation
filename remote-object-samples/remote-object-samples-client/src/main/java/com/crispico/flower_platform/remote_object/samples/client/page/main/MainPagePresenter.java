package com.crispico.flower_platform.remote_object.samples.client.page.main;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyProxy;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyView;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.foundation.annotation.definition.ComponentType;
import com.crispico.foundation.annotation.definition.GenComponentRegistration;
import com.crispico.foundation.annotation.definition.TriggerFoundationAnnotationProcessor;
import com.crispico.foundation.client.view.FoundationView;
import com.crispico.shared.MapBuilder;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.FoundationPagePresenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * @author Cristian Spiescu
 */
@TriggerFoundationAnnotationProcessor
@GenComponentRegistration(componentType=ComponentType.PAGE)
public class MainPagePresenter extends FoundationPagePresenter<MyView, MyProxy> {

    public static interface MyView extends FoundationView {
	}

	@NameToken(NAME_TOKEN)
	@ProxyStandard
	public interface MyProxy extends ProxyPlace<MainPagePresenter> {
	}

	public static final String NAME_TOKEN = "/main";

	@Inject
	protected MainPagePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
		super(eventBus, view, proxy, ClientGlobals.getMainSlot());
	}
	
	@Inject
	protected void postCreate(Provider<ServicePresenter> provider) {
		addNamedSlot(ClientGlobals.getDefaultMultiSlot(), "SLOT_MAIN");
		
		{
			new RemoteObject();
			ServicePresenter service = provider.get();
			service.initConnectionParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue(
					"remoteAddress", "localhost:9001", 
					"securityToken", "44444444", 
					"nodeId", "", 
					"instanceName", "e"));
			FunctionPresenter caller;

			caller = service.addFunction();
			caller.setFunctionName("sayHello");
			caller.initParams("name", "John", "n", "10", "f", "3.14", "b", "true");
			
			caller = service.addFunction();
			caller.setFunctionName("myOtherFunction");
			caller.initParams("name1", "def", "count1", "");

			addToSlot(ClientGlobals.getDefaultMultiSlot(), service);
		}
	
	}
		
}
