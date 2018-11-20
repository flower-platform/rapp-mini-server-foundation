package com.crispico.flower_platform.remote_object.samples.client.page.main;

import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.flower_platform.remote_object.samples.client.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyProxy;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyView;
import com.crispico.flower_platform.remote_object.samples.client.service.ServicePresenter;
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
			ServicePresenter service = provider.get();
			service.setName("myService");
			service.initConnectionParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue("param1", "value1", "param2", "value2"));
			FunctionPresenter caller;

			caller = service.addFunction();
			caller.setFunctionName("myFunction");
			caller.initParams("name", "def", "count", "");
//			caller.onClick();
			new Consumer<Map<String, String>>() {

				@Override
				public native void accept(Map<String, String> t)/*-{
					ro.myFunction(t.@java.util.Map::get(*)("name"), t.get("count"));
				}-*/;

			};
			
			caller = service.addFunction();
			caller.setFunctionName("myOtherFunction");
			caller.initParams("name1", "def", "count1", "");

			addToSlot(ClientGlobals.getDefaultMultiSlot(), service);
		}
	
	}
		
}
