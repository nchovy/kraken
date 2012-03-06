package org.krakenapps.ftp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListEntry {
	public static enum Type {
		File, Directory
	};

	private Long size;
	private Date modify;
	private Date create;
	private Type type;
	private String unique;
	private String perm;
	private String lang;
	private String mediaType;
	private String charset;
	private String name;

	public ListEntry() {
	}

	public ListEntry(String str) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String[] tokens = str.split(" ", 2)[0].split(";");

		this.name = str.split(" ", 2)[1];
		for (String token : tokens) {
			try {
				String key = token.split("=")[0];
				String value = token.split("=")[1];
				if (key.equalsIgnoreCase("size"))
					this.size = Long.parseLong(value);
				else if (key.equalsIgnoreCase("modify"))
					this.modify = dateFormat.parse(value);
				else if (key.equalsIgnoreCase("create"))
					this.create = dateFormat.parse(value);
				else if (key.equalsIgnoreCase("type"))
					this.type = (value.equals("dir")) ? Type.Directory : Type.File;
				else if (key.equalsIgnoreCase("unique"))
					this.unique = value;
				else if (key.equalsIgnoreCase("perm"))
					this.perm = value;
				else if (key.equalsIgnoreCase("lang"))
					this.lang = value;
				else if (key.equalsIgnoreCase("media-type"))
					this.mediaType = value;
				else if (key.equalsIgnoreCase("charset"))
					this.charset = value;
			} catch (ParseException e) {
			} catch (IndexOutOfBoundsException e) {
			}
		}
	}

	public Long getSize() {
		return size;
	}

	public Date getModify() {
		return modify;
	}

	public Date getCreate() {
		return create;
	}

	public Type getType() {
		return type;
	}

	public String getUnique() {
		return unique;
	}

	public String getPerm() {
		return perm;
	}

	public String getLang() {
		return lang;
	}

	public String getMediaType() {
		return mediaType;
	}

	public String getCharset() {
		return charset;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String str = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		if (size != null)
			str += "size=" + size + ";";
		if (modify != null)
			str += "modify=" + dateFormat.format(modify) + ";";
		if (create != null)
			str += "create=" + dateFormat.format(create) + ";";
		if (type != null)
			str += "type=" + type + ";";
		if (unique != null)
			str += "unique=" + unique + ";";
		if (perm != null)
			str += "perm=" + perm + ";";
		if (lang != null)
			str += "lang=" + lang + ";";
		if (mediaType != null)
			str += "media-type=" + mediaType + ";";
		if (charset != null)
			str += "charset=" + charset + ";";

		return str + " " + name;
	}

}
