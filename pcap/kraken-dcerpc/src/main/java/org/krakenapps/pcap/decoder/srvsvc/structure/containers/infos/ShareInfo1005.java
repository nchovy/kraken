/**
 * 
 */
package org.krakenapps.pcap.decoder.srvsvc.structure.containers.infos;

/**
 * @author tgnice@nchovy.com
 * 
 */
public class ShareInfo1005 {

	int shi1005_flags;
	final int SHI1005_FLAGS_DFS = 0x00000001;
	final int SHI1005_FLAGS_DFS_ROOT = 0x00000002;
	final int CSC_MASK = 0x00000030;
	final int SHI1005_FLAGS_RESTRICT_EXCLUSIVE_OPENS = 0x00000100;
	final int SHI1005_FLAGS_FORCE_SHARED_DELETE = 0x00000200;
	final int SHI1005_FLAGS_ALLOW_NAMESPACE_CACHING = 0x00000400;
	final int SHI1005_FLAGS_ACCESS_BASED_DIRECTORY_ENUM = 0x00000800;
	final int SHI1005_FLAGS_FORCE_LEVELII_OPLOCK = 0x00001000;
	final int SHI1005_FLAGS_ENABLE_HASH = 0x00002000;
	final int SHI1005_FLAGS_ENABLE_CA = 0x00004000;
	final int SHI1005_FLAGS_ENCRYPT_DATA = 0x00008000;

	public int getShi1005_flags() {
		return shi1005_flags;
	}

	public void setShi1005_flags(int shi1005_flags) {
		this.shi1005_flags = shi1005_flags;
	}

	public boolean isDfs() {
		return (shi1005_flags & SHI1005_FLAGS_DFS) == SHI1005_FLAGS_DFS ? true
				: false;
	}

	public boolean isDfsRoot() {
		return (shi1005_flags & SHI1005_FLAGS_DFS_ROOT) == SHI1005_FLAGS_DFS_ROOT ? true
				: false;
	}

	public boolean isCscMask() {
		return (shi1005_flags & CSC_MASK) == CSC_MASK ? true : false;
	}

	public boolean isRestrictExclusiveOpens() {
		return (shi1005_flags & SHI1005_FLAGS_RESTRICT_EXCLUSIVE_OPENS) == SHI1005_FLAGS_RESTRICT_EXCLUSIVE_OPENS ? true
				: false;
	}

	public boolean isForceSharedDelete() {
		return (shi1005_flags & SHI1005_FLAGS_FORCE_SHARED_DELETE) == SHI1005_FLAGS_FORCE_SHARED_DELETE ? true
				: false;
	}

	public boolean isAllowNamespaceCaching() {
		return (shi1005_flags & SHI1005_FLAGS_ALLOW_NAMESPACE_CACHING) == SHI1005_FLAGS_ALLOW_NAMESPACE_CACHING ? true
				: false;
	}

	public boolean isAccessBasedDirectoryEnum() {
		return (shi1005_flags & SHI1005_FLAGS_ACCESS_BASED_DIRECTORY_ENUM) == SHI1005_FLAGS_ACCESS_BASED_DIRECTORY_ENUM ? true
				: false;
	}

	public boolean isEnableHash() {
		return (shi1005_flags & SHI1005_FLAGS_ENABLE_HASH) == SHI1005_FLAGS_ENABLE_HASH ? true
				: false;
	}

	public boolean isEnableCA() {
		return (shi1005_flags & SHI1005_FLAGS_ENABLE_CA) == SHI1005_FLAGS_ENABLE_CA ? true
				: false;
	}

	public boolean isEncryptData() {
		return (shi1005_flags & SHI1005_FLAGS_ENCRYPT_DATA) == SHI1005_FLAGS_ENCRYPT_DATA ? true
				: false;
	}

	public boolean isFoceLevel2Oplock() {
		return (shi1005_flags & SHI1005_FLAGS_FORCE_LEVELII_OPLOCK) == SHI1005_FLAGS_FORCE_LEVELII_OPLOCK ? true
				: false;
	}
}
