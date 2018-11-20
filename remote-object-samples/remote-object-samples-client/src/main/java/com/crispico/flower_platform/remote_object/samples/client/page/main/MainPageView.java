package com.crispico.flower_platform.remote_object.samples.client.page.main;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Cristian Spiescu
 */
public class MainPageView extends EventRedispatchingViewImpl<MainPagePresenter> implements MainPagePresenter.MyView {

	interface Binder extends UiBinder<Widget, MainPageView> {
	}

	@Inject
	protected MainPageView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

}