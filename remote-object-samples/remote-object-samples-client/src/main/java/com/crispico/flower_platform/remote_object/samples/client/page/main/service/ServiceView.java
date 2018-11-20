package com.crispico.flower_platform.remote_object.samples.client.page.main.service;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * @author Cristian Spiescu
 */
public class ServiceView extends EventRedispatchingViewImpl<ServicePresenter> implements ServicePresenter.MyView {

	interface Binder extends UiBinder<Widget, ServiceView> {
	}

	@UiField
	protected Button refreshButton;

	@UiField
	protected Label serviceName;

	@Inject
	protected ServiceView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		refreshButton.addClickHandler(e -> {
			JSONObject o = new JSONObject();
			getPresenter().connectionParams.forEach((k, v) -> o.put(k, new JSONString((String) v)));
			getPresenter().remoteObject = createRemoteObject(o.getJavaScriptObject());
		});
	}

	protected native JavaScriptObject createRemoteObject(JavaScriptObject connectionParams) /*-{
		var roi = new $wnd.rapp_mini_server.JsRemoteObjectBase();
		return roi.initialize(new $wnd.rapp_mini_server.RemoteObject()
			.setRemoteAddress(connectionParams.remoteAddress)
			.setSecurityToken(connectionParams.securityToken)
			.setNodeId(connectionParams.nodeId)
			.setInstanceName(connectionParams.instanceName)
		);
	}-*/;
	
	@Override
	public void setServiceName(String name) {
		serviceName.setText(name);
	}
}