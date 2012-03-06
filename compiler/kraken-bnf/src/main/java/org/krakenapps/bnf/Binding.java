/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.bnf;

public class Binding {
	private Rule rule;
	private Binding[] children;
	private Object value;

	public Binding(Rule rule, Binding[] children) {
		this.rule = rule;
		this.children = children;
	}

	public Binding(Rule rule, Object value) {
		this.rule = rule;
		this.value = value;
	}

	public Rule getRule() {
		return rule;
	}
	
	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public Binding[] getChildren() {
		return children;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.format("[rule=%s, value=%s, children=%d]", rule, value, children != null ? children.length : 0);
	}

}
