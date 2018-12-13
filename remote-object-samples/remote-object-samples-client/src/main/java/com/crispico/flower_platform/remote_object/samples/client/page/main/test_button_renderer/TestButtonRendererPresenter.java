package com.crispico.flower_platform.remote_object.samples.client.page.main.test_button_renderer;

import javax.inject.Inject;
import javax.inject.Provider;

import com.crispico.client.ClientGlobals;
import com.crispico.client.component.BasicModelPWidget;
import com.crispico.foundation.client.view.FoundationView;
import com.crispico.shared.util.Pair;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.web.bindery.event.shared.EventBus;

/**
 * @author Cristian Spiescu
 */
public class TestButtonRendererPresenter extends BasicModelPWidget {

    public static interface MyView extends FoundationView {

		void setLabel(String value);
	}

	@Inject
	protected TestButtonRendererPresenter(EventBus eventBus, Provider<TestButtonRendererView> viewProvider) {
		super(eventBus, viewProvider);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Pair<String, Runnable> getModel() {
		return (Pair<String, Runnable>) super.getModel();
	}

	@Inject
	protected void postCreate() {
		addNamedSlot(ClientGlobals.getDefaultSingleSlot(), "SLOT_MAIN");
	}
		
	protected void onButtonClick(ClickEvent clickEvent) {
		getModel().b.run();
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		((MyView) getView()).setLabel(getModel().a);
	}
}