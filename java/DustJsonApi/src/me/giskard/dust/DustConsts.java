package me.giskard.dust;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface DustConsts {
	String DUST_CRED_FILE = "credentials.txt";

	String DUST_UNIT_ID = "dust.1";

	String DUST_CHARSET_UTF8 = StandardCharsets.UTF_8.name();// "UTF-8";
	String DUST_FMT_TIMESTAMP = "yyyyMMdd'T'HHmmss'Z'";

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

	String TOKEN_CMD = "cmd";
	String TOKEN_CMD_LOAD = "load";
	String TOKEN_CMD_SAVE = "save";

	String TOKEN_PATH = "path";

	String TOKEN_INIT = "init";
	String TOKEN_AGENTS = "agents";
	String TOKEN_AGENT = "agent";
	String TOKEN_CLASS_NAME = "className";
	String TOKEN_SKIP = "skip";

	String TOKEN_PARAMS = "params";
	String TOKEN_ALIAS = "alias";
	String TOKEN_SOURCE = "source";
	String TOKEN_SERIALIZER = "serializer";

	String TOKEN_NAME = "name";
	String TOKEN_DESC = "desc";
	String TOKEN_PAYLOAD = "payload";
	String TOKEN_USER = "user";
	String TOKEN_PASSWORD = "password";
	String TOKEN_AUTH = "auth";

	String TOKEN_KEY = "key";
	String TOKEN_UNIT = "unit";
	String TOKEN_META = "meta";
	String TOKEN_DATA = "data";

	String TOKEN_PARENT = "parent";
	String TOKEN_TARGET = "target";
	String TOKEN_MEMBERS = "members";
	String TOKEN_CHILDMAP = "childMap";
	String TOKEN_FILTER = "filter";
	String TOKEN_ROOT = "root";

	String TOKEN_TYPE_APP = "Application";
	String TOKEN_TYPE_AGENT = "Agent";
	String TOKEN_TYPE_MESSAGE = "Message";
	String TOKEN_TYPE_RELEASEONSHUTDOWN = "releaseOnShutdown";
	
	String TOKEN_RESULT_REJECT = "Reject";
	String TOKEN_RESULT_PASS = "Pass";
	String TOKEN_RESULT_READ = "Read";
	String TOKEN_RESULT_READACCEPT = "ReadAccept";
	String TOKEN_RESULT_ACCEPT = "Accept";


	String TOKEN_LEVEL_TRACE = "Trace";
	String TOKEN_LEVEL_INFO = "Info";
	String TOKEN_LEVEL_WARNING = "Warning";
	String TOKEN_LEVEL_ERROR = "Error";

	enum DustAction {
		Init, Begin, Process, End, Release,
	};

	interface DustAgent {
		<RetType> RetType agentProcess(DustAction action, Object params) throws Exception;
	}

	@SuppressWarnings("unchecked")
	abstract class DustAgentBase implements DustAgent {
		protected Map<String, Object> cfg;

		@Override
		public <RetType> RetType agentProcess(DustAction action, Object params) throws Exception {
			Object ret = null;

			switch (action) {
			case Begin:
				break;
			case End:
				break;
			case Init:
				this.cfg = (Map<String, Object>) params;
				init();
				break;
			case Process:
				ret = process(cfg, params);
				break;
			case Release:
				release();
				break;
			}

			return (RetType) ret;
		}

		protected void init() throws Exception {
		}

		protected void release() throws Exception {
		}

		protected Object process(Map<String, Object> cfg, Object params) throws Exception {
			return null;
		}

	}
}
