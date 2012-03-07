package org.krakenapps.sqlengine.bdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.sqlparser.ast.ColumnConstraint;
import org.krakenapps.sqlparser.ast.ColumnConstraintDefinition;
import org.krakenapps.sqlparser.ast.NotNullConstraint;
import org.krakenapps.sqlparser.ast.PrimaryKeyConstraint;
import org.krakenapps.sqlparser.ast.ReferencesSpecification;
import org.krakenapps.sqlparser.ast.UniqueConstraint;

public class ConstraintSerializer {
	private ConstraintSerializer() {
	}

	public static Object serialize(List<ColumnConstraintDefinition> constraints) {
		List<Object> l = new ArrayList<Object>();
		for (ColumnConstraintDefinition def : constraints) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("type", serializeConstraint(def.getColumnConstraint()));
			l.add(m);
		}
		return l;
	}

	@SuppressWarnings("unchecked")
	public static List<ColumnConstraintDefinition> deserialize(Object[] arr) {
		List<ColumnConstraintDefinition> l = new ArrayList<ColumnConstraintDefinition>();
		for (Object o : arr) {
			Map<String, Object> m = (Map<String, Object>) o;
			ColumnConstraint constraint = deserializeConstraint((Object[]) m.get("type"));
			l.add(new ColumnConstraintDefinition(constraint));
		}

		return l;
	}

	private static Object[] serializeConstraint(ColumnConstraint constraint) {
		if (constraint instanceof NotNullConstraint)
			return new Object[] { 1 };
		if (constraint instanceof PrimaryKeyConstraint)
			return new Object[] { 2 };
		if (constraint instanceof UniqueConstraint)
			return new Object[] { 3 };
		if (constraint instanceof ReferencesSpecification) {
			ReferencesSpecification r = (ReferencesSpecification) constraint;
			return new Object[] { 4, r.getTableName(), r.getColumns() };
		}

		throw new UnsupportedOperationException("not supported constraint: " + constraint);
	}

	private static ColumnConstraint deserializeConstraint(Object[] arr) {
		int type = (Integer) arr[0];
		switch (type) {
		case 1:
			return new NotNullConstraint();
		case 2:
			return new PrimaryKeyConstraint();
		case 3:
			return new UniqueConstraint();
		case 4:
			return new ReferencesSpecification((String) arr[1], toList((Object[]) arr[2]));
		default:
			throw new UnsupportedOperationException("not supported constraint: " + type);
		}
	}

	private static List<String> toList(Object[] arr) {
		List<String> l = new ArrayList<String>();
		for (Object o : arr)
			l.add((String) o);
		return l;
	}
}
