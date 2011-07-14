package org.krakenapps.crl;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

public class LdapSearch {
	private LdapSearch() { 
	}
	
	@SuppressWarnings("unchecked")
	public static byte[] getCrls(String ldapUrl, String filter) throws NamingException {
		DirContext dcLdap = new InitialDirContext();
		NamingEnumeration result = dcLdap.search(ldapUrl, filter, null);

		while ((result != null) && (result.hasMore())) {
			SearchResult srLdapSearch = (SearchResult) result.next();
			Attributes attrs = srLdapSearch.getAttributes();

			if (attrs == null)
				continue;
			else {
				for (NamingEnumeration attributes = attrs.getAll(); attributes.hasMore();) {
					Attribute attr = (Attribute) attributes.next();
					String strAttrID = attr.getID();
					
					for (NamingEnumeration attribute = attr.getAll(); attribute.hasMore();) {
						if(!strAttrID.equalsIgnoreCase("certificateRevocationList"))
							continue;
						
						/* get CRL bytes */
						return (byte[]) attribute.next();
					}
				}
			}
		}
		return null;
	}
}
