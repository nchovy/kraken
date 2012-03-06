/*
 **************************************************************************
 *                                                                        *
 *          General Purpose Hash Function Algorithms Library              *
 *                                                                        *
 * Author: Arash Partow - 2002                                            *
 * URL: http://www.partow.net                                             *
 * URL: http://www.partow.net/programming/hashfunctions/index.html        *
 *                                                                        *
 * Copyright notice:                                                      *
 * Free use of the General Purpose Hash Function Algorithms Library is    *
 * permitted under the guidelines and in accordance with the most current *
 * version of the Common Public License.                                  *
 * http://www.opensource.org/licenses/cpl.php                             *
 *                                                                        *
 **************************************************************************
 */
package org.krakenapps.bloomfilter;

public abstract class GeneralHashFunction {

	public static HashFunction<String> RSHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int b = 378551;
			int a = 63689;
			int hash = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = hash * a + key.charAt(i);
				a = a * b;
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "RSHash";
		}
	};

	public static HashFunction<String> JSHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 1315423911;
			for (int i = 0; i < key.length(); i++) {
				hash ^= (hash << 5) + key.charAt(i) + (hash >> 2);
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "JSHash";
		}
	};

	public static HashFunction<String> PJWHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int BitIsUnsignedInt = Integer.SIZE;
			int ThreeQuarters = (BitIsUnsignedInt * 3) / 4;
			int OneEighth = BitIsUnsignedInt / 8;
			int HighBits = (0xFFFFFFFF) << (BitIsUnsignedInt - OneEighth);
			int hash = 0;
			int test = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = (hash << OneEighth) + key.charAt(i);
				if ((test = hash & HighBits) != 0)
					hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "PJWHash";
		}
	};

	public static HashFunction<String> ELFHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 0;
			int x = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = (hash << 4) + key.charAt(i);
				if ((x = hash & 0xF0000000) != 0)
					hash ^= (x >> 24);
				hash &= ~x;
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "ELFHash";
		}
	};

	public static HashFunction<String> BKDRHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int seed = 131;
			int hash = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = (hash * seed) + key.charAt(i);
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "BKDRHash";
		}
	};

	public static HashFunction<String> SDBMHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = key.charAt(i) + (hash << 6) + (hash << 16) - hash;
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "SDBMHash";
		}
	};

	public static HashFunction<String> DJBHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 5381;
			for (int i = 0; i < key.length(); i++) {
				hash = (hash << 5) + hash + key.charAt(i);
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "DJBHash";
		}
	};

	public static HashFunction<String> BPHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 0;
			for (int i = 0; i < key.length(); i++) {
				hash = hash << 7 ^ key.charAt(i);
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "BPHash";
		}
	};

	public static HashFunction<String> FNVHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int fnv_prime = 0x811c9dc5;
			int hash = 0;
			for (int i = 0; i < key.length(); i++) {
				hash *= fnv_prime;
				hash ^= key.charAt(i);
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "FNVHash";
		}
	};

	public static HashFunction<String> APHash = new HashFunction<String>() {
		@Override
		public int hashCode(String key) {
			int hash = 0xAAAAAAAA;
			for (int i = 0; i < key.length(); i++) {
				hash ^= ((i & 1) == 0) ? ((hash << 7) ^ key.charAt(i) ^ (hash >> 3))
						: (~((hash << 11) ^ key.charAt(i) ^ (hash >> 5)));
			}
			return unsigned32(hash);
		}

		@Override
		public String toString() {
			return "APHash";
		}
	};

	@SuppressWarnings("rawtypes")
	public static HashFunction[] stringHashFunctions = { RSHash, JSHash,
			PJWHash, ELFHash, BKDRHash, SDBMHash, DJBHash, BPHash, FNVHash };

	private static int unsigned32(int hash) {
		return (int) ((hash & 0xFFFFFFFFL) % (Integer.MAX_VALUE + 1));
	}

}
