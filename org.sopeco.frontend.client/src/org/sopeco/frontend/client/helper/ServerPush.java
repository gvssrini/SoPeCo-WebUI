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
package org.sopeco.frontend.client.helper;

import java.util.List;
import java.util.logging.Logger;

import org.sopeco.frontend.client.layout.MainLayoutPanel;
import org.sopeco.frontend.client.layout.popups.Message;
import org.sopeco.frontend.client.rpc.PushRPC;
import org.sopeco.frontend.client.rpc.PushRPC.Type;
import org.sopeco.frontend.client.rpc.PushRPCAsync;
import org.sopeco.frontend.shared.entities.RunningControllerStatus;
import org.sopeco.frontend.shared.entities.FrontendScheduledExperiment;
import org.sopeco.frontend.shared.push.PushListPackage;
import org.sopeco.frontend.shared.push.PushObjectPackage;
import org.sopeco.frontend.shared.push.PushPackage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Marius Oehler
 * 
 */
public final class ServerPush implements AsyncCallback<PushPackage> {

	private static final Logger LOGGER = Logger.getLogger(ServerPush.class.getName());
	
	private static ServerPush push;
	private static PushRPCAsync pushRPC = GWT.create(PushRPC.class);
	private boolean waiting = false;

	public static ServerPush get() {
		if (push == null) {
			push = new ServerPush();
		}
		return push;
	}

	private ServerPush() {
	}

	@Override
	public void onFailure(Throwable caught) {
		waiting = false;
		LOGGER.severe("ServerPush failed..");
		startRequest();
	}

	@Override
	public void onSuccess(PushPackage result) {
		waiting = false;
		execute(result);
		startRequest();
	}

	public void startRequest() {
		synchronized (pushRPC) {
			if (waiting) {
				return;
			} else {
				waiting = true;
			}
		}
		pushRPC.push(this);
	}

	private void execute(PushPackage pushPackage) {
		if (pushPackage.getType() == Type.PUSH_SCHEDULED_EXPERIMENT) {
			PushListPackage inPackage = (PushListPackage) pushPackage;
			List<FrontendScheduledExperiment> list = inPackage.getAttachment(FrontendScheduledExperiment.class);
			MainLayoutPanel.get().getExecuteController().getTabControllerTwo().setScheduledExperiments(list);
		} else if (pushPackage.getType() == Type.PUSH_CURRENT_CONTROLLER_EXPERIMENT) {
			PushObjectPackage inPackage = (PushObjectPackage) pushPackage;
			RunningControllerStatus ccExperiment = inPackage.getAttachment(RunningControllerStatus.class);
			MainLayoutPanel.get().getExecuteController().getTabControllerThree()
					.setCurrentControllerExperiment(ccExperiment);
		} else if (pushPackage.getType() == Type.PUSH_CURRENT_CONTROLLER_QUEUE) {
			PushListPackage inPackage = (PushListPackage) pushPackage;
			List<FrontendScheduledExperiment> list = inPackage.getAttachment(FrontendScheduledExperiment.class);
			MainLayoutPanel.get().getExecuteController().getTabControllerThree().setControllerQueue(list);
		}
	}

}
