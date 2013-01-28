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
package org.sopeco.frontend.server.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.sopeco.engine.registry.ExtensionRegistry;
import org.sopeco.frontend.client.rpc.VisualizationRPC;
import org.sopeco.frontend.server.chartconnector.IChartConnection;
import org.sopeco.frontend.server.chartconnector.IChartConnectionExtension;
import org.sopeco.frontend.server.gcharts.GCharts;
import org.sopeco.frontend.server.persistence.UiPersistence;
import org.sopeco.frontend.server.user.User;
import org.sopeco.frontend.server.user.UserManager;
import org.sopeco.frontend.shared.definitions.result.SharedExperimentRuns;
import org.sopeco.frontend.shared.entities.ChartData;
import org.sopeco.frontend.shared.entities.ChartOptions;
import org.sopeco.frontend.shared.entities.ChartParameter;
import org.sopeco.frontend.shared.entities.Visualization;
import org.sopeco.frontend.shared.helper.AggregationInputType;
import org.sopeco.frontend.shared.helper.AggregationOutputType;
import org.sopeco.persistence.dataset.DataSetAggregated;
import org.sopeco.persistence.dataset.ParameterValue;
import org.sopeco.persistence.dataset.SimpleDataSet;
import org.sopeco.persistence.dataset.SimpleDataSetColumn;
import org.sopeco.persistence.dataset.SimpleDataSetRow;
import org.sopeco.persistence.dataset.SimpleDataSetRowBuilder;
import org.sopeco.persistence.entities.ExperimentSeries;
import org.sopeco.persistence.entities.ExperimentSeriesRun;
import org.sopeco.persistence.entities.ScenarioInstance;
import org.sopeco.persistence.entities.definition.ParameterDefinition;
import org.sopeco.persistence.entities.definition.ParameterRole;
import org.sopeco.persistence.exceptions.DataNotFoundException;

public class VisualizationRPCImpl extends SuperRemoteServlet implements
		VisualizationRPC {
	private IChartConnection chartCreator;
	private List<Visualization> visualizations;
	private boolean loaded = false;
	private List<IChartConnectionExtension> extensions;
	private static final String G_CHARTS = "Google Charts";

	public VisualizationRPCImpl() {
		chartCreator = new GCharts();
		visualizations = new ArrayList<Visualization>();
		extensions = (ExtensionRegistry.getSingleton()
				.getExtensions(IChartConnectionExtension.class)).getList();
	}

	@Override
	public Visualization createChart(SharedExperimentRuns experiementRun,
			List<ChartParameter> chartParameter, ChartOptions options, String extension) {
		ChartData data;
		loadExtension(extension);
		String scenarioName = experiementRun.getParentSeries()
				.getParentInstance().getScenarioName();
		String measurementEnvironmentUrl = experiementRun.getParentSeries()
				.getParentInstance().getControllerUrl();
		String experimentName = experiementRun.getParentSeries()
				.getExperimentName();
		Long timestamp = experiementRun.getTimestamp();
		String accountName = getUser().getCurrentAccount().getId();
		ExperimentSeriesRun run = getRun(scenarioName,
				measurementEnvironmentUrl, experimentName, timestamp);
		data = loadData(run, chartParameter);
		Visualization visualization = chartCreator.createVisualization(
				run.getLabel(), data, chartParameter, options);
		visualization.setScenarioName(scenarioName);
		visualization.setMeasurementEnvironmentUrl(measurementEnvironmentUrl);
		visualization.setExperimentName(experimentName);
		visualization.setTimestamp(timestamp);
		visualization.setAccountId(accountName);
		visualization.setId(System.currentTimeMillis());
		System.out.println("saving chart...");
		visualizations.add(visualization);
		UiPersistence.getUiProvider().storeVisualization(visualization);
		return visualization;

	}

	@Override
	public List<Visualization> getVisualizations(int start, int length) {
		if (!loaded) {
			String accountName = getUser().getCurrentAccount().getId();
			System.out.println("loading charts...");
			visualizations.addAll(UiPersistence.getUiProvider()
					.loadVisualizationsByAccount(accountName));
			loaded = true;
		}
		if (start > visualizations.size() - 1){
			return new ArrayList<Visualization>();
		}
		start = start < 0 ? 0 : start;
		length = start + length > visualizations.size() ? visualizations.size()-start : length;
		List<Visualization> vis = new ArrayList<Visualization>(visualizations.subList(start, start+length));
		for (Visualization visualization : vis) {
			if (visualization.getData() == null
					&& visualization.getType() == Visualization.Type.GCHART) {
				visualization.setData(loadData(getRun(visualization),
						visualization.getChartParameters()));
			}
		}
		return vis;
	}

	private ChartData loadData(ExperimentSeriesRun run,
			List<ChartParameter> chartParameter) {
		ChartData data;
		data = new ChartData();
		DataSetAggregated dataset = run.getSuccessfulResultDataSet();
		SimpleDataSet simpledata = dataset.convertToSimpleDataSet();
		Map<String, AggregationInputType> inParams = new HashMap<String, AggregationInputType>();
		Map<String, AggregationOutputType> outParams = new HashMap<String, AggregationOutputType>();
		for (ChartParameter cp : chartParameter){
			switch (cp.getType()) {
			case ChartParameter.INPUT:
				inParams.put(cp.getParameterName(), cp.getAggregationInputType());
				break;
			default:
				outParams.put(cp.getParameterName(), cp.getAggregationOutputType());
				break;
			}
		}

		String name;
		Map<String, List<Double>> datamap = new HashMap<String, List<Double>>();
		for (SimpleDataSetRow row : simpledata) {
			name = "";
			for (ParameterValue<?> value : row.getRowValues()){
				if(value.getParameter().getRole() == ParameterRole.INPUT){
					if (inParams.get(value.getParameter().getFullName()) != null){
						switch (inParams.get(value.getParameter().getFullName())){
						case SHOW:
							name += value.getValueAsString();
							break;
						case AGGREGATE:
							break;
						}
					}
				}
			}
			for (ParameterValue<?> value : row.getRowValues()){
				if(value.getParameter().getRole() == ParameterRole.OBSERVATION){
					String cname = value.getParameter().getFullName()+ " " +name;
					List<Double> list = datamap.get(cname);
					if (list == null){
						list = new ArrayList<Double>();
						datamap.put(cname, list);
					}
					if (outParams.get(value.getParameter().getFullName()) != null){
						switch (outParams.get(value.getParameter().getFullName())){
						case SCATTER:
							list.add(value.getValueAsDouble());
							break;
						case SUM:
							if (list.size() <= 0){
								list.add(0.0);
							}
							list.set(0, list.get(0)+value.getValueAsDouble());
							break;
						}
					}
				}
			}
			
		}
		data.setData(datamap);
		return data;
	}

	public ExperimentSeriesRun getRun(SharedExperimentRuns experiementRun) {
		return getRun(experiementRun.getParentSeries().getParentInstance()
				.getScenarioName(), experiementRun.getParentSeries()
				.getParentInstance().getControllerUrl(), experiementRun
				.getParentSeries().getExperimentName(),
				experiementRun.getTimestamp());
	}

	public ExperimentSeriesRun getRun(Visualization visualization) {
		return getRun(visualization.getScenarioName(),
				visualization.getMeasurementEnvironmentUrl(),
				visualization.getExperimentName(), visualization.getTimestamp());
	}

	public ExperimentSeriesRun getRun(String scenarioName,
			String measurementEnvironmentUrl, String experimentName,
			Long timestamp) {
		ScenarioInstance instance;
		try {
			User user = UserManager.getUser(getSessionId());
			if (user == null) {
				throw new DataNotFoundException("No user at session found..");
			}
			instance = user.getCurrentPersistenceProvider()
					.loadScenarioInstance(scenarioName,
							measurementEnvironmentUrl);
			ExperimentSeries series = instance
					.getExperimentSeries(experimentName);
			for (ExperimentSeriesRun r : series.getExperimentSeriesRuns()) {
				if (timestamp.equals(r.getTimestamp())) {
					return r;
				}
			}
		} catch (DataNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	public ChartParameter[] getChartParameter(
			SharedExperimentRuns experiementRun) {
		ExperimentSeriesRun run = getRun(experiementRun);
		DataSetAggregated dataset = run.getSuccessfulResultDataSet();
		List<ChartParameter> chartParameters = new ArrayList<ChartParameter>();
		for (ParameterDefinition pd : dataset.getParameterDefinitions()) {
			int role = 0;
			switch (pd.getRole()) {
			case OBSERVATION:
				role = ChartParameter.OBSERVATION;
				break;
			case INPUT:
				role = ChartParameter.INPUT;
				break;
			}
			ChartParameter chartParameter = new ChartParameter();
			chartParameter.setParameterName(pd.getFullName());
			chartParameter.setType(role);
			chartParameters.add(chartParameter);
		}
		return chartParameters.toArray(new ChartParameter[chartParameters
				.size()]);
	}

	@Override
	public Void deleteVisualization(Visualization visualization) {
		System.out.println("deleting chart...");
		visualizations.remove(visualization);
		UiPersistence.getUiProvider().removeVisualization(visualization);
		return null;
	}
	
	private void loadExtension(String name){
		if (name.equals(G_CHARTS)){
			chartCreator = new GCharts();
			return;
		}
		for (IChartConnectionExtension ex : extensions){
			if (ex.getName().equals(name)){
				chartCreator = ex.createExtensionArtifact();
				return;
			}
		}
	}

	@Override
	public List<String> getExtensions() {
		List<String> extensionNames = new ArrayList<String>();
		extensionNames.add(G_CHARTS);
		for (IChartConnectionExtension ex : extensions){
			extensionNames.add(ex.getName());
		}
		return extensionNames;
	}
}
