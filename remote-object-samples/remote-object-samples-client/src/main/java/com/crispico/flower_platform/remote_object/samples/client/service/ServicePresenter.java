package com.crispico.flower_platform.remote_object.samples.client.service;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.alert.Alert;
import com.crispico.client.component.properties_form.PropertiesFormPWidget;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.flower_platform.remote_object.samples.client.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.service.ServicePresenter.MyView;
import com.crispico.foundation.client.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.view.FoundationView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.FoundationComponentPresenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;

/**
 * @author Cristian Spiescu
 */
public class ServicePresenter extends FoundationComponentPresenter<MyView> {

    public static interface MyView extends FoundationView {

		void setServiceName(String name);
	}

    @Inject
    protected Provider<FunctionPresenter> functionProvider;
    
    @Inject
    protected PropertiesFormPWidget connectionParamsForm;
    
    protected Map<String, Object> connectionParams;
    
    protected final SingleSlot<PresenterWidget<?>> SLOT_CONNECTION_PARAMS = addNamedSlot(new SingleSlot<>(), "SLOT_CONNECTION_PARAMS");
    
    protected String name;
    
	public void setName(String name) {
		this.name = name;
		getView().setServiceName(name);
	}

	@Inject
	protected ServicePresenter(EventBus eventBus, Provider<ServiceView> viewProvider) {
		super(eventBus, viewProvider);
	}
	
	@Inject
	protected void postCreate() {
		addNamedSlot(ClientGlobals.getDefaultMultiSlot(), "SLOT_FUNCTIONS");
	}
	
	public void initConnectionParams(Map<String, Object> value) {
		connectionParams = value;
		connectionParamsForm.setInline(true);
		connectionParamsForm.setPropertyAccessorCommitter(new MapPropertyAccessorCommitter());
		connectionParamsForm.setPropertyDescriptors(connectionParams.keySet().stream().map(key -> new PropertyDescriptor().setName(key)).collect(Collectors.toList()));
		connectionParamsForm.setModel(connectionParams);
		setInSlot(SLOT_CONNECTION_PARAMS, connectionParamsForm);
	}
		
	protected void onRefreshButtonClick(ClickEvent clickEvent) {
		// TODO replace with meaningful code
		Alert.show("Please replace this with some meaningful code!");
	}
	
	public FunctionPresenter addFunction() {
		FunctionPresenter f = functionProvider.get();
		addToSlot(ClientGlobals.getDefaultMultiSlot(), f);
		return f;
	}
}