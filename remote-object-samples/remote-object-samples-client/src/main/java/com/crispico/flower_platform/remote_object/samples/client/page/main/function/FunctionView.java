package com.crispico.flower_platform.remote_object.samples.client.page.main.function;

import java.util.function.Consumer;

import javax.inject.Inject;

import com.crispico.client.EventRedispatchingViewImpl;
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
		callButton.addClickHandler(e -> getPresenter().callButtonClick(null));
	}
	
	@Override
	public void setFunctionName(String value) {
		callButton.setText(value);
	}
	
	@Override
	public void clearResult() {
		this.result.setText("");
	}

	protected void onResult(String result) {
		this.result.setText(result);
	}

	protected void onError(Object error) {
		this.result.setText("ERROR");
	}

	@Override
	public String getResult() {
		return result.getValue();
	}

	public native void callFunction(JavaScriptObject remoteObject, String functionName, JavaScriptObject params, Consumer<Object> callback) /*-{
		that = this;
		params.push(function (result) { 
			that.@com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionView::onResult(Ljava/lang/String;)(result);
			if (callback != null) {
				callback.@java.util.function.Consumer::accept(*)(result);
			} 
		}); 
		params.push(function (error) { that.@com.crispico.flower_platform.remote_object.samples.client.page.main.function.FunctionView::onError(Ljava/lang/Object;)(error); }); 
		remoteObject[functionName].apply(remoteObject, params);
	}-*/;

}