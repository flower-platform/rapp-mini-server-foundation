package com.crispico.flower_platform.remote_object.samples.client.page.main;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * @author Cristian Spiescu
 */
public class MainPageView extends EventRedispatchingViewImpl<MainPagePresenter> implements MainPagePresenter.MyView {

	interface Binder extends UiBinder<Widget, MainPageView> {
	}

	@UiField
	protected Button connectButton;

	@UiField
	protected Button disconnectButton;
	
	@Inject
	protected MainPageView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		connectButton.addClickHandler(e -> getPresenter().hubConnectButtonClick(null, null));
		disconnectButton.addClickHandler(e -> getPresenter().hubDisconnectButtonClick());
	}

	
	@Override
	public void setHubconnectionType(int type) {
		
	}

	
	
}