package com.crispico.flower_platform.remote_object.samples.client.page.main.function;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
import com.crispico.client.component.properties_form.PropertyBasicDescriptor;
import com.crispico.client.component.properties_form.PropertyDescriptor;
import com.crispico.flower_platform.remote_object.samples.client.page.main.service.ServicePresenter;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
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

	@UiField
	protected TextBox result;
	
	@Inject
	protected FunctionView(Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));	
	}

	@Override
	public void setPresenter(PresenterWidget<?> page) {
		super.setPresenter(page);
		callButton.addClickHandler(e -> {
			this.result.setText("");
			JavaScriptObject a = JavaScriptObject.createArray();
			for (PropertyBasicDescriptor pd : getPresenter().form.getPropertyDescriptors()) {
				pushToArray(a, getPresenter().values.get(((PropertyDescriptor) pd).getName()));
			}
			callFunction(getPresenter().<ServicePresenter>getParent().getRemoteObject(), getPresenter().functionName, a);
		});
	}
	
	private native void pushToArray(JavaScriptObject a, Object value) /*-{
		a.push(value);
	}-*/;

	
	@Override
	public void setFunctionName(String value) {
		callButton.setText(value);
	}
	
	protected native void callFunction(JavaScriptObject remoteObject, String functionName, JavaScriptObject params) /*-{
		that = this;
		params.push(function (result) { that.@com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionView::onResult(Ljava/lang/String;)(result); }); 
		remoteObject[functionName].apply(remoteObject, params);
	}-*/;

	protected void onResult(String result) {
		this.result.setText(result);
	}

}