package org.krakenapps.docxcod;

import java.util.Map;

public interface OOXMLProcessor {

	void process(OOXMLPackage pkg, Map<String, Object> rootMap);

}
