package me.giskard.dust.core;

public interface DustConsts {
	String DUST_CRED_FILE = "credentials.json";

	String UNIT_DUST = "dust.1";
	String UNIT_DEV = "dev.1";
	String UNIT_GUI = "gui.1";
	String UNIT_TEXT = "text.1";
	String UNIT_LAYOUT = "layout.1";
	String UNIT_LANG = "lang.1";
	String UNIT_GEOMETRY = "geometry.1";

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

	String TOKEN_CMD = UNIT_DUST + DUST_SEP_TOKEN + "cmd";
	String TOKEN_CMD_LOAD = UNIT_DUST + DUST_SEP_TOKEN + "load";
	String TOKEN_CMD_SAVE = UNIT_DUST + DUST_SEP_TOKEN + "save";
	String TOKEN_CMD_INFO = UNIT_DUST + DUST_SEP_TOKEN + "info";
	String TOKEN_CMD_REFRESH = UNIT_DUST + DUST_SEP_TOKEN + "refresh";
	String TOKEN_CMD_RESPOND = UNIT_DUST + DUST_SEP_TOKEN + "respond";
	String TOKEN_CMD_TEST = UNIT_DUST + DUST_SEP_TOKEN + "test";
	String TOKEN_CMD_LOADALL = UNIT_DUST + DUST_SEP_TOKEN + "loadAll";

	String TOKEN_VALTYPE = UNIT_DUST + DUST_SEP_TOKEN + "valtype";
	String TOKEN_VALTYPE_HANDLE = TOKEN_VALTYPE + "Handle";
	String TOKEN_VALTYPE_INTEGER = TOKEN_VALTYPE + "Integer";
	String TOKEN_VALTYPE_REAL = TOKEN_VALTYPE + "Real";
	String TOKEN_VALTYPE_STRING = TOKEN_VALTYPE + "String";
	String TOKEN_VALTYPE_BOOL = TOKEN_VALTYPE + "Bool";
	String TOKEN_VALTYPE_STREAM = TOKEN_VALTYPE + "Stream";
	String TOKEN_VALTYPE_RAW = TOKEN_VALTYPE + "Raw";

	String TOKEN_COLLTYPE = UNIT_DUST + DUST_SEP_TOKEN + "colltype";
	String TOKEN_COLLTYPE_ONE = TOKEN_COLLTYPE + "One";
	String TOKEN_COLLTYPE_MAP = TOKEN_COLLTYPE + "Map";
	String TOKEN_COLLTYPE_ARR = TOKEN_COLLTYPE + "Arr";
	String TOKEN_COLLTYPE_SET = TOKEN_COLLTYPE + "Set";

	String TOKEN_PREFIX = UNIT_DUST + DUST_SEP_TOKEN + "prefix";
	String TOKEN_PATH = UNIT_DUST + DUST_SEP_TOKEN + "path";
	String TOKEN_COUNT = UNIT_DUST + DUST_SEP_TOKEN + "count";
	String TOKEN_INFO = UNIT_DUST + DUST_SEP_TOKEN + "Info";

	String TOKEN_INIT = UNIT_DUST + DUST_SEP_TOKEN + "init";
	String TOKEN_AGENT = UNIT_DUST + DUST_SEP_TOKEN + "agent";
	String TOKEN_SKIP = UNIT_DUST + DUST_SEP_TOKEN + "skip";

	String TOKEN_BINARY_RESOLVER = UNIT_DUST + DUST_SEP_TOKEN + "binaryResolver";
	String TOKEN_BINARY = UNIT_DUST + DUST_SEP_TOKEN + "binary";

	String TOKEN_TRANSACTION_HEAD = UNIT_DUST + DUST_SEP_TOKEN + "transactionHead";
	String TOKEN_TRANSACTION_ITEM = UNIT_DUST + DUST_SEP_TOKEN + "transactionItem";
	
	String TOKEN_LISTENERS = UNIT_DUST + DUST_SEP_TOKEN + "listeners";
	String TOKEN_ALIAS = UNIT_DUST + DUST_SEP_TOKEN + "alias";
	String TOKEN_SOURCE = UNIT_DUST + DUST_SEP_TOKEN + "source";
	String TOKEN_SERIALIZER = UNIT_DUST + DUST_SEP_TOKEN + "serializer";

	String TOKEN_ID = UNIT_DUST + DUST_SEP_TOKEN + "id";
	String TOKEN_TYPE = UNIT_DUST + DUST_SEP_TOKEN + "type";
	String TOKEN_TAGS = UNIT_DUST + DUST_SEP_TOKEN + "tags";

	String TOKEN_GLOBALID = UNIT_DUST + DUST_SEP_TOKEN + "globalId";
	String TOKEN_NAME = UNIT_DUST + DUST_SEP_TOKEN + "name";
	String TOKEN_DESC = UNIT_DUST + DUST_SEP_TOKEN + "desc";
	String TOKEN_PAYLOAD = UNIT_DUST + DUST_SEP_TOKEN + "payload";
	String TOKEN_POSTFIX = UNIT_DUST + DUST_SEP_TOKEN + "postfix";

	String TOKEN_ACCESS = UNIT_DUST + DUST_SEP_TOKEN + "access";
	String TOKEN_USER = UNIT_DUST + DUST_SEP_TOKEN + "user";
	String TOKEN_PASSWORD = UNIT_DUST + DUST_SEP_TOKEN + "password";
	String TOKEN_AUTH = UNIT_DUST + DUST_SEP_TOKEN + "auth";

	String TOKEN_MAIL = UNIT_DUST + DUST_SEP_TOKEN + "mail";

	String TOKEN_KEY = UNIT_DUST + DUST_SEP_TOKEN + "key";
	String TOKEN_AUTHOR = UNIT_DUST + DUST_SEP_TOKEN + "author";
	String TOKEN_UNIT = UNIT_DUST + DUST_SEP_TOKEN + "unit";
	String TOKEN_META = UNIT_DUST + DUST_SEP_TOKEN + "meta";
	String TOKEN_DATA = UNIT_DUST + DUST_SEP_TOKEN + "data";

	String TOKEN_PARENT = UNIT_DUST + DUST_SEP_TOKEN + "parent";
	String TOKEN_NEXT = UNIT_DUST + DUST_SEP_TOKEN + "next";
	String TOKEN_TARGET = UNIT_DUST + DUST_SEP_TOKEN + "target";
	String TOKEN_MEMBERS = UNIT_DUST + DUST_SEP_TOKEN + "members";
	String TOKEN_CHILDMAP = UNIT_DUST + DUST_SEP_TOKEN + "childMap";
	String TOKEN_FILTER = UNIT_DUST + DUST_SEP_TOKEN + "filter";
	String TOKEN_ROOT = UNIT_DUST + DUST_SEP_TOKEN + "root";
	String TOKEN_INDEX = UNIT_DUST + DUST_SEP_TOKEN + "index";
	String TOKEN_MAPPING = UNIT_DUST + DUST_SEP_TOKEN + "mapping";
	String TOKEN_CONSTS = UNIT_DUST + DUST_SEP_TOKEN + "consts";
	String TOKEN_OPTIONS = UNIT_DUST + DUST_SEP_TOKEN + "options";
	String TOKEN_DEFAULT = UNIT_DUST + DUST_SEP_TOKEN + "default";
	String TOKEN_SEPARATOR = UNIT_DUST + DUST_SEP_TOKEN + "separator";

	String TOKEN_POSITION = UNIT_DUST + DUST_SEP_TOKEN + "position";
	String TOKEN_SPAN = UNIT_DUST + DUST_SEP_TOKEN + "span";
	String TOKEN_RANGE = UNIT_DUST + DUST_SEP_TOKEN + "range";

	String TOKEN_TYPE_APP = UNIT_DUST + DUST_SEP_TOKEN + "Application";
	String TOKEN_TYPE_AGENT = UNIT_DUST + DUST_SEP_TOKEN + "Agent";
	String TOKEN_TYPE_SERVICE = UNIT_DUST + DUST_SEP_TOKEN + "Service";
	String TOKEN_TYPE_RELEASEONSHUTDOWN = UNIT_DUST + DUST_SEP_TOKEN + "releaseOnShutdown";

	String TOKEN_RESULT_REJECT = UNIT_DUST + DUST_SEP_TOKEN + "Reject";
	String TOKEN_RESULT_PASS = UNIT_DUST + DUST_SEP_TOKEN + "Pass";
	String TOKEN_RESULT_READ = UNIT_DUST + DUST_SEP_TOKEN + "Read";
	String TOKEN_RESULT_READACCEPT = UNIT_DUST + DUST_SEP_TOKEN + "ReadAccept";
	String TOKEN_RESULT_ACCEPT = UNIT_DUST + DUST_SEP_TOKEN + "Accept";

	String TOKEN_EVENT = UNIT_DUST + DUST_SEP_TOKEN + "Event";
	String TOKEN_EVENT_DATE = UNIT_DUST + DUST_SEP_TOKEN + "date";
	String TOKEN_EVENT_TIME = UNIT_DUST + DUST_SEP_TOKEN + "time";
	String TOKEN_EVENT_DURATION = UNIT_DUST + DUST_SEP_TOKEN + "duration";
	String TOKEN_EVENT_DURATION_UNIT = UNIT_DUST + DUST_SEP_TOKEN + "durationUnit";
	String TOKEN_EVENT_DURATION_UNIT_MSEC = UNIT_DUST + DUST_SEP_TOKEN + "unitMsec";

	String TOKEN_LEVEL_TRACE = UNIT_DUST + DUST_SEP_TOKEN + "Trace";
	String TOKEN_LEVEL_INFO = UNIT_DUST + DUST_SEP_TOKEN + "Info";
	String TOKEN_LEVEL_WARNING = UNIT_DUST + DUST_SEP_TOKEN + "Warning";
	String TOKEN_LEVEL_ERROR = UNIT_DUST + DUST_SEP_TOKEN + "Error";

	String TOKEN_MANDATORY = UNIT_DUST + DUST_SEP_TOKEN + "mandatory";
	String TOKEN_OPTIONAL = UNIT_DUST + DUST_SEP_TOKEN + "optional";
	String TOKEN_APPEARS = UNIT_DUST + DUST_SEP_TOKEN + "appears";

	String TOKEN_FINAL = UNIT_DUST + DUST_SEP_TOKEN + "final";
	String TOKEN_READABLETO = UNIT_DUST + DUST_SEP_TOKEN + "readableTo";

	String TOKEN_LASTCHANGED = UNIT_DUST + DUST_SEP_TOKEN + "lastChanged";
	
	String TOKEN_STREAM = UNIT_DUST + DUST_SEP_TOKEN + "Stream";
//	String TOKEN_STREAM_REF = DUST_UNIT_DUST + DUST_SEP_TOKEN + "StreamRef";
	
	String TOKEN_STREAM_IMAGE = UNIT_DUST + DUST_SEP_TOKEN + "Image";
	
	

	String TOKEN_TEXT_DOC = UNIT_TEXT + DUST_SEP_TOKEN + "Document";
	String TOKEN_TEXT_BLOCK = UNIT_TEXT + DUST_SEP_TOKEN + "Block";
	String TOKEN_TEXT_STRING = UNIT_TEXT + DUST_SEP_TOKEN + "String";
	String TOKEN_TEXT_TEXT = UNIT_TEXT + DUST_SEP_TOKEN + "text";
	String TOKEN_TEXT_ORPHANS = UNIT_TEXT + DUST_SEP_TOKEN + "orphans";
	
	String TOKEN_TEXT_STYLE = UNIT_TEXT + DUST_SEP_TOKEN + "Style";
	String TOKEN_TEXT_STYLE_DEF = UNIT_TEXT + DUST_SEP_TOKEN + "styleDef";
	String TOKEN_TEXT_STYLES = UNIT_TEXT + DUST_SEP_TOKEN + "styles";

	String TOKEN_TEXT_LANG = UNIT_TEXT + DUST_SEP_TOKEN + "lang";
	
	String TOKEN_TEXT_TRANSCLUSION = UNIT_DUST + DUST_SEP_TOKEN + "Transclusion";


	String TOKEN_TEXT_GROUP = UNIT_TEXT + DUST_SEP_TOKEN + "group";
	String TOKEN_TEXT_GROUP_BULLET = UNIT_TEXT + DUST_SEP_TOKEN + "groupBullet";
	String TOKEN_TEXT_GROUP_NUMBER = UNIT_TEXT + DUST_SEP_TOKEN + "groupNumber";
	String TOKEN_TEXT_GROUP_INLINE = UNIT_TEXT + DUST_SEP_TOKEN + "groupInline";

	String TOKEN_LAYOUT_RESPONSIVE = UNIT_LAYOUT + DUST_SEP_TOKEN + "Responsive";
	String TOKEN_LAYOUT_TABLE = UNIT_LAYOUT + DUST_SEP_TOKEN + "Table";
	String TOKEN_LAYOUT_CELL = UNIT_LAYOUT + DUST_SEP_TOKEN + "Cell";
	String TOKEN_LAYOUT_LAYOUT = UNIT_LAYOUT + DUST_SEP_TOKEN + "layout";
	String TOKEN_LAYOUT_LAYOUT_OTPIONS = UNIT_LAYOUT + DUST_SEP_TOKEN + "layoutOptions";

	String TOKEN_GEOMETRY_NODE = UNIT_GEOMETRY + DUST_SEP_TOKEN + "Node";

	String TOKEN_LANG_LANGUAGE = UNIT_LANG + DUST_SEP_TOKEN + "Language";
	String TOKEN_LANG_SUPPORTED = UNIT_LANG + DUST_SEP_TOKEN + "supported";

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
