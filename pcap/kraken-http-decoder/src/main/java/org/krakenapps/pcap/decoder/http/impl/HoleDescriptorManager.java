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
package org.krakenapps.pcap.decoder.http.impl;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.pcap.decoder.http.HttpDecoder;

/**
 * @author mindori
 */
public class HoleDescriptorManager {
	private List<HoleDescriptor> holeList;
	private int start;
	
	public HoleDescriptorManager() {
		holeList = new ArrayList<HoleDescriptor>();
	}
	
	public void addHole(HttpDecoder http, HoleDescriptor newHole) {
		if(start == newHole.getFirst())
			flush(http, newHole);
		else 
			holeList.add(newHole);
	}
	
	private void flush(HttpDecoder http, HoleDescriptor newHole) {
		/* Flush holes from start */
		http.dispatchMultipartData(newHole.getData(), 0, newHole.getData().length);
		int flushPoint = newHole.getLast();
		
		boolean findFlag = false;
		while(true) {
			/* Retrieve remain holes */
			for(int i = 0; i < holeList.size(); i++) { 
				HoleDescriptor hole = holeList.get(i);
				/* Find overlapped hole */
				if(hole.getFirst() < flushPoint) {
					byte[] b = hole.getData();
					int offset = flushPoint - hole.getFirst();
					int length = b.length - offset;
					http.dispatchMultipartData(b, offset, length);
					
					flushPoint = hole.getLast();
					holeList.remove(i);
					findFlag = true;
					
				}
				/* Find continuous hole */
				else if(hole.getFirst() == flushPoint + 1) {
					http.dispatchMultipartData(hole.getData(), 0, hole.getData().length);
					
					flushPoint = hole.getLast();
					holeList.remove(i);
					findFlag = true;
				}
			}
			if(!findFlag)
				break;
			else
				findFlag = false;
		}
		/* Set start point of flush */ 
		start = flushPoint + 1;
	}
}	