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
package org.sopeco.webui.client.layout.center.execute.tabFour;

import org.sopeco.service.persistence.entities.ExecutedExperimentDetails;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

/**
 * 
 * @author Marius Oehler
 * 
 */
public class TabView extends FlowPanel {

	private DataGrid<ExecutedExperimentDetails> dataGrid;
	private FlowPanel detailPanel;

	private SplitLayoutPanel splitLayout;

	public TabView() {
		detailPanel = new FlowPanel();
		detailPanel.addStyleName("historyDetails");

		splitLayout = new SplitLayoutPanel() {
			@Override
			public void onResize() {
				super.onResize();
				dataGrid.onResize();
			}
		};
		splitLayout.setHeight("100%");
		splitLayout.add(detailPanel);

		add(splitLayout);
	}

	public void setDataGrid(DataGrid<ExecutedExperimentDetails> pDataGrid) {
		dataGrid = pDataGrid;

		splitLayout.clear();
		splitLayout.addNorth(dataGrid, 200);
		splitLayout.add(detailPanel);
	}

	public DataGrid<ExecutedExperimentDetails> getDataGrid() {
		return dataGrid;
	}

	public FlowPanel getDetailPanel() {
		return detailPanel;
	}
}
