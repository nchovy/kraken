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
package org.krakenapps.dom.model;

import org.krakenapps.api.FieldOption;

public class Schedule {
	@FieldOption(name = "day", nullable = false)
	private int dayOfWeek;

	@FieldOption(name = "begin", nullable = false)
	private int beginSecond;

	@FieldOption(name = "end", nullable = false)
	private int endSecond;

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getBeginSecond() {
		return beginSecond;
	}

	public void setBeginSecond(int beginSecond) {
		if (endSecond < 0 || endSecond > 86400)
			throw new IllegalArgumentException("end second should be 0 <= value <= 86400");
		this.beginSecond = beginSecond;
	}

	public int getEndSecond() {
		return endSecond;
	}

	public void setEndSecond(int endSecond) {
		if (endSecond < 0 || endSecond > 86400)
			throw new IllegalArgumentException("end second should be 0 <= value <= 86400");
		this.endSecond = endSecond;
	}

	@Override
	public String toString() {
		return "day=" + dayOfWeek + ", begin=" + beginSecond + ", end=" + endSecond;
	}
}
