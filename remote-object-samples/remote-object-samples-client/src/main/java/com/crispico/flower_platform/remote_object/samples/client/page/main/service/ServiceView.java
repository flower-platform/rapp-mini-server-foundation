package com.crispico.flower_platform.remote_object.samples.client.page.main.service;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
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
		createRoDirectButton.addClickHandler(getPresenter()::createRoDirectButtonClick);
		createRoHubButton.addClickHandler(getPresenter()::createRoHubButtonClick);
	}
	
	@Override
	public void setServiceName(String name) {
		serviceName.setText(name);
	}

	@Override
	public void hideDirectROButton() {
		createRoDirectButton.setVisible(false);
	}
	
}