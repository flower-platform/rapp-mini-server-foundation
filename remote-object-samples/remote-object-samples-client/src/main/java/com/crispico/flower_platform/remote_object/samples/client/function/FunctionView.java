package com.crispico.flower_platform.remote_object.samples.client.function;

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
public class FunctionView extends EventRedispatchingViewImpl<FunctionPresenter> implements FunctionPresenter.MyView {

	interface Binder extends UiBinder<Widget, FunctionView> {
	}

	@UiField
	protected Button callButton;

	@Inject
	protected FunctionView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		callButton.addClickHandler(getPresenter()::onCallButtonClick);
	}
	
	@Override
	public void setFunctionName(String value) {
		callButton.setText(value);
	}
}