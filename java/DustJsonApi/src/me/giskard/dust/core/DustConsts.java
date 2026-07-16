package me.giskard.dust.core;

import me.giskard.tokens.DustGenTokens_aaa_1;
import me.giskard.tokens.DustGenTokens_dust_1;
import me.giskard.tokens.DustGenTokens_mind_1;
import me.giskard.tokens.DustGenTokens_misc_1;

public interface DustConsts extends DustGenTokens_dust_1, DustGenTokens_misc_1, DustGenTokens_mind_1, DustGenTokens_aaa_1 {


	String DUST_CRED_FILE = "credentials.json";
	
	String UNIT_DUST = "dust.1";
	String UNIT_MIND = "mind.1";

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

	Object NOT_FOUND = new Object();
	Object NOT_IMPLEMENTED = new Object();

	enum DustContext {
		Work, Input, Service, Agent, Dialog,
	}

	enum DustAccess {
		Check(false), Peek(false), Get(false), Set(true), Insert(true), Delete(false), Reset(false), Visit(false), Begin(false), Commit(false), Rollback(false),
		Process(false);

		public final boolean creator;

		private DustAccess(boolean creator) {
			this.creator = creator;
		}
	}

	enum DustOptCreate {
		Meta, Primary, Reference, None
	}

	enum DustCollType {
		One, Set, Arr, Map;
	};

	enum DustValType {
		Handle, Integer, Real, String, Bool, Stream, Raw;
	};

	public interface DustHandle {
		DustHandle getUnit();

		DustHandle getType();

		String getId();
	}

	enum DustAction {
		Init, Begin, Process, End, Release,
	}

	public abstract class DustAgent implements DustConsts {
		protected final Object process(DustAction action, DustAccess access) throws Exception {
			Object ret = null;

			switch (action) {
			case Init:
				init();
				break;
			case Begin:
				ret = begin();
				break;
			case Process:
				ret = process(access);
				break;
			case End:
				ret = end(access == DustAccess.Commit);
				break;
			case Release:
				release();
				break;
			}

			return ret;
		}

		protected void init() throws Exception {
		}

		protected Object begin() throws Exception {
			return NOT_IMPLEMENTED;
		}

		protected abstract Object process(DustAccess access) throws Exception;

		protected Object end(boolean commit) throws Exception {
			return NOT_IMPLEMENTED;
		}

		protected void release() throws Exception {
		}
	}

}
