package com.crispico.flower_platform.remote_object.samples.client.page.main;

import static com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection.HUB_MODE_HTTP_PULL;
import static com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection.HUB_MODE_HTTP_PUSH;
import static com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection.HUB_MODE_WEB_SOCKET;

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
import com.crispico.flower_platform.remote_object.client.JsRemoteObjectBase;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyProxy;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter.MyView;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.test_button_renderer.TestButtonRendererPresenter;
import com.crispico.flower_platform.remote_object.shared.RemoteObject;
import com.crispico.flower_platform.remote_object.shared.RemoteObjectHubConnection;
import com.crispico.flower_platform.remote_object.shared.ResultCallback;
import com.crispico.foundation.annotation.definition.ComponentType;
import com.crispico.foundation.annotation.definition.GenComponentRegistration;
import com.crispico.foundation.annotation.definition.TriggerFoundationAnnotationProcessor;
import com.crispico.foundation.client.component.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.view.FoundationView;
import com.crispico.shared.MapBuilder;
import com.crispico.shared.util.Pair;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
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
    	void setHubconnectionType(int type);
	}

	@NameToken(NAME_TOKEN)
	@ProxyStandard
	public interface MyProxy extends ProxyPlace<MainPagePresenter> {
	}

	public static final String NAME_TOKEN = "/main";

	private static final int JAVA_SERVICE_PRESENTER = 0;
	private static final int CPP_SERVICE_PRESENTER = 1;
	private static final int JS_SERVICE_PRESENTER = 2;
	
	private static final String javaIpAddress = "192.168.100.151";
//	private static final String javaIpAddress = "localhost";
	private static final String hubIpAddress = javaIpAddress;
	private static final String arduinoIpAddress = "192.168.100.175";
	
	private static MainPagePresenter INSTANCE;
	
    protected final SingleSlot<PresenterWidget<?>> SLOT_HUB_PARAMS = addNamedSlot(new SingleSlot<>(), "SLOT_HUB_PARAMS");
    protected final SingleSlot<PresenterWidget<?>> SLOT_TEST_BUTTONS = addNamedSlot(new SingleSlot<>(), "SLOT_TEST_BUTTONS");

    @Inject
    protected PropertiesFormPWidget hubParamsForm;
    
    public Map<String, String> hubParams;

    public JavaScriptObject hubConnection;

    @Inject
    protected RepeaterPWidget buttonsRepeater;

	private JsRemoteObjectBase roi = new JsRemoteObjectBase();
	
	private RemoteObject javaService;
	
	private ResultCallback defaultErrorCallback = r -> {
		nextTest();
	};
	
	private int lastTest = 0;
	private final int nTests = 14;

	
	@Inject
	protected MainPagePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
		super(eventBus, view, proxy, ClientGlobals.getMainSlot());
		INSTANCE = this;
	}
    
	public static MainPagePresenter getInstance() {
		return INSTANCE;
	}
	
	@Inject
	protected void postCreate(Provider<ServicePresenter> provider, Provider<TestButtonRendererPresenter> rendererProvider) {
		buttonsRepeater.getProperties().setChildProvider(rendererProvider);
		setInSlot(SLOT_TEST_BUTTONS, buttonsRepeater);
		buttonsRepeater.setModel(Arrays.asList(
				new Pair<String, Runnable>("Test 1", this::test1_JSToJavaDirectSayHello),
				new Pair<String, Runnable>("Test 2", this::test2_JSToJavaDirectSayHelloComplex),
				new Pair<String, Runnable>("Test 3", this::test3_JSToCPPDirectSayHello),
				new Pair<String, Runnable>("Test 4", this::test4_JSToJavaPushSayHello),
				new Pair<String, Runnable>("Test 5", this::test5_JSToJavaPollSayHello),
				new Pair<String, Runnable>("Test 6", this::test6_JSToCPPPushSayHello),
				new Pair<String, Runnable>("Test 7", this::test7_JSToCPPPollSayHello),
				new Pair<String, Runnable>("Test 8", this::test8_JSToJSPullSayHello),
				new Pair<String, Runnable>("Test 9", this::test9_JSToJSWebSocketSayHelloComplex),
				new Pair<String, Runnable>("Test 10", this::test10_JavaToCPPDirectSayHello),
				new Pair<String, Runnable>("Test 11", this::test11_JavaToCPPPollSayHello),
				new Pair<String, Runnable>("Test 12", this::test12_CPPToJavaDirectSayHello),
				new Pair<String, Runnable>("Test 13", this::test13_CPPToJSWebSocketSayHello),
				new Pair<String, Runnable>("Test 14", this::test14_CPPToJavaPollSayHello),
				new Pair<String, Runnable>("Run all", () -> { resetTests(); nextTest(); })
				));

		 javaService = roi.initialize(new RemoteObject()
					.setRemoteAddress(javaIpAddress + ":9001")
					.setSecurityToken("JAVA_TKN"));

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
			caller.initParams("ip", hubIpAddress, "port", "9001", "objectName", "", "securityToken", "JAVA_TKN", "nodeId", "CPP_Node");

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
			caller.setFunctionName("wsNotify");

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
			
			if (Window.Location.getParameter("runTests") != null) {
				resetTests();
				nextTest();
			};
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

	public void hubConnectButtonClick(Runnable callback) {
			JSONObject o = new JSONObject();
			hubParams.forEach((k, v) -> o.put(k, new JSONString((String) v)));
			hubConnection = createHubConnection(o.getJavaScriptObject(), callback);
	}

	public void hubDisconnectButtonClick() {
		stopHubConnection(hubConnection);
	}
	
	public native JavaScriptObject createHubConnection(JavaScriptObject hubParams, Runnable callback) /*-{
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

	   	hubConnection.start({
	   		onSuccess: function(o) {
	   			if (callback) {
	   				callback.@java.lang.Runnable::run()();
	   			}	
	   		}
	   	});

	   	return hubConnection;
	}-*/;

	public native void stopHubConnection(JavaScriptObject hubConnection) /*-{
	   	hubConnection.stop();
	}-*/;

	
	private void setHubParam(String key, String value) {
		hubParams.put(key, value);
		hubParamsForm.setModel(hubParams);
	}

	
	private ServicePresenter getServicePresenter(int serviceIndex) {
		ServicePresenter servicePresenter = (ServicePresenter) getChildren(ClientGlobals.getDefaultMultiSlot()).toArray()[serviceIndex];
		return servicePresenter;
	}
	
	private FunctionPresenter getFunctionPresenter(ServicePresenter servicePresenter, String functionName) {
		FunctionPresenter functionPresenter = null;
		for (PresenterWidget<?> pw : servicePresenter.getChildren(ClientGlobals.getDefaultMultiSlot())) {
			FunctionPresenter fp = (FunctionPresenter) pw;
			if (fp.getFunctionName().equals(functionName)) {
				functionPresenter = fp;
				break;
			}
		}
		return functionPresenter;
	}
	
	public void sendResult(String s, boolean ok) {
//		else {
			javaService.invokeMethod("saveTestResult", new Object[] { s, ok ? "OK" : "FAILED" }, null, null);
//		}
		if (lastTest == 0) {
			Alert.show(s + "\t" + (ok ? "OK" : "FAILED"));
		} else {
			nextTest();
		}
	}

	private void runTest(int n) {
		GWT.log("RUNNING TEST " + n);
		switch(n) {
		case 1: test1_JSToJavaDirectSayHello(); break;
		case 2: test2_JSToJavaDirectSayHelloComplex(); break;
		case 3: test3_JSToCPPDirectSayHello();
		case 4: test4_JSToJavaPushSayHello();
		case 5: test5_JSToJavaPollSayHello();
		case 6: test6_JSToCPPPushSayHello();
		case 7: test7_JSToCPPPollSayHello();
		case 8: test8_JSToJSPullSayHello();
		case 9: test9_JSToJSWebSocketSayHelloComplex();
		case 10: test10_JavaToCPPDirectSayHello();
		case 11: test11_JavaToCPPPollSayHello();
		case 12: test12_CPPToJavaDirectSayHello();
		case 13: test14_CPPToJavaPollSayHello();
		case 14: test13_CPPToJSWebSocketSayHello();
		}
	}
	
	private void resetTests() {
		lastTest = 0;
	}
	
	private void nextTest() {
		if (lastTest == nTests) {
			return;
		}
		lastTest++;
		runTest(lastTest);
	}
	
	protected void test1_JSToJavaDirectSayHello() {
		ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
		javaServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(javaServicePresenter, "sayHello").callButtonClick(r -> {
			sendResult("test1_JSToJavaDirectSayHello", r.equals("Hello from Java, John! n=10, f=3.14, b=true"));
		});
	}

	protected void test2_JSToJavaDirectSayHelloComplex() {
		ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
		javaServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(javaServicePresenter, "sayHelloComplex").callButtonClick(r -> {
			sendResult("test2_JSToJavaDirectSayHelloComplex", r.equals("{\"a\":5,\"b\":\"A=5\"}"));
		});
	}

	protected void test3_JSToCPPDirectSayHello() {
		ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
		cppServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(cppServicePresenter, "sayHello").callButtonClick(r -> {
			sendResult("test3_JSToCPPDirectSayHello", r.equals("Hello from Arduino, John! n=10 f=3.14 b=1"));
		});
	}

	protected void test4_JSToJavaPushSayHello() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + RemoteObjectHubConnection.HUB_MODE_WEB_SOCKET);
		hubConnectButtonClick(() -> {
			ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
			javaServicePresenter.createRoDirectButtonClick(null);
			getFunctionPresenter(javaServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PUSH);
			getFunctionPresenter(javaServicePresenter, "connectToHub").callButtonClick(r -> {
				javaServicePresenter.createRoHubButtonClick(null);
				getFunctionPresenter(javaServicePresenter, "sayHello").callButtonClick(s -> {
					sendResult("test4_JSToJavaPushSayHello", s.equals("Hello from Java, John! n=10, f=3.14, b=true"));
				});
			});
		});
	}

	protected void test5_JSToJavaPollSayHello() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + RemoteObjectHubConnection.HUB_MODE_HTTP_PULL);
		hubConnectButtonClick(() -> {
			ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
			javaServicePresenter.createRoDirectButtonClick(null);
			getFunctionPresenter(javaServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
			getFunctionPresenter(javaServicePresenter, "connectToHub").setValue("pollingInterval", "1000");
			getFunctionPresenter(javaServicePresenter, "connectToHub").callButtonClick(r -> {
				javaServicePresenter.createRoHubButtonClick(null);
				getFunctionPresenter(javaServicePresenter, "sayHello").callButtonClick(s -> {
					sendResult("test5_JSToJavaPollSayHello", s.equals("Hello from Java, John! n=10, f=3.14, b=true"));
				});
			});
		});
	}

	protected void test6_JSToCPPPushSayHello() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + RemoteObjectHubConnection.HUB_MODE_WEB_SOCKET);
		hubConnectButtonClick(() -> {
			ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
			cppServicePresenter.createRoDirectButtonClick(null);
			getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PUSH);
			getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("pollingInterval", "0");
			getFunctionPresenter(cppServicePresenter, "connectToHub").callButtonClick(r -> {
				new Timer() {
					@Override
					public void run() {
						cppServicePresenter.createRoHubButtonClick(null);
						getFunctionPresenter(cppServicePresenter, "sayHello").callButtonClick(s -> {
							sendResult("test6_JSToCPPPushSayHello", s.equals("Hello from Arduino, John! n=10 f=3.14 b=1"));
						});
					}
				}.schedule(1000); // wait for Arduino to connect to hub
			});
		});
		
	}

	protected void test7_JSToCPPPollSayHello() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + HUB_MODE_WEB_SOCKET);
		hubConnectButtonClick(() -> {
			ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
			cppServicePresenter.createRoDirectButtonClick(null);
			getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
			getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("pollingInterval", "1000");
			getFunctionPresenter(cppServicePresenter, "connectToHub").callButtonClick(r -> {
				new Timer() {
					@Override
					public void run() {
						cppServicePresenter.createRoHubButtonClick(null);
						getFunctionPresenter(cppServicePresenter, "sayHello").callButtonClick(s -> {
							sendResult("test7_JSToCPPPollSayHello", s.equals("Hello from Arduino, John! n=10 f=3.14 b=1"));
						});
					}
				}.schedule(1000); // wait for Arduino to connect to hub
			});
		});
	}

	protected void test8_JSToJSPullSayHello() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + HUB_MODE_HTTP_PULL);
		hubConnectButtonClick(() -> {
			ServicePresenter jsServicePresenter = getServicePresenter(JS_SERVICE_PRESENTER);
//			jsServicePresenter.setConnectionParam("nodeId", "JS_Node2");
			jsServicePresenter.createRoHubButtonClick(null);
			getFunctionPresenter(jsServicePresenter, "sayHello").callButtonClick(r -> {
				sendResult("test8_JSToJSPullSayHello", r.equals("Hello from JS, John! n=10, f=3.14, b=true"));
			});
		});
	}

	protected void test9_JSToJSWebSocketSayHelloComplex() {
		// connect JS node (this UI) to hub
		setHubParam("mode", "" + HUB_MODE_WEB_SOCKET);
		hubConnectButtonClick(() -> {
			ServicePresenter jsServicePresenter = getServicePresenter(JS_SERVICE_PRESENTER);
//			jsServicePresenter.setConnectionParam("nodeId", "JS_Node2");
			jsServicePresenter.createRoHubButtonClick(null);
			getFunctionPresenter(jsServicePresenter, "sayHelloComplex").callButtonClick(r -> {
				sendResult("test9_JSToJSWebSocketSayHelloComplex", r.equals("{\"a\":5,\"b\":\"A=5\"}"));
			});
		});
	}

	protected void test10_JavaToCPPDirectSayHello() {
		ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
		javaServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(javaServicePresenter, "initRemoteObject1").callButtonClick(r -> {
			getFunctionPresenter(javaServicePresenter, "callSayHelloRo1").callButtonClick(s -> {
				sendResult("test10_JavaToCPPDirectSayHello", s.equals("Hello from Arduino, Joe! n=2 f=5.23 b=1"));
			});
		});
	}
	
	protected void test11_JavaToCPPPollSayHello() {
		ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
		ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
		javaServicePresenter.createRoDirectButtonClick(null);
		cppServicePresenter.createRoDirectButtonClick(null);
		
		getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
		getFunctionPresenter(javaServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
		getFunctionPresenter(cppServicePresenter, "connectToHub").callButtonClick(a -> {
			new Timer() {
				@Override
				public void run() {
					getFunctionPresenter(javaServicePresenter, "connectToHub").callButtonClick(b -> {
						getFunctionPresenter(javaServicePresenter, "initRemoteObject2").callButtonClick(c -> {
							getFunctionPresenter(javaServicePresenter, "callSayHelloRo2").callButtonClick(r -> {
								sendResult("test11_JavaToCPPPollSayHello", r.equals("Hello from Arduino, Joe! n=2 f=5.23 b=1"));
							});
						});
					});
				}
			}.schedule(1000); // wait for Arduino to connect to hub
		});
	}

	protected void test12_CPPToJavaDirectSayHello() {
		ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
		cppServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(cppServicePresenter, "initRemoteObject1").callButtonClick(r -> {
			getFunctionPresenter(cppServicePresenter, "callSayHelloRo1").callButtonClick(s -> {
				sendResult("test12_CPPToJavaDirectSayHello", s.equals("Hello from Java, Joe! n=5, f=8.5, b=true"));
			});
		});
	}

	protected void test13_CPPToJSWebSocketSayHello() {
		ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
		cppServicePresenter.createRoDirectButtonClick(null);
		getFunctionPresenter(cppServicePresenter, "initRemoteObject3").callButtonClick(a -> {
			setHubParam("mode", "" + RemoteObjectHubConnection.HUB_MODE_WEB_SOCKET);
			setHubParam("remoteAddress", arduinoIpAddress + ":9005");
			hubConnectButtonClick(() -> {
				getFunctionPresenter(cppServicePresenter, "wsNotify").callButtonClick(r -> {
					// result is sent from JS service
				});
			});
		});
		
	}
	
	protected void test14_CPPToJavaPollSayHello() {
		ServicePresenter javaServicePresenter = getServicePresenter(JAVA_SERVICE_PRESENTER);
		ServicePresenter cppServicePresenter = getServicePresenter(CPP_SERVICE_PRESENTER);
		javaServicePresenter.createRoDirectButtonClick(null);
		cppServicePresenter.createRoDirectButtonClick(null);
		
		getFunctionPresenter(cppServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
		getFunctionPresenter(javaServicePresenter, "connectToHub").setValue("mode", "" + HUB_MODE_HTTP_PULL);
		getFunctionPresenter(cppServicePresenter, "connectToHub").callButtonClick(a -> {
			new Timer() {
				@Override
				public void run() {
					getFunctionPresenter(javaServicePresenter, "connectToHub").callButtonClick(b -> {
						getFunctionPresenter(cppServicePresenter, "initRemoteObject2").callButtonClick(c -> {
							getFunctionPresenter(cppServicePresenter, "callSayHelloRo2").callButtonClick(r -> {
								// result will be sent by the CPP (e.g. Arduino) device
							});
						});
					});
				}
			}.schedule(1000); // wait for Arduino to connect to hub
		});
	}

}
