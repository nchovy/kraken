package org.krakenapps.socialweb.facebook.jsonobject.fieldelement;

import java.util.ArrayList;

public class Like {

	int count;
	ArrayList<From> likeList;

	public Like() {
		this.count = 0;
		this.likeList = new ArrayList<From>();
	}

	public Like(ArrayList<From> likeList , int count) {
		this.likeList = likeList;
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ArrayList<From> getLikeList() {
		return likeList;
	}

	public void setLikeList(ArrayList<From> likeList) {
		this.likeList = likeList;
	}

}
