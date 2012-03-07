package org.krakenapps.event.api;

import java.util.HashMap;

public class EventPredicate {
	public enum Operator {
		Equal("eq"), Not("neq"), Like("like"), Between("between"), Gt("gt"), Lt("lt"), Ge("ge"), Le("le");
		String mnemonic;

		private Operator(String mnemonic) {
			this.mnemonic = mnemonic;
		}

		public String getMnemonic() {
			return mnemonic;
		}

		private static HashMap<String, Operator> mnemonicMap = new HashMap<String, Operator>();
		static {
			mnemonicMap.put("eq", Equal);
			mnemonicMap.put("neq", Not);
			mnemonicMap.put("like", Like);
			mnemonicMap.put("between", Between);
			mnemonicMap.put("gt", Gt);
			mnemonicMap.put("lt", Lt);
			mnemonicMap.put("ge", Ge);
			mnemonicMap.put("le", Le);
		}

		public static Operator valueOfMnemonic(String mnemonic) {
			return mnemonicMap.get(mnemonic);
		}
	}

	private String name;

	private Operator operator;

	private Object field1;

	private Object field2;

	public EventPredicate(String name, Operator operator, Object field1, Object field2) {
		this.name = name;
		this.operator = operator;
		this.field1 = field1;
		this.field2 = field2;
	}

	public String getName() {
		return name;
	}

	public Operator getOperator() {
		return operator;
	}

	public Object getField1() {
		return field1;
	}

	public Object getField2() {
		return field2;
	}
}