package org.krakenapps.logdb.query.command;

import java.util.Map;

public class OptionChecker {
	private boolean bracket;
	private String lh1;
	private String rh1;
	private OptionChecker lh2;
	private boolean isAnd;
	private OptionChecker rh2;

	public OptionChecker(String lh, String rh) {
		this.bracket = true;
		this.lh1 = lh;
		this.rh1 = rh;
	}

	public OptionChecker(OptionChecker lh, boolean isAnd, OptionChecker rh) {
		this.lh2 = lh;
		this.isAnd = isAnd;
		this.rh2 = rh;
	}

	public boolean eval(Map<String, Object> m) {
		if (lh1 != null && rh1 != null)
			return rh1.equals(m.get(lh1));

		if (isAnd)
			return lh2.eval(m) & rh2.eval(m);
		else
			return lh2.eval(m) | rh2.eval(m);
	}

	public boolean isBracket() {
		return bracket;
	}

	public void setBracket(boolean bracket) {
		this.bracket = bracket;
	}

	public String getLh1() {
		return lh1;
	}

	public String getRh1() {
		return rh1;
	}

	public OptionChecker getLh2() {
		return lh2;
	}

	public boolean isAnd() {
		return isAnd;
	}

	public OptionChecker getRh2() {
		return rh2;
	}

	public boolean isHighPriority() {
		if (bracket)
			return true;
		if (lh1 != null && rh1 != null)
			return true;
		if (isAnd)
			return true;
		return false;
	}

	@Override
	public String toString() {
		if (lh1 != null && rh1 != null)
			return (lh1 + "=>" + rh1);
		else
			return ("(" + lh2 + (isAnd ? " & " : " | ") + rh2 + ")");
	}
}
