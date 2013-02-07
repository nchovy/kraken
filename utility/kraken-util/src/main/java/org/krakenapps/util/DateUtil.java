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
package org.krakenapps.util;

import java.util.Date;

public class DateUtil {
	public static Date normalizeDate(Date target, int resolution) {
		return new Date(normalize(target.getTime(), resolution * 1000));
	}

	public static Date normalizeDate(Date target, int resolution, int bias) {
		return new Date(normalize(target.getTime(), resolution * 1000) + bias * 1000);
	}

	private static long normalize(long lBegin, int resolution) {
		return (lBegin / resolution + 0/*(lBegin % resolution == 0 ? 0 : 1)*/) * resolution;
	}
}
