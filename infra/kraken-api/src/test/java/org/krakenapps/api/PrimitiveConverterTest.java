/*
 * Copyright 2011 Future Systems
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
		m.put("ignore", "skip");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("name", "xeraph");
		m2.put("vegetable_list", Arrays.asList("cucumber", "pumpkin"));
		m.put("nested", m2);

		Sample s = PrimitiveConverter.parse(Sample.class, m);
		assertEquals("qoo", s.foo);
		assertEquals(123, s.bar);
		assertEquals(null, s.ignore);
		assertEquals("xeraph", s.nested.name);
		assertEquals(Arrays.asList("cucumber", "pumpkin"), s.nested.vegetableList);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testToPrimitive() {
		Sample s = new Sample("qoo", 123, "ignore field", new Nested("xeraph", "cucumber", "pumpkin"), new Nested(
				"delmitz"));
		Map<String, Object> o = (Map<String, Object>) PrimitiveConverter.serialize(s);
		assertEquals("qoo", o.get("foo"));
		assertEquals(123, o.get("bar"));
		assertEquals(false, o.containsKey("ignore"));
		Map<String, Object> m = (Map<String, Object>) o.get("nested");
		assertEquals("xeraph", m.get("name"));
		assertEquals(Arrays.asList("cucumber", "pumpkin"), m.get("vegetable_list"));
		Map<String, Object> m2 = (Map<String, Object>) o.get("ref");
		assertEquals("delmitz", m2.get("name"));
		assertEquals(true, m2.containsKey("vegetable_list"));
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

	@SuppressWarnings("unchecked")
	@Test
	public void testToList() {
		Nested s = new Nested();
		s.name = "nchovy refrigerator";
		s.vegetableList = Arrays.asList("haruyache", "etulyache", "sahulyache", "nahulyache");

		Map<String, Object> o = (Map<String, Object>) PrimitiveConverter.serialize(s);
		assertEquals("nchovy refrigerator", o.get("name"));
		assertEquals(Arrays.asList("haruyache", "etulyache", "sahulyache", "nahulyache"), o.get("vegetable_list"));
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
		@FieldOption(skip = true)
		private String ignore;
		private Nested nested;
		@SuppressWarnings("unused")
		@ReferenceKey("name")
		private Nested ref;

		@SuppressWarnings("unused")
		public Sample() {
		}

		public Sample(String foo, int bar, String ignore, Nested nested, Nested ref) {
			this.foo = foo;
			this.bar = bar;
			this.ignore = ignore;
			this.nested = nested;
			this.ref = ref;
		}
	}

	public static class NestedList {
		@CollectionTypeHint(Nested.class)
		private List<Nested> list;

		public NestedList() {
		}

		public NestedList(List<Nested> list) {
			this.list = list;
		}
	}

	public static class Nested {
		private String name;
		private List<String> vegetableList = new ArrayList<String>();

		public Nested() {
		}

		public Nested(String name, String... vegetableList) {
			this.name = name;
			this.vegetableList = Arrays.asList(vegetableList);
		}
	}
}
