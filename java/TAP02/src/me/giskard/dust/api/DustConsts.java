package me.giskard.dust.api;

public interface DustConsts {

	String DUST_CRED_FILE = "credentials.json";

	String UNIT_DUST = "giskard.me/dust.1";
	String UNIT_MIND = "giskard.me/mind.1";

	String DUST_CHARSET_UTF8 = "UTF-8";
	String DUST_FMT_TIMESTAMP = "yyyyMMdd'T'HHmmss'Z'";
	String DUST_FMT_DATE = "yyyy-MM-dd";
	String DUST_FMT_TIME = "HH:mm:ss.SSS'Z'";

	int DUST_DEF_ID_BYTES = 4;

	String DUST_PLATFORM_JAVA = "java";
	String DUST_PLATFORM_ANDROID = "android";

	String DUST_EXT_JSON = ".json";
	String DUST_EXT_CSV = ".csv";
	String DUST_EXT_XML = ".xml";
	String DUST_EXT_TXT = ".txt";
	String DUST_EXT_LDIF = ".ldif";
	String DUST_EXT_PNG = ".png";

	String DUST_SEP = "_";
	String DUST_SEP_ID = ":";
	String DUST_SEP_TOKEN = "$";

	String DUST_CONST_FALSE = "false";
	String DUST_CONST_TRUE = "true";

	int KEY_ADD = -1;
	int KEY_SIZE = -2;
	int KEY_INDEXOF = -3;
	int KEY_MEMBEROF = -4;
	Object KEY_MAP_KEYS = new Object();

	Object NOT_FOUND = new Object() {
		@Override
		public String toString() {
			return "Dust NOT_FOUND";
		}
	};
	Object NOT_IMPLEMENTED = new Object() {
		@Override
		public String toString() {
			return "Dust NOT_IMPLEMENTED";
		}
	};

	enum DustAccess {
		Check, Peek, Get, Set, Insert, Delete, Reset, Visit, Commit
	}
}
