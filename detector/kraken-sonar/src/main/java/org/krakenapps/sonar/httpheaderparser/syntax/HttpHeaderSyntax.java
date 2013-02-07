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
 package org.krakenapps.sonar.httpheaderparser.syntax;

import static org.krakenapps.bnf.Syntax.choice;
import static org.krakenapps.bnf.Syntax.k;
import static org.krakenapps.bnf.Syntax.option;
import static org.krakenapps.bnf.Syntax.ref;
import static org.krakenapps.bnf.Syntax.repeat;

import org.krakenapps.bnf.Syntax;
import org.krakenapps.sonar.httpheaderparser.parser.*;

public class HttpHeaderSyntax {
	private HttpHeaderSyntax() {
		
	}
	
	public static Syntax create() {
		Syntax s = new Syntax();

		// Roots
		s.addRoot("user_agent");
		s.addRoot("server");
		s.addRoot("via");
		
		// User-Agent		= "User-Agent" ":" 1*( product | comment )
		// Server			= "Server" ":" 1*( product | comment )
		// Via				= "Via" ":" 1*( received-protocol received-by [comment] )
		s.add("user_agent", new UserAgentParser(), k("User-Agent"), k(":"), repeat(choice(ref("product"), ref("comment"))));
		s.add("server", new ServerParser(), k("Server"), k(":"), repeat(choice(ref("product"), ref("comment"))));
		s.add("via", null, k("Via"), k(":"), repeat(ref("via-content")));
		s.add("via-content", null, ref("received-protocol"), ref("received-by"), option(ref("comment")));
		
		// product			= token [ "/" product-version ]
		// product-version	= token
		// token			= 1*<any CHAR except CTLs or separators>
		s.add("product", new ProductParser(), ref("token"), option(k("/"), ref("product-version")) );
		s.add("product-version", new ProductVersionParser(), ref("token"));
		s.add("token", new TokenParser(), new Token());
		
		// comment			= "(" *( ctext | quoted-pair | comment ) ")"
		// ctext			= <any TEXT excluding "(" and ")">
		// text				= <any OCTET except CTLs, but including LWS>
		// quoted-pair		= "\" CHAR
		// CHAR				= <any US-ASCII character (octets 0 - 127)>
		s.add("comment", new CommentParser(), k("("), option(repeat(choice(ref("quoted-pair"), ref("ctext"), ref("comment")))), k(")"));
		s.add("ctext", new CTextParser(), new CText());
		s.add("quoted-pair", new QuotedPairParser(), k("\\"), ref("CHAR"));
		s.add("CHAR", new CharParser(), new Char());
		
		// received-protocol	= [ protocol-name "/" ] protocol-version
		// protocol-name		= token
		// protocol-version		= token
		// received-by			= ( host [ ":" port ] ) | pseudonym
		// pseudonym			= token
		s.add("received-protocol", null, option(ref("protocol-name"), k("/")), ref("protocol-version"));
		s.add("protocol-name", null, ref("token"));
		s.add("protocol-version", null, ref("token"));
		s.add("received-by", null, choice(ref("received-by-content"), ref("pseudonym")));
		s.add("received-by-content", null, ref("host"), ref("host-port"));
		s.add("host-port", null, ref("host"), option(k(":"), ref("port")));
		s.add("pseudonym", null, ref("token"));
		s.add("host", null, ref("token"));
		s.add("port", null, ref("token"));
		
		return s;
	}
}
