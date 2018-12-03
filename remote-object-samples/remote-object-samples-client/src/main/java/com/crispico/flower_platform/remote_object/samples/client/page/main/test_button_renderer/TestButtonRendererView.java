package com.crispico.flower_platform.remote_object.samples.client.page.main.test_button_renderer;

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
public class TestButtonRendererView extends EventRedispatchingViewImpl<TestButtonRendererPresenter> implements TestButtonRendererPresenter.MyView {

	interface Binder extends UiBinder<Widget, TestButtonRendererView> {
	}

	@UiField
	protected Button button;

	@Inject
	protected TestButtonRendererView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		button.addClickHandler(getPresenter()::onButtonClick);
	}
	
	@Override
	public void setLabel(String value) {
		button.setText(value);
	}
	
}