/**
 * Copyright (c) 2013 SAP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the SAP nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SAP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sopeco.frontend.client.layout.center.experiment;

import org.sopeco.frontend.client.event.EventControl;
import org.sopeco.frontend.client.event.ExperimentChangedEvent;
import org.sopeco.frontend.client.event.handler.ExperimentChangedEventHandler;
import org.sopeco.frontend.client.layout.center.experiment.TerminationView.Condition;
import org.sopeco.frontend.client.manager.ScenarioManager;
import org.sopeco.persistence.entities.definition.ExperimentTerminationCondition;
import org.sopeco.persistence.entities.definition.MeasurementEnvironmentDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * 
 * @author Marius Oehler
 * 
 */
public class TerminationController implements ValueChangeHandler<Boolean> {

	private TerminationView view;

	public TerminationController() {
		view = new TerminationView();

		// ExperimentTerminationCondition t = new
		// ExperimentTerminationCondition("test 1", "desc 1");
		// ExperimentTerminationCondition t2 = new
		// ExperimentTerminationCondition("test 2", "desc 2");
		//
		// t.addParameter("para1", "1");
		// t.addParameter("para2", "2");
		// t.addParameter("para3", "3");
		//
		// t2.addParameter("para1", "1");
		// t2.addParameter("para2", "2");
		//
		// view.addCondition(t);
		// view.addCondition(t2);

		EventControl.get().addHandler(ExperimentChangedEvent.TYPE, new ExperimentChangedEventHandler() {
			@Override
			public void onExperimentChanged(ExperimentChangedEvent event) {
				updateConditions();
			}
		});

	}

	public TerminationView getView() {
		return view;
	}

	/**
	 * 
	 */
	private void updateConditions() {
		view.clear();

		MeasurementEnvironmentDefinition med = ScenarioManager.get().getCurrentScenarioDefinition()
				.getMeasurementEnvironmentDefinition();

		for (ExperimentTerminationCondition termination : med.getSupportedTerminationConditions()) {
			Condition addedCondition = view.addCondition(termination);

			if (ScenarioManager.get().experiment().isSetTermination(termination)) {
				addedCondition.setConfiguration(ScenarioManager.get().experiment().getTerminationCondition(termination)
						.getParametersValues());
				
				addedCondition.setConditionVisibility(true);
			}
		}

		addCheckboxHandler();
	}

	/**
	 * 
	 */
	private void addCheckboxHandler() {
		for (Condition condition : view.getConditionMap().values()) {
			condition.addValueChangeHandler(this);
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		Condition source = (Condition) event.getSource();
		source.setConditionVisibility(event.getValue());

		if (event.getValue()) {
			GWT.log("user con: " + source.getCondition().getName());
			GWT.log("    conf: " + source.getConfig());
			ScenarioManager.get().experiment().addTermination(source.getCondition());
		} else {
			GWT.log("remove con: " + source.getCondition().getName());
			ScenarioManager.get().experiment().removeTermination(source.getCondition());
		}
	}
}
