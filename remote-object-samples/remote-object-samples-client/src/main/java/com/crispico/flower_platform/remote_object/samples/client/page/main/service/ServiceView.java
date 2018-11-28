package com.crispico.flower_platform.remote_object.samples.client.page.main.service;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
import com.crispico.flower_platform.remote_object.samples.client.page.main.MainPagePresenter;
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
	protected Button createRoDirectButton;

	@UiField
	protected Button createRoHubButton;

	@UiField
	protected Label serviceName;

	@Inject
	protected ServiceView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		createRoDirectButton.addClickHandler(e -> {
			JSONObject o = new JSONObject();
			getPresenter().connectionParams.forEach((k, v) -> o.put(k, new JSONString((String) v)));
			getPresenter().remoteObject = createRemoteObjectDirect(o.getJavaScriptObject());
		});
		createRoHubButton.addClickHandler(e -> {
			JSONObject connectionParams = new JSONObject();
			getPresenter().connectionParams.forEach((k, v) -> connectionParams.put(k, new JSONString((String) v)));
			JSONObject hubParams = new JSONObject();
			((MainPagePresenter)(getPresenter().getParent())).hubParams.forEach((k, v) -> hubParams.put(k, new JSONString((String) v)));
			getPresenter().remoteObject = createRemoteObjectHub(connectionParams.getJavaScriptObject(), hubParams.getJavaScriptObject());
		});
	}
	
	protected native JavaScriptObject createRemoteObjectHub(JavaScriptObject connectionParams, JavaScriptObject hubParams) /*-{
		var roi = new $wnd.rapp_mini_server.JsRemoteObjectBase();
		return roi.initialize(new $wnd.rapp_mini_server.RemoteObject()
			.setRemoteAddress(hubParams.remoteAddress)
			.setSecurityToken(hubParams.securityToken)
			.setInstanceName(connectionParams.instanceName)
			.setNodeId(connectionParams.nodeId)
		);
	}-*/;
	
	protected native JavaScriptObject createRemoteObjectDirect(JavaScriptObject connectionParams) /*-{
		var roi = new $wnd.rapp_mini_server.JsRemoteObjectBase();
		return roi.initialize(new $wnd.rapp_mini_server.RemoteObject()
			.setRemoteAddress(connectionParams.remoteAddress)
			.setSecurityToken(connectionParams.securityToken)
			.setInstanceName(connectionParams.instanceName)
		);
	}-*/;
	
	@Override
	public void setServiceName(String name) {
		serviceName.setText(name);
	}

	@Override
	public void hideDirectROButton() {
		createRoDirectButton.setVisible(false);
	}
	
}