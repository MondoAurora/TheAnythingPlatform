package me.giskard.dust;

import java.nio.charset.StandardCharsets;

public interface DustConsts {
	String DUST_CRED_FILE = "credentials.txt";

	String DUST_UNIT_ID = "dust.1";

	String DUST_CHARSET_UTF8 = StandardCharsets.UTF_8.name();// "UTF-8";
	String DUST_FMT_TIMESTAMP = "yyyyMMdd'T'HHmmss'Z'";

	String DUST_PLATFORM_JAVA = "java";

	String DUST_EXT_JSON = ".json";
	String DUST_EXT_CSV = ".csv";
	String DUST_EXT_XML = ".xml";
	String DUST_EXT_TXT = ".txt";
	String DUST_EXT_LDIF = ".ldif";

	String DUST_SEP = "_";
	String DUST_SEP_ID = ":";
	String DUST_SEP_TOKEN = "$";

	int KEY_ADD = -1;
	int KEY_SIZE = -2;
	Object KEY_MAP_KEYS = new Object();

	String TOKEN_CMD = DUST_UNIT_ID + DUST_SEP_TOKEN + "cmd";
	String TOKEN_CMD_LOAD = DUST_UNIT_ID + DUST_SEP_TOKEN + "load";
	String TOKEN_CMD_SAVE = DUST_UNIT_ID + DUST_SEP_TOKEN + "save";

	String TOKEN_COLLTYPE = DUST_UNIT_ID + DUST_SEP_TOKEN + "colltype";
	String TOKEN_COLLTYPE_SINGLE = DUST_UNIT_ID + DUST_SEP_TOKEN + "single";

	String TOKEN_PREFIX = DUST_UNIT_ID + DUST_SEP_TOKEN + "prefix";
	String TOKEN_PATH = DUST_UNIT_ID + DUST_SEP_TOKEN + "path";
	String TOKEN_COUNT = DUST_UNIT_ID + DUST_SEP_TOKEN + "count";
	String TOKEN_INFO = DUST_UNIT_ID + DUST_SEP_TOKEN + "Info";

	String TOKEN_INIT = DUST_UNIT_ID + DUST_SEP_TOKEN + "init";
	String TOKEN_AGENT = DUST_UNIT_ID + DUST_SEP_TOKEN + "agent";
	String TOKEN_SKIP = DUST_UNIT_ID + DUST_SEP_TOKEN + "skip";

	String TOKEN_BINARY_RESOLVER = DUST_UNIT_ID + DUST_SEP_TOKEN + "binaryResolver";
	String TOKEN_BINARY = DUST_UNIT_ID + DUST_SEP_TOKEN + "binary";

//	String TOKEN_PARAMS = DUST_UNIT_ID + DUST_SEP_TOKEN + "params";
	String TOKEN_LISTENERS = DUST_UNIT_ID + DUST_SEP_TOKEN + "listeners";
	String TOKEN_ALIAS = DUST_UNIT_ID + DUST_SEP_TOKEN + "alias";
	String TOKEN_SOURCE = DUST_UNIT_ID + DUST_SEP_TOKEN + "source";
	String TOKEN_SERIALIZER = DUST_UNIT_ID + DUST_SEP_TOKEN + "serializer";

	String TOKEN_ID = DUST_UNIT_ID + DUST_SEP_TOKEN + "id";
	String TOKEN_TYPE = DUST_UNIT_ID + DUST_SEP_TOKEN + "type";
	String TOKEN_NAME = DUST_UNIT_ID + DUST_SEP_TOKEN + "name";
	String TOKEN_DESC = DUST_UNIT_ID + DUST_SEP_TOKEN + "desc";
	String TOKEN_PAYLOAD = DUST_UNIT_ID + DUST_SEP_TOKEN + "payload";
	String TOKEN_USER = DUST_UNIT_ID + DUST_SEP_TOKEN + "user";
	String TOKEN_PASSWORD = DUST_UNIT_ID + DUST_SEP_TOKEN + "password";
	String TOKEN_AUTH = DUST_UNIT_ID + DUST_SEP_TOKEN + "auth";

	String TOKEN_KEY = DUST_UNIT_ID + DUST_SEP_TOKEN + "key";
	String TOKEN_UNIT = DUST_UNIT_ID + DUST_SEP_TOKEN + "unit";
	String TOKEN_META = DUST_UNIT_ID + DUST_SEP_TOKEN + "meta";
	String TOKEN_DATA = DUST_UNIT_ID + DUST_SEP_TOKEN + "data";

	String TOKEN_PARENT = DUST_UNIT_ID + DUST_SEP_TOKEN + "parent";
	String TOKEN_TARGET = DUST_UNIT_ID + DUST_SEP_TOKEN + "target";
	String TOKEN_MEMBERS = DUST_UNIT_ID + DUST_SEP_TOKEN + "members";
	String TOKEN_CHILDMAP = DUST_UNIT_ID + DUST_SEP_TOKEN + "childMap";
	String TOKEN_FILTER = DUST_UNIT_ID + DUST_SEP_TOKEN + "filter";
	String TOKEN_ROOT = DUST_UNIT_ID + DUST_SEP_TOKEN + "root";
	String TOKEN_INDEX = DUST_UNIT_ID + DUST_SEP_TOKEN + "index";
	String TOKEN_MAPPING = DUST_UNIT_ID + DUST_SEP_TOKEN + "mapping";

	String TOKEN_TYPE_APP = DUST_UNIT_ID + DUST_SEP_TOKEN + "Application";
	String TOKEN_TYPE_AGENT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Agent";
	String TOKEN_TYPE_SERVICE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Service";
	String TOKEN_TYPE_RELEASEONSHUTDOWN = DUST_UNIT_ID + DUST_SEP_TOKEN + "releaseOnShutdown";

	String TOKEN_RESULT_REJECT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Reject";
	String TOKEN_RESULT_PASS = DUST_UNIT_ID + DUST_SEP_TOKEN + "Pass";
	String TOKEN_RESULT_READ = DUST_UNIT_ID + DUST_SEP_TOKEN + "Read";
	String TOKEN_RESULT_READACCEPT = DUST_UNIT_ID + DUST_SEP_TOKEN + "ReadAccept";
	String TOKEN_RESULT_ACCEPT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Accept";

	String TOKEN_LEVEL_TRACE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Trace";
	String TOKEN_LEVEL_INFO = DUST_UNIT_ID + DUST_SEP_TOKEN + "Info";
	String TOKEN_LEVEL_WARNING = DUST_UNIT_ID + DUST_SEP_TOKEN + "Warning";
	String TOKEN_LEVEL_ERROR = DUST_UNIT_ID + DUST_SEP_TOKEN + "Error";
	
	String TOKEN_MANDATORY = DUST_UNIT_ID + DUST_SEP_TOKEN + "mandatory";
	String TOKEN_OPTIONAL = DUST_UNIT_ID + DUST_SEP_TOKEN + "optional";
	String TOKEN_APPEARS = DUST_UNIT_ID + DUST_SEP_TOKEN + "appears";
	
	Object NOT_FOUND = new Object();
	
	public abstract class DustObject {
		public abstract DustObject getUnit();
		public abstract DustObject getType();
		public abstract String getId();
		
		protected abstract Object getContent();
	}
	
	enum DustContext {
		Work, Input, Service, Agent, Dialog, 
	}

	enum DustAccess {
		Check(false), Peek(false), Get(false), Set(true), Insert(true), Delete(false), Reset(false), Visit(false),
		Begin(false), Commit(false), Rollback(false), Process(false);

		public final boolean creator;

		private DustAccess(boolean creator) {
			this.creator = creator;
		}
	}
		
	public abstract class DustAgent implements DustConsts {
		protected void init() throws Exception {
		}

		protected abstract Object process(DustAccess access) throws Exception;

		protected void release() throws Exception {
		}
	}
		
	enum DustOptCreate {
		Meta, Primary, Reference, None
	}
}
