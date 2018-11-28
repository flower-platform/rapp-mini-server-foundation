package com.crispico.flower_platform.remote_object.samples.client.page.main.service;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.properties_form.PropertiesFormPWidget;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionPresenter;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter.MyView;
import com.crispico.foundation.client.form.MapPropertyAccessorCommitter;
import com.crispico.foundation.client.form.PropertyChangedEvent;
import com.crispico.foundation.client.view.FoundationView;
import com.google.gwt.core.client.JavaScriptObject;
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
		
		void hideDirectROButton();
		
	}

    @Inject
    protected Provider<FunctionPresenter> functionProvider;
    
    @Inject
    protected PropertiesFormPWidget connectionParamsForm;
    
    protected Map<String, String> connectionParams;
    
    protected final SingleSlot<PresenterWidget<?>> SLOT_CONNECTION_PARAMS = addNamedSlot(new SingleSlot<>(), "SLOT_CONNECTION_PARAMS");
    
    protected JavaScriptObject remoteObject;
    
	public JavaScriptObject getRemoteObject() {
		return remoteObject;
	}

	@Inject
	protected ServicePresenter(EventBus eventBus, Provider<ServiceView> viewProvider) {
		super(eventBus, viewProvider);
	}
	
	@Inject
	protected void postCreate() {
		addNamedSlot(ClientGlobals.getDefaultMultiSlot(), "SLOT_FUNCTIONS");
		addVisibleHandlerToSource(PropertyChangedEvent.getType(), connectionParamsForm, e -> { 
			if ("nodeId".equals(e.getPropertyEditor().getPropertyDescriptor().getName())) {
				updateServiceName();
			}
		});
	}
	
	protected void updateServiceName() {
		getView().setServiceName(connectionParams.get("nodeId"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initConnectionParams(Map value) {
		connectionParams = value;
		connectionParamsForm.setInline(true);
		connectionParamsForm.setPropertyAccessorCommitter(new MapPropertyAccessorCommitter());
		connectionParamsForm.setPropertyDescriptors(connectionParams.keySet().stream().map(key -> new PropertyDescriptor().setName(key)).collect(Collectors.toList()));
		connectionParamsForm.setModel(connectionParams);
		setInSlot(SLOT_CONNECTION_PARAMS, connectionParamsForm);
		updateServiceName();
	}
	
	public FunctionPresenter addFunction() {
		FunctionPresenter f = functionProvider.get();
		addToSlot(ClientGlobals.getDefaultMultiSlot(), f);
		return f;
	}

}