package org.krakenapps.ber;

public class BERObject {
	private UniversalTags tag;
	private int length;
	private byte[] value;

	public BERObject(int tagValue, int length, byte[] value) {
		mappingTag(tagValue);
		this.length = length;
		this.value = value;
	}

	public UniversalTags getTag() {
		return tag;
	}

	public int getLength() {
		return length;
	}

	public byte[] getValue() {
		return value;
	}
	
	private void mappingTag(int tagValue) {
		switch(tagValue) {
		case 1:
			tag = UniversalTags.EndOfContent; 
			break;
		case 2:
			tag = UniversalTags.BOOLEAN;
			break;
		case 3:
			tag = UniversalTags.INTEGER;
			break;
		case 4: 
			tag = UniversalTags.BIT_STRING;
			break;
		case 5:
			tag = UniversalTags.OCTET_STRING;
			break;
		case 6:
			tag = UniversalTags.OID;
			break;
		case 7:
			tag = UniversalTags.OBJECT_DESCRIPTOR;
			break;
		case 8:
			tag = UniversalTags.EXTERNAL;
			break;
		case 9:
			tag = UniversalTags.REAL;
			break;
		case 10:
			tag = UniversalTags.ENUM;
			break;
		case 11:
			tag = UniversalTags.EMBEDDED_PDV;
			break;
		case 12:
			tag = UniversalTags.UTF8_STRING;
			break;
		case 13:
			tag = UniversalTags.RELATIVE_OID;
			break;
		case 16:
			tag = UniversalTags.SEQUENCE;
			break;
		case 17:
			tag = UniversalTags.SET;
			break;
		case 18:
			tag = UniversalTags.NUMERIC_STRING;
			break;
		case 19:
			tag = UniversalTags.PRINTABLE_STRING;
			break;
		case 20:
			tag = UniversalTags.T61_STRING;
			break;
		case 21:
			tag = UniversalTags.VIDEOTEX_STRING;
			break;
		case 22:
			tag = UniversalTags.IA5_STRING;
			break;
		case 23:
			tag = UniversalTags.UTC_TIME;
			break;
		case 24:
			tag = UniversalTags.GENERALIZED_TIME;
			break;
		case 25:
			tag = UniversalTags.GRAPHIC_STRING;
			break;
		case 26:
			tag = UniversalTags.VISIBLE_STRING;
			break;
		case 27:
			tag = UniversalTags.GENERAL_STRING;
			break;
		case 28:
			tag = UniversalTags.UNIVERSAL_STRING;
			break;
		case 29:
			tag = UniversalTags.CHARACTER_STRING;
			break;
		case 30:
			tag = UniversalTags.BMP_STRING;
			break;
		default:
			tag = null;
			break;
		}
	}
}