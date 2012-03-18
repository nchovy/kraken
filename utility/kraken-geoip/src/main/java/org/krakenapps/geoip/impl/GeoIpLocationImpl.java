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
package org.krakenapps.geoip.impl;

import org.krakenapps.geoip.GeoIpLocation;

public class GeoIpLocationImpl implements GeoIpLocation {
	private int id;
	private String country;
	private String region;
	private String city;
	private String postalCode;
	private double latitude;
	private double longitude;
	private Integer metroCode;
	private Integer areaCode;

	public GeoIpLocationImpl(String s) {
		String[] t = s.split(",");

		id = Integer.parseInt(t[0]);
		country = t[1].replaceAll("\"", "");
		region = t[2];
		city = t[3];
		postalCode = t[4];
		latitude = Double.parseDouble(t[5]);
		longitude = Double.parseDouble(t[6]);

		if (t.length > 7 && !t[7].isEmpty())
			metroCode = Integer.parseInt(t[7]);

		if (t.length > 8 && !t[8].isEmpty())
			areaCode = Integer.parseInt(t[8]);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getCountry() {
		return country;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public String getCity() {
		return city;
	}

	@Override
	public String getPostalCode() {
		return postalCode;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	@Override
	public Integer getMetroCode() {
		return metroCode;
	}

	@Override
	public Integer getAreaCode() {
		return areaCode;
	}

	@Override
	public String toString() {
		return String.format("id=%d, country=%s, region=%s, city=%s, latitude=%f, longitude=%f", id, country, region, city, latitude,
				longitude);
	}

}
