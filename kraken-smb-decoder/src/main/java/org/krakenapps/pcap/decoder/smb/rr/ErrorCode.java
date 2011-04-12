package org.krakenapps.pcap.decoder.smb.rr;

import java.util.HashMap;
import java.util.Map;


public enum ErrorCode {
	SUCCESS(0x0000),
	//class 0x01
	ERRbadfunc( 0x0001),
	ERRbadfile(0x0002),
	ERRbadpath(0x0003),
	ERRnofids(0x0004),
	ERRnoaccess(0x0005),
	ERRbadfid(0x0006),
	ERRbadmcb(0x0007),
	ERRnomem(0x0008),
	ERRbadmem(0x0009),
	ERRbadenv(0x000A),
	ERRbadformat(0x000B),
	ERRbadaccess(0x000C),
	ERRbaddata(0x000D),
	ERRbaddrive(0x000F),
	ERRremcd(0x0010),
	ERRdiffdevice(0x0011),
	ERRnofile(0x0012),
	ERRgeneral(0x001F),
	ERRbadshare(0x0020),
	ERRlock(0x0021),
	ERReof(0x0026),
	ERRunsup(0x0032),
	ERRfilexists(0x0050),
	ERRinvalidparam(0x0057),
	ERRunknownlevel(0x007C),
	ERRinvalidseek(0x0083),
	ERROR_NOT_LOCKED(0x009E),
	ERROR_NO_MORE_SEARCH_HANDLES(0x0071),
	ERROR_CANCEL_VIOLATION(0x00AD),
	ERROR_ATOMIC_LOCKS_NOT_SUPPORTED(0x00AE),
	ERRbadpipe(0x00E6),
	ERROR_CANNOT_COPY(0x010A),
	ERRpipebusy(0x00E7),
	ERRpipeclosing(0x00E8),
	ERRnotconnected(0x00E9),
	ERRmoredata(0x00EA),
	ERRbadealist(0x00FF),
	ERROR_EAS_DIDNT_FIT(0x0113),
	ERROR_EAS_NOT_SUPPORTED(0x011A),
	ERROR_EA_ACCESS_DENIED(0x03E2),
	ERR_NOTIFY_ENUM_DIR(0x03FE),
	//0x02
	ERRerror(0x0001),
	ERRbadpw(0x0002),
	ERRaccess(0x0004),
	ERRinvtid(0x0005),
	ERRincnetname(0x0006),
	ERRinvdevice(0x0007),
	ERRinvsess(0x0010),
	ERRworking(0x0011),
	ERRnotme(0x0012),
	ERRbadcmd(0x0016),
	ERRqfull(0x0031),
	ERRqtoobig(0x0032),
	ERRqeof(0x0033),
	ERRinvpfid(0x0034),
	ERRsmbcmd(0x0040),
	ERRsrverror(0x0041),
	ERRfilespecs(0x0043),
	ERRbadpermits(0x0045),
	ERRsetattrmode(0x0047),
	ERRtimeout(0x0058),
	ERRnoresource(0x0059),
	ERRtoomanyuids(0x005A),
	ERRbaduid(0x005B),
	ERRusempx(0x00FA),
	ERRusestd(0x00FB),
	ERRcontmpx(0x00FC),
	ERRaccountExpired(0x08BF),
	ERRbadClient(0x08C0),
	ERRbadLogonTime(0x08C1),
	ERRpasswordExpired(0x08C2),
	ERRnosupport(0xFFFF),
	//0x03
	ERRnowrite(0x0013),
	ERRbadunit(0x0014),
	ERRnotready(0x0015),
	ERRdata(0x0017),
	ERRbadreq(0x0018),
	ERRseek(0x0019),
	ERRbadmedia(0x001A),
	ERRbadserctor(0x001B),
	ERRnopaper(0x001C),
	ERRwrite(0x001D),
	ERRread(0x001E),
	ERRwrongdisk(0x0022),
	ERERFCBUnavail(0x0023),
	ERRsharebufexc(0x0024),
	ERRdiskfull(0x0027);
	
	private static class ErrorCodeKey {
		private int errorClass;
		private int errorCode;
		public ErrorCodeKey(int errorClass, int errorCode) {
			super();
			this.errorClass = errorClass;
			this.errorCode = errorCode;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + errorClass;
			result = prime * result + errorCode;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ErrorCodeKey other = (ErrorCodeKey) obj;
			if (errorClass != other.errorClass)
				return false;
			if (errorCode != other.errorCode)
				return false;
			return true;
		}
		
	}
	private static Map<ErrorCodeKey, ErrorCode> codeMap = new HashMap<ErrorCodeKey, ErrorCode>();

	public int getCode() {
		return code;
	}
	public static ErrorCode parse(int errorClass, int code) {
		return codeMap.get(new ErrorCodeKey(errorClass, code));
	}
	
	ErrorCode( int code){
		this.code = code;
	}
	
	private int code;
	private static ErrorCode[] class0 = {
		SUCCESS
	};
	private static ErrorCode[] class1 = {
		ERRbadfunc,
		ERRbadfile,
		ERRbadpath,
		ERRnofids,
		ERRnoaccess,
		ERRbadfid,
		ERRbadmcb,
		ERRnomem,
		ERRbadmem,
		ERRbadenv,
		ERRbadformat,
		ERRbadaccess,
		ERRbaddata,
		ERRbaddrive,
		ERRremcd,
		ERRdiffdevice,
		ERRnofile,
		ERRgeneral,
		ERRbadshare,
		ERRlock,
		ERReof,
		ERRunsup,
		ERRfilexists,
		ERRinvalidparam,
		ERRunknownlevel,
		ERRinvalidseek,
		ERROR_NOT_LOCKED,
		ERROR_NO_MORE_SEARCH_HANDLES,
		ERROR_CANCEL_VIOLATION,
		ERROR_ATOMIC_LOCKS_NOT_SUPPORTED,
		ERRbadpipe,
		ERROR_CANNOT_COPY,
		ERRpipebusy,
		ERRpipeclosing,
		ERRnotconnected,
		ERRmoredata,
		ERRbadealist,
		ERROR_EAS_DIDNT_FIT,
		ERROR_EAS_NOT_SUPPORTED,
		ERROR_EA_ACCESS_DENIED,
		ERR_NOTIFY_ENUM_DIR
	};
	private static ErrorCode[] class2 = {
		ERRerror,
		ERRbadpw,
		ERRbadpath,
		ERRaccess,
		ERRinvtid,
		ERRincnetname,
		ERRinvdevice,
		ERRinvsess,
		ERRworking,
		ERRnotme,
		ERRbadcmd,
		ERRqfull,
		ERRqtoobig,
		ERRqeof,
		ERRinvpfid,
		ERRsmbcmd,
		ERRsrverror,
		ERRfilespecs,
		ERRbadpermits,
		ERRsetattrmode,
		ERRtimeout,
		ERRnoresource,
		ERRtoomanyuids,
		ERRbaduid,
		ERRnotconnected,
		ERRusempx,
		ERRusestd,
		ERRcontmpx,
		ERRaccountExpired,
		ERRbadClient,
		ERRbadLogonTime,
		ERRpasswordExpired,
		ERRnosupport
	};
	private static ErrorCode[] class3 = {//0x03
		ERRnowrite,
		ERRbadunit,
		ERRnotready,
		ERRbadcmd,
		ERRdata,
		ERRbadreq,
		ERRseek,
		ERRbadmedia,
		ERRbadserctor,
		ERRnopaper,
		ERRwrite,
		ERRread,
		ERRgeneral,
		ERRbadshare,
		ERRlock,
		ERRwrongdisk,
		ERERFCBUnavail,
		ERRsharebufexc,
		ERRdiskfull
	};

	static {
		for(ErrorCode code : class0){
			codeMap.put(new ErrorCodeKey(0,code.getCode()) , code);
		}
		for (ErrorCode code : class1) {
			codeMap.put(new ErrorCodeKey(1, code.getCode()), code);
		}
		for (ErrorCode code : class2) {
			codeMap.put(new ErrorCodeKey(2, code.getCode()), code);
		}
		for (ErrorCode code : class3) {
			codeMap.put(new ErrorCodeKey(3, code.getCode()), code);
		}
	}
}
