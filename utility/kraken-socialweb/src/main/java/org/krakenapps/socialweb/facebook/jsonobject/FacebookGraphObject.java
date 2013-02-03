package org.krakenapps.socialweb.facebook.jsonobject;

import java.util.Set;

import org.json.JSONObject;
import org.krakenapps.socialweb.facebook.graphapi.objectcode.Permissions;

public interface FacebookGraphObject {
	public int parseJson(JSONObject json);
	public int parseJson(JSONObject json , Set<Permissions> permit);

}
