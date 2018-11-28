package com.crispico.flower_platform.remote_object.samples.client;

import jsinterop.annotations.JsType;

@JsType(namespace="js", name="sampleService")
public class SampleJsAppService {

	public static native String sayHello(String name, int n, float f, boolean b) /*-{
		var s = "Hello from JS, " + name + "! n=" + n + ", f=" + f + ", b=" + b;
		console.log(s);
		return s;
	}-*/;

	public static native String sayHelloComplex(String complexObjectJson) /*-{
		var o = JSON.parse(complexObjectJson);
		console.log(o);
		o.b = "A=" + o.a;
		return JSON.stringify(o);
	}-*/;
	
}
