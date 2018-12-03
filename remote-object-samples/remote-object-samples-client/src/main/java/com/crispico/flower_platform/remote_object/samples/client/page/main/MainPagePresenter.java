package com.crispico.flower_platform.remote_object.samples.client.page.main;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.alert.Alert;
import com.crispico.client.component.properties_form.PropertiesFormPWidget;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.client.component.repeater.RepeaterPWidget;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyProxy;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyView;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.test_button_renderer.TestButtonRendererPresenter;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.foundation.annotation.definition.ComponentType;
import com.crispico.foundation.annotation.definition.GenComponentRegistration;
import com.crispico.foundation.annotation.definition.TriggerFoundationAnnotationProcessor;
import com.crispico.foundation.client.component.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.view.FoundationView;
import com.crispico.shared.MapBuilder;
import com.crispico.shared.util.Pair;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.FoundationPagePresenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
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
	
    protected final SingleSlot<PresenterWidget<?>> SLOT_HUB_PARAMS = addNamedSlot(new SingleSlot<>(), "SLOT_HUB_PARAMS");
    protected final SingleSlot<PresenterWidget<?>> SLOT_TEST_BUTTONS = addNamedSlot(new SingleSlot<>(), "SLOT_TEST_BUTTONS");

    @Inject
    protected PropertiesFormPWidget hubParamsForm;
    
    public Map<String, String> hubParams;

    public JavaScriptObject hubConnection;

    @Inject
    protected RepeaterPWidget buttonsRepeater;
    
	@Inject
	protected MainPagePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
		super(eventBus, view, proxy, ClientGlobals.getMainSlot());
	}
    
	@Inject
	protected void postCreate(Provider<ServicePresenter> provider, Provider<TestButtonRendererPresenter> rendererProvider) {
		buttonsRepeater.getProperties().setChildProvider(rendererProvider);
		setInSlot(SLOT_TEST_BUTTONS, buttonsRepeater);
		buttonsRepeater.setModel(Arrays.asList(
				new Pair<String, Runnable>("salut", this::test1_JS_to_Java_direct),
				new Pair<String, Runnable>("salut", null)
				));
		
		String javaIpAddress = "192.168.100.151";
//		String javaIpAddress = "localhost";
		String hubIpAddress = javaIpAddress;
		String arduinoIpAddress = "192.168.100.182";

		new RemoteObject();

		initHubParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue(
				"remoteAddress", javaIpAddress + ":9001", 
				"securityToken", "JS_TOKEN", 
				"nodeId", "JS_Node1", 
				"mode", "2"));
		addNamedSlot(ClientGlobals.getDefaultMultiSlot(), "SLOT_MAIN");
		
		{  // Java
			ServicePresenter service = provider.get();
			service.initConnectionParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue(
					"instanceName", "",
					"remoteAddress", javaIpAddress + ":9001", 
					"securityToken", "JAVA_TKN", 
					"nodeId", "Java_Node"));
			FunctionPresenter caller;

			caller = service.addFunction();
			caller.setFunctionName("sayHello");
			caller.initParams("name", "John", "n", "10", "f", "3.14", "b", "true");
			
			caller = service.addFunction();
			caller.setFunctionName("sayHelloComplex");
			caller.initParams("complexObject", "{\"a\":5,\"b\":\"test string\"}");

			caller = service.addFunction();
			caller.setFunctionName("initRemoteObject1");
			caller.initParams("ip", arduinoIpAddress, "port", "9001", "objectName", "", "securityToken", "CPPTOKEN");

			caller = service.addFunction();
			caller.setFunctionName("callSayHelloRo1");
			caller.initParams("name", "Joe");
			
			caller = service.addFunction();
			caller.setFunctionName("connectToHub");
			caller.initParams("hubIp", hubIpAddress, "port", "9001", "nodeId", "Java_Node", "securityToken", "JAVA_TKN", "mode", "0", "pollingInterval", "1000");

			caller = service.addFunction();
			caller.setFunctionName("pollNowHub");

			caller = service.addFunction();
			caller.setFunctionName("initRemoteObject2");
			caller.initParams("ip", hubIpAddress, "port", "9001", "objectName", "", "securityToken", "JAVA_TKN", "nodeId", "");

			caller = service.addFunction();
			caller.setFunctionName("callSayHelloRo2");
			caller.initParams("name", "Joe");

			caller = service.addFunction();
			caller.setFunctionName("disconnectFromHub");
			caller.initParams();
			
			addToSlot(ClientGlobals.getDefaultMultiSlot(), service);
		}

		{ // Arduino
			ServicePresenter service = provider.get();
			service.initConnectionParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue(
					"instanceName", "",
					"remoteAddress", arduinoIpAddress + ":9001", 
					"securityToken", "CPPTOKEN", 
					"nodeId", "CPP_Node"));
			FunctionPresenter caller;

			caller = service.addFunction();
			caller.setFunctionName("sayHello");
			caller.initParams("name", "John", "n", "10", "f", "3.14", "b", "true");
			
			caller = service.addFunction();
			caller.setFunctionName("initRemoteObject1");
			caller.initParams("ip", javaIpAddress, "port", "9001", "objectName", "", "securityToken", "JAVA_TKN");

			caller = service.addFunction();
			caller.setFunctionName("callSayHelloRo1");
			caller.initParams("name", "Joe");

			caller = service.addFunction();
			caller.setFunctionName("connectToHub");
			caller.initParams("hubIp", javaIpAddress, "port", "9001", "nodeId", "CPP_Node", "securityToken", "CPPTOKEN", "mode", "0", "pollingInterval", "1000");

			caller = service.addFunction();
			caller.setFunctionName("pollNowHub");
			
			caller = service.addFunction();
			caller.setFunctionName("initRemoteObject2");
			caller.initParams("ip", javaIpAddress, "port", "9001", "objectName", "", "securityToken", "CPPTOKEN", "nodeId", "Java_Node");

			caller = service.addFunction();
			caller.setFunctionName("callSayHelloRo2");
			caller.initParams("name", "Joe");

			caller = service.addFunction();
			caller.setFunctionName("disconnectFromHub");
			caller.initParams();

			caller = service.addFunction();
			caller.setFunctionName("initRemoteObject3");
			caller.initParams("port", "9005", "objectName", "js.sampleService", "securityToken", "JS_TOKEN", "nodeId", "");

			caller = service.addFunction();
			caller.setFunctionName("callSayHelloRo3");
			caller.initParams("name", "Joe");

			caller = service.addFunction();
			caller.setFunctionName("stopRemoteObject3");
			caller.initParams();

			addToSlot(ClientGlobals.getDefaultMultiSlot(), service);
		}

		{  // JS
			ServicePresenter service = provider.get();
			service.initConnectionParams(MapBuilder.createMapFromArrayAsStringKeyObjectValue(
					"instanceName", "js.sampleService",
					"nodeId", "JS_Node1"));
			service.getView().hideDirectROButton();
			
			FunctionPresenter caller;

			caller = service.addFunction();
			caller.setFunctionName("sayHello");
			caller.initParams("name", "John", "n", "10", "f", "3.14", "b", "true");
			
			caller = service.addFunction();
			caller.setFunctionName("sayHelloComplex");
			caller.initParams("complexObject", "{\"a\":5,\"b\":\"test string\"}");
			
			addToSlot(ClientGlobals.getDefaultMultiSlot(), service);
		}

		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initHubParams(Map value) {
		hubParams = value;
		hubParamsForm.setInline(true);
		hubParamsForm.setPropertyAccessorCommitter(new MapPropertyAccessorCommitter());
		hubParamsForm.setPropertyDescriptors(hubParams.keySet().stream().map(key -> new PropertyDescriptor().setName(key)).collect(Collectors.toList()));
		hubParamsForm.setModel(hubParams);

		setInSlot(SLOT_HUB_PARAMS, hubParamsForm);
	}

	public native JavaScriptObject createHubConnection(JavaScriptObject hubParams) /*-{
		var roi = new $wnd.rapp_mini_server.JsRemoteObjectBase();
    	var hubConnection;
    	
    	if (hubParams.mode == "2") {
	    	hubConnection = new  $wnd.rapp_mini_server.RemoteObjectWebSocketConnection()
					.setRemoteAddress(hubParams.remoteAddress)
					.setSecurityToken(hubParams.securityToken)
					.setLocalNodeId(hubParams.nodeId)
		   			.setServiceInvoker(roi);
    	} else {
	    	hubConnection = new  $wnd.rapp_mini_server.RemoteObjectHubConnection()
					.setRemoteAddress(hubParams.remoteAddress)
					.setSecurityToken(hubParams.securityToken)
					.setLocalNodeId(hubParams.nodeId)
		   			.setScheduler(roi)
		   			.setRequestSender(roi)
		   			.setServiceInvoker(roi);
    	}

	   	hubConnection.start();

	   	return hubConnection;
	}-*/;

	public native void stopHubConnection(JavaScriptObject hubConnection) /*-{
	   	hubConnection.stop();
	}-*/;

	protected void test1_JS_to_Java_direct() {
		Alert.show("test 1");
	}
}
