package org.sopeco.frontend.client.rpc;

import org.sopeco.frontend.shared.entities.RawScheduledExperiment;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Marius Oehler
 * 
 */
public interface ExecuteRPCAsync {
	void execute(String url, AsyncCallback<Void> callback);

	void isRunning(String url, AsyncCallback<Long> callback);

	void stop(String url, AsyncCallback<Void> callback);

	void scheduleExperiment(RawScheduledExperiment rawScheduledExperiment, AsyncCallback<Void> callback);
}
