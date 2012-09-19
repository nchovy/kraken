package org.krakenapps.msgbus;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "msgbus-token-plugin")
@MsgbusPlugin
public class TokenPlugin {
	
	@Requires
	private TokenApi tokenApi;
	
	@MsgbusMethod
	public void issueToken(Request req, Response resp) {
		Token token = new Token();
		token.setData(req.get("token_data"));
		tokenApi.addToken(token);

		resp.put("token_id", token.getTokenId());
	}
}