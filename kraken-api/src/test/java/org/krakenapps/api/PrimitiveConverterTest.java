package org.krakenapps.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitiveConverterTest {
	@Test
	public void testToUnderscoreName() {
		assertEquals("foo", PrimitiveConverter.toUnderscoreName("Foo"));
		assertEquals("foo_bar", PrimitiveConverter.toUnderscoreName("FooBar"));
		assertEquals("foo_bar_qoo", PrimitiveConverter.toUnderscoreName("FooBarQoo"));
	}

	@Test
	public void testFromPrimitive() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("foo", "qoo");
		m.put("bar", 123);

		Sample s = PrimitiveConverter.parse(Sample.class, m);
		assertEquals("qoo", s.foo);
		assertEquals(123, s.bar);
	}

	@Test
	public void testFromList() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("vegetable_list", Arrays.asList("cucumber", "pumpkin", "tomato", "eggplant"));

		Nested s = PrimitiveConverter.parse(Nested.class, m);

		List<String> vegetables = s.vegetableList;
		assertEquals("cucumber", vegetables.get(0));
		assertEquals("pumpkin", vegetables.get(1));
		assertEquals("tomato", vegetables.get(2));
		assertEquals("eggplant", vegetables.get(3));
	}

	@Test
	public void testToPrimitive() {
		Sample s = new Sample("qoo", 123);
		Map<String, Object> o = PrimitiveConverter.serialize(s);
		assertEquals("qoo", o.get("foo"));
		assertEquals(123, o.get("bar"));
	}

	@Test
	public void testFromNested() {
		Map<String, Object> m = new HashMap<String, Object>();
		Map<String, Object> m2 = new HashMap<String, Object>();

		m.put("foo", "qoo");
		m.put("bar", 100);
		m2.put("name", "bono");
		m2.put("vegetable_list", Arrays.asList("cucumber", "pumpkin"));

		m.put("nested", m2);

		Sample s = PrimitiveConverter.parse(Sample.class, m);
		assertEquals("bono", s.nested.name);

		List<String> vegetables = s.nested.vegetableList;
		assertEquals("cucumber", vegetables.get(0));
		assertEquals("pumpkin", vegetables.get(1));
	}

	@Test
	public void testFromNestedList() {
		Map<String, Object> m = new HashMap<String, Object>();
		Map<String, Object> m2 = new HashMap<String, Object>();
		Map<String, Object> m3 = new HashMap<String, Object>();
		List<Object> l = new ArrayList<Object>();

		m2.put("name", "bono");
		m2.put("vegetable_list", Arrays.asList("cucumber", "pumpkin"));

		m3.put("name", "qoo");
		m3.put("vegetable_list", Arrays.asList("tomato", "eggplant"));

		l.add(m2);
		l.add(m3);

		m.put("list", l);

		NestedList s = PrimitiveConverter.parse(NestedList.class, m);
		assertEquals("bono", s.list.get(0).name);
		assertEquals("qoo", s.list.get(1).name);

		List<String> vegetables = s.list.get(0).vegetableList;
		assertEquals("cucumber", vegetables.get(0));
		assertEquals("pumpkin", vegetables.get(1));

		List<String> vegetables2 = s.list.get(1).vegetableList;
		assertEquals("tomato", vegetables2.get(0));
		assertEquals("eggplant", vegetables2.get(1));
	}

	private static class Sample {
		private String foo;
		private int bar;
		private Nested nested;

		@SuppressWarnings("unused")
		public Sample() {
		}

		public Sample(String foo, int bar) {
			this.foo = foo;
			this.bar = bar;
		}

		@Override
		public String toString() {
			return "foo=" + foo + ", bar=" + bar;
		}
	}

	public static class NestedList {
		@CollectionTypeHint(Nested.class)
		private List<Nested> list;
	}

	public static class Nested {
		private String name;
		private List<String> vegetableList = new ArrayList<String>();

		@Override
		public String toString() {
			return "name=" + name + ", vegetables=" + vegetableList.size();
		}
	}
}
