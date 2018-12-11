package com.crispico.flower_platform.remote_object.samples.client.page.main.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.properties_form.PropertiesFormPWidget;
import com.crispico.client.component.properties_form.PropertyBasicDescriptor;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter.MyView;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter;
import com.crispico.foundation.client.component.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.view.FoundationView;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.FoundationComponentPresenter;

/**
 * @author Cristian Spiescu
 */
public class FunctionPresenter extends FoundationComponentPresenter<MyView> {

    public static interface MyView extends FoundationView {

		void setFunctionName(String value);

		void clearResult();
		
		String getResult();
		
		void callFunction(JavaScriptObject remoteObject, String functionName, JavaScriptObject params, Consumer<Object> successCallback, Consumer<Object> errorCallback);
		
	}

    @Inject
    protected PropertiesFormPWidget form;
    
    protected Map<String, String> values = new HashMap<>();
    
    protected String functionName;
    

	@Inject
	protected FunctionPresenter(EventBus eventBus, Provider<FunctionView> viewProvider) {
		super(eventBus, viewProvider);
	}
	
	@Inject
	protected void postCreate() {
		addNamedSlot(ClientGlobals.getDefaultSingleSlot(), "SLOT_MAIN");
		form.setPropertyAccessorCommitter(new MapPropertyAccessorCommitter());
		form.setInline(true);
		setInSlot(ClientGlobals.getDefaultSingleSlot(), form);
	}

	public void initParams(List<PropertyDescriptor> propertyDescriptors) {
		form.setPropertyDescriptors(propertyDescriptors);
		form.setModel(values);
	}
	
	public void initParams(String... params) {
		initParams(build(params));
	}

	public List<PropertyDescriptor> build(String... keyAndValues) {
		List<PropertyDescriptor> result = new ArrayList<>();
		for (int i = 0; i < keyAndValues.length; i = i + 2) {
			result.add(new PropertyDescriptor().setName(keyAndValues[i]));
			values.put(keyAndValues[i], keyAndValues[i+1]);
		}
		return result;
	}
	
	public void setFunctionName(String value) {
		this.functionName = value;
		getView().setFunctionName(value);
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setValue(String key, String value) {
		values.put(key, value);
		form.setModel(values);
	}
	
	public String getResult() {
		return getView().getResult();
	}
	
	public void callButtonClick(Consumer<Object> successCallback, Consumer<Object> errorCallback) {
		getView().clearResult();
		JavaScriptObject a = JavaScriptObject.createArray();
		if (form.getPropertyDescriptors() != null) {
			for (PropertyBasicDescriptor pd : form.getPropertyDescriptors()) {
				pushToArray(a, values.get(((PropertyDescriptor) pd).getName()));
			}
		}
		getView().callFunction(this.<ServicePresenter>getParent().getRemoteObject(), functionName, a, successCallback, errorCallback);
	}

	private native void pushToArray(JavaScriptObject a, Object value) /*-{
		a.push(value);
	}-*/;
	
}
