package org.krakenapps.socialweb.facebook.jsonobject.fieldelement;

import java.util.ArrayList;

public class Likes {

	int count;
	ArrayList<From> likeList;

	Likes() {
		this.count = 0;
		this.likeList = new ArrayList<From>();
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
