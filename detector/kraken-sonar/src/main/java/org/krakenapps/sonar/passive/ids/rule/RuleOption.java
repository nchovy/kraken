package org.krakenapps.sonar.passive.ids.rule;

public class RuleOption {
	private String name;
	private String value;

	public RuleOption(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + "=" + value;
	}
}
