package org.krakenapps.crl;

public class X500AttributeClassifier {
	private X500AttributeClassifier() {
	}
	
	public static X500Attribute getAttribute(String oid) {
		/* OID of X.500 attribute types: 2.5.4 */
		/* http://www.alvestrand.no/objectid/2.5.4.html */
		if(!oid.startsWith("2.5.4"))
			return null;
		
		int value = Integer.parseInt(oid.substring(6));
		switch (value) {
		case 0:
			return X500Attribute.OBJECTCLASS;
		case 1:
			return X500Attribute.ALIASEDENTRYNAME;
		case 2:
			return X500Attribute.KNOWLDGEINFORMATION;
		case 3:
			return X500Attribute.COMMONNAME;
		case 4:
			return X500Attribute.SURNAME;
		case 5:
			return X500Attribute.SERIALNUMBER;
		case 6:
			return X500Attribute.COUNTRYNAME;
		case 7:
			return X500Attribute.LOCALITYNAME;
		case 8:
			return X500Attribute.STATEORPROVINCENAME;
		case 9:
			return X500Attribute.STREETADDRESS;
		case 10:
			return X500Attribute.ORGANIZATIONNAME;
		case 11:
			return X500Attribute.ORGANIZATIONALUNITNAME;
		case 12:
			return X500Attribute.TITLE;
		case 13:
			return X500Attribute.DESCRIPTION;
		case 14:
			return X500Attribute.SEARCHGUIDE;
		case 15:
			return X500Attribute.BUSINESSCATEGORY;
		case 16:
			return X500Attribute.POSTALADDRESS;
		case 17:
			return X500Attribute.POSTALCODE;
		case 18:
			return X500Attribute.POSTOFFICEBOX;
		case 19:
			return X500Attribute.PHYSICALDELIVERYOFFICENAME;
		case 20:
			return X500Attribute.TELEPHONENUMBER;
		case 21:
			return X500Attribute.TELEXNUMBER;
		case 22:
			return X500Attribute.TELETEXTERMINALIDENTIFIER;
		case 23:
			return X500Attribute.FACSIMILETELEPHONENUMBER;
		case 24:
			return X500Attribute.X121ADDRESS;
		case 25:
			return X500Attribute.INTERNATIONALISDNNUMBER;
		case 26:
			return X500Attribute.REGISTEREDADDRESS;
		case 27:
			return X500Attribute.DESTINATIONINDICATOR;
		case 28:
			return X500Attribute.PREFERREDDELIVERYMETHOD;
		case 29:
			return X500Attribute.PRESENTATIONADDRESS;
		case 30:
			return X500Attribute.SUPPORTEDAPPLICATIONCONTEXT;
		case 31:
			return X500Attribute.MEMBER;
		case 32:
			return X500Attribute.OWNER;
		case 33:
			return X500Attribute.ROLEOCCUPANT;
		case 34:
			return X500Attribute.SEEALSO;
		case 35:
			return X500Attribute.USERPASSWORD;
		case 36:
			return X500Attribute.USERCERTIFICATE;
		case 37:
			return X500Attribute.CACERTIFICATE;
		case 38:
			return X500Attribute.AUTHORITYREVOCATIONLIST;
		case 39:
			return X500Attribute.CERTIFICATEREVOCATIONLIST;
		case 40:
			return X500Attribute.CROSSCERTIFICATEPAIR;
		case 41:
			return X500Attribute.NAME;
		case 42:
			return X500Attribute.GIVENNAME;
		case 43:
			return X500Attribute.INITIALS;
		case 44:
			return X500Attribute.GENERATIONQUALIFIER;
		case 45:
			return X500Attribute.UNIQUEIDENTIFIER;
		case 46:
			return X500Attribute.DNQUALIFIER;
		case 47:
			return X500Attribute.ENHANCEDSEARCHGUIDE;
		case 48:
			return X500Attribute.PROTOCOLINFORMATION;
		case 49:
			return X500Attribute.DISTINGUISHEDNAME;
		case 50:
			return X500Attribute.UNIQUEMEMBER;
		case 51:
			return X500Attribute.HOUSEIDENTIFIER;
		case 52:
			return X500Attribute.SUPPORTEDALGORITHMS;
		case 53:
			return X500Attribute.DELTAREVOCATIONLIST;
		case 58:
			return X500Attribute.ATTRIBUTECERTIFICATE;
		case 65:
			return X500Attribute.PSEUDONYM;
		default:
			return null;
		}
	}
}