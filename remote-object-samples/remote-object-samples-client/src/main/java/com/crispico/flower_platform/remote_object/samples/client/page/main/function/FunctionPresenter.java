package com.crispico.flower_platform.remote_object.samples.client.page.main.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.properties_form.PropertiesFormPWidget;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter.MyView;
import com.crispico.foundation.client.component.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.view.FoundationView;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.FoundationComponentPresenter;

/**
 * @author Cristian Spiescu
 */
public class FunctionPresenter extends FoundationComponentPresenter<MyView> {

    public static interface MyView extends FoundationView {

		void setFunctionName(String value);
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

}
