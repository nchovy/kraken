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
package org.krakenapps.logdb.query.command;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.codec.CustomCodec;
import org.krakenapps.codec.EncodedStringCache;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.TypeMismatchException;
import org.krakenapps.codec.UnsupportedTypeException;
import org.krakenapps.logdb.sort.SortCodec;

public class FunctionCodec implements CustomCodec {
	public final static FunctionCodec instance = new FunctionCodec();

	private static Map<Class<?>, Byte> typeCode;
	static {
		typeCode = new HashMap<Class<?>, Byte>();
		typeCode.put(Function.Average.class, (byte) 128);
		typeCode.put(Function.Count.class, (byte) 129);
		// typeCode.put(Function.DistinctCount.class, (byte) 130);
		typeCode.put(Function.First.class, (byte) 131);
		typeCode.put(Function.Last.class, (byte) 132);
		// typeCode.put(Function.ValueList.class, (byte) 133);
		typeCode.put(Function.Max.class, (byte) 134);
		typeCode.put(Function.Min.class, (byte) 135);
		// typeCode.put(Function.Mode.class, (byte) 136);
		typeCode.put(Function.Range.class, (byte) 137);
		typeCode.put(Function.Sum.class, (byte) 138);
		// typeCode.put(Function.SumSquare.class, (byte) 139);
		// typeCode.put(Function.Values.class, (byte) 140);
		typeCode.put(Timechart.PerSecond.class, (byte) 141);
		typeCode.put(Timechart.PerMinute.class, (byte) 142);
		typeCode.put(Timechart.PerHour.class, (byte) 143);
		typeCode.put(Timechart.PerDay.class, (byte) 144);
	}

	@Override
	public void encode(ByteBuffer bb, Object value) {
		Byte code = typeCode.get(value.getClass());
		if (code == null)
			throw new UnsupportedTypeException(value.toString());

		List<Object> datas = getDatas(code, (Function) value);
		int contentLength = 0;
		for (Object data : datas) {
			if (data instanceof String)
				contentLength += EncodedStringCache.getEncodedString((String) data).length();
			else
				contentLength += EncodingRule.lengthOf(data, SortCodec.instance);
		}

		bb.put(code.byteValue());
		EncodingRule.encodeRawNumber(bb, int.class, contentLength);
		for (Object data : datas) {
			if (data instanceof String) {
				EncodedStringCache c = EncodedStringCache.getEncodedString((String) data);
				bb.put(EncodingRule.STRING_TYPE);
				EncodingRule.encodeRawNumber(bb, int.class, c.value().length);
				bb.put(c.value());
			} else
				EncodingRule.encode(bb, data);
		}
	}

	@Override
	public int lengthOf(Object value) {
		Byte code = typeCode.get(value.getClass());
		if (code == null)
			throw new UnsupportedTypeException(value.toString());

		List<Object> datas = getDatas(code, (Function) value);
		int contentLength = 0;
		for (Object data : datas) {
			if (data instanceof String)
				contentLength += EncodedStringCache.getEncodedString((String) data).length();
			else
				contentLength += EncodingRule.lengthOf(data);
		}

		return 1 + EncodingRule.lengthOfRawNumber(int.class, contentLength) + contentLength;
	}

	private List<Object> getDatas(byte code, Function value) {
		List<Object> datas = new ArrayList<Object>();
		datas.add(value.getName());
		datas.add(value.getTarget());
		datas.add(value.getKeyName());

		if (code == (byte) 128) {
			datas.add(((Function.Average) value).getD());
			datas.add(((Function.Average) value).getCount());
		} else if (code == (byte) 129) {
			datas.add(((Function.Count) value).getResult());
		} else if (code == (byte) 130) {
			// datas.add(((Function.DistinctCount) value).getObjs());
		} else if (code == (byte) 131) {
			datas.add(((Function.First) value).getFirst());
		} else if (code == (byte) 132) {
			datas.add(((Function.Last) value).getLast());
		} else if (code == (byte) 133) {
			// datas.add(((Function.ValueList) value).getObjs());
		} else if (code == (byte) 134) {
			datas.add(((Function.Max) value).getMax());
		} else if (code == (byte) 135) {
			datas.add(((Function.Min) value).getMin());
		} else if (code == (byte) 136) {
			// datas.add(((Function.Mode) value).getResult());
			// datas.add(((Function.Mode) value).getMaxCount());
			// datas.add(((Function.Mode) value).getNowCount());
		} else if (code == (byte) 137) {
			datas.add(((Function.Range) value).getMax());
			datas.add(((Function.Range) value).getMin());
		} else if (code == (byte) 138) {
			datas.add(((Function.Sum) value).getSum());
		} else if (code == (byte) 139) {
			// datas.add(((Function.SumSquare) value).getSum());
		} else if (code == (byte) 140) {
			// datas.add(((Function.Values) value).getObjs());
		} else if (code == (byte) 141 || code == (byte) 142 || code == (byte) 143 || code == (byte) 144) {
			datas.add(((Timechart.PerTime) value).getAmount());
		}
		return datas;
	}

	@Override
	public Object decode(ByteBuffer bb) {
		int beginPosition = bb.position();
		byte code = bb.get();

		int length = (int) EncodingRule.decodeRawNumber(bb);
		int limit = bb.limit();
		bb.limit(bb.position() + length);
		String name = (String) EncodingRule.decode(bb);
		String target = (String) EncodingRule.decode(bb);
		String keyName = (String) EncodingRule.decode(bb);
		Function func = Function.getFunction(name, target, keyName, Timechart.func);

		if (typeCode.get(func.getClass()) != code)
			throw new TypeMismatchException(typeCode.get(func.getClass()), code, beginPosition);

		if (code == (byte) 128) {
			((Function.Average) func).setD((Double) EncodingRule.decode(bb));
			((Function.Average) func).setCount((Integer) EncodingRule.decode(bb));
		} else if (code == (byte) 129) {
			((Function.Count) func).setResult((Long) EncodingRule.decode(bb));
		} else if (code == (byte) 130) {
			// ((Function.DistinctCount) func).setObjs((List<Object>)
			// EncodingRule.decode(bb));
		} else if (code == (byte) 131) {
			((Function.First) func).setFirst(EncodingRule.decode(bb));
		} else if (code == (byte) 132) {
			((Function.Last) func).setLast(EncodingRule.decode(bb));
		} else if (code == (byte) 133) {
			// ((Function.ValueList) func).setObjs((List<Object>)
			// EncodingRule.decode(bb));
		} else if (code == (byte) 134) {
			((Function.Max) func).setMax(EncodingRule.decode(bb));
		} else if (code == (byte) 135) {
			((Function.Min) func).setMin(EncodingRule.decode(bb));
		} else if (code == (byte) 136) {
			// ((Function.Mode) func).setResult(EncodingRule.decode(bb));
			// ((Function.Mode) func).setMaxCount((Integer)
			// EncodingRule.decode(bb));
			// ((Function.Mode) func).setNowCount((Integer)
			// EncodingRule.decode(bb));
		} else if (code == (byte) 137) {
			((Function.Range) func).setMax((Number) EncodingRule.decode(bb));
			((Function.Range) func).setMin((Number) EncodingRule.decode(bb));
		} else if (code == (byte) 138) {
			((Function.Sum) func).setSum((Number) EncodingRule.decode(bb));
		} else if (code == (byte) 139) {
			// ((Function.SumSquare) func).setSum((Number)
			// EncodingRule.decode(bb));
		} else if (code == (byte) 140) {
			// ((Function.Values) func).setObjs((List<Object>)
			// EncodingRule.decode(bb));
		} else if (code == (byte) 141 || code == (byte) 142 || code == (byte) 143 || code == (byte) 144) {
			((Timechart.PerTime) func).setAmount((Long) EncodingRule.decode(bb));
		}

		bb.limit(limit);

		return func;
	}

	@Override
	public int getObjectLength(ByteBuffer bb) {
		throw new UnsupportedOperationException();
	}

}
