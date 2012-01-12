package org.krakenapps.webfx;

import java.util.HashMap;
import java.util.Map;

public class ControllerAction {
	private String controller;
	private String action;
	private Map<String, String> params = new HashMap<String, String>();

	public ControllerAction() {
	}

	public ControllerAction(String controller, String action) {
		this.controller = controller;
		this.action = action;
	}

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "controller=" + controller + ", action=" + action + ", params=" + params;
	}
}
