package org.sopeco.frontend.client.layout.center.execute.tabTwo;

import java.util.Map;
import java.util.TreeMap;

import org.sopeco.frontend.client.layout.MainLayoutPanel;
import org.sopeco.frontend.client.model.Manager;
import org.sopeco.frontend.shared.entities.RawScheduledExperiment;
import org.sopeco.frontend.shared.helper.Metering;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * 
 * @author Marius Oehler
 * 
 */
public class ScheduleTab extends FlowPanel {
	public ScheduleTab() {
		init();
	}

	private void init() {
		getElement().getStyle().setOverflowY(Overflow.AUTO);
	}

	public void refreshList() {
		double metering = Metering.start();
		clear();

		Map<Long, ScheduleItemPanel> scheduledItems = new TreeMap<Long, ScheduleItemPanel>();

//		for (RawScheduledExperiment e : Manager.get().getCurrentScenarioDetails().getScheduledExperimentsList()) {
//			ScheduleItemPanel item = new ScheduleItemPanel(e);
//			// add(item);
//			// item.setParent(this);
//			//scheduledItems.put(e.getNextExecutionTime(), item);
//		}

		for (ScheduleItemPanel item : scheduledItems.values()) {
			add(item);
		}
		Metering.stop(metering);
	}

	public void removeExperiment(ScheduleItemPanel item) {
		RawScheduledExperiment exp = item.getExperiment();
//		Manager.get().getCurrentScenarioDetails().getScheduledExperimentsList().remove(exp);
//		Manager.get().storeAccountDetails();

		MainLayoutPanel.get().getExecuteController().refreshScheduleTab();
	}
}
