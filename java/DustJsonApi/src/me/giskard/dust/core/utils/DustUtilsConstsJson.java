package me.giskard.dust.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindUtils;

public interface DustUtilsConstsJson extends DustConsts {

//https://jsonapi.org/format/
	String JSONAPI_VERSION = "1.1";

	String JSONAPI_IDSEP = "-";

	String EXT_JSON_NULL = "null";
	String EXT_JSON_TRUE = "true";
	String EXT_JSON_FALSE = "false";

	String EXT_JSONAPI_KEY = "key";

//@formatter:off
	enum JsonApiMember {
		jsonapi, version, ext, profile,
		
		meta, links, type, describedby,
		
		data, errors, included, 

		id, lid, attributes, relationships,
		
		self, related,
		href, rel, title, hreflang,
		first, last, prev, next, 
		
		detail, count,
		
		;
		
		public static final EnumSet<JsonApiMember> TOP = EnumSet.of(jsonapi, data, errors, meta, links, included);
		public static final EnumSet<JsonApiMember> DATA = EnumSet.of(data, included);
		public static final EnumSet<JsonApiMember> HEADER = EnumSet.of(version, ext, profile);
	};
	
	enum JsonApiParam {
		include, fields, sort, filter, page, limit, offset
	}

//@formatter:on	

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public class JsonApiFilter {

		private String condition;
		private DustHandle handle;

		Map values = new HashMap();

		public JsonApiFilter(String condition) {
			this.condition = condition;
		}

		public void setCondition(String condition) {
			this.condition = condition;
		}

		public String getCondition() {
			return condition;
		}

		public void setHandle(DustHandle h) {
			this.handle = h;
			values = DustMindUtils.getValues(h, values, true);

			Object typeAtts = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h.getType(), TOKEN_CHILDMAP, KEY_MAP_KEYS);
			if (typeAtts instanceof Collection) {
				for (Object att : ((Collection) typeAtts)) {
					values.putIfAbsent(att, null);
				}
			}
		}

		public Map getValues() {
			return values;
		};

		public Object get(Object a) {
			Object ret = Dust.access(DustAccess.Peek, null, handle, a);
			return ret;
		};

		public boolean equals(Object a, Object b) {
			return DustUtils.isEqual(a, b);
		};

		public boolean lessThan(Object a, Object b) {
			return 0 < ((Comparable) a).compareTo(b);
		};

		public boolean lessOrEqual(Object a, Object b) {
			return 0 <= ((Comparable) a).compareTo(b);
		};

		public boolean greaterThan(Object a, Object b) {
			return 0 > ((Comparable) a).compareTo(b);
		};

		public boolean greaterOrEqual(Object a, Object b) {
			return 0 >= ((Comparable) a).compareTo(b);
		};

		// to avoid name clash with MVEL contains operator...
		public boolean contain(Object a, Object b) {
			if (a instanceof String) {
				return ((String) a).contains((String) b);
			} else if (a instanceof Collection) {
				return ((Collection) a).contains((String) b);
			}

			return false;
		};

		public boolean startsWith(Object a, Object b) {
			return ((String) a).startsWith((String) b);
		};

		public boolean endsWith(Object a, Object b) {
			return ((String) a).endsWith((String) b);
		};

		public boolean isType(Object a, Object b) {
			DustException.wrap(null, "TBD");
			return false;
		};

		public boolean isType(Object a, Object b, Object f) {
			DustException.wrap(null, "TBD");
			return false;
		};

		public int count(Object a) {
			DustException.wrap(null, "TBD");
			return 0;
		};

		public boolean any(Object a, Object... b) {
			for (Object m : b) {
				if (DustUtils.isEqual(a, m)) {
					return true;
				}
			}
			return false;
		};

		public boolean has(Object a) {
			Object o = Dust.access(DustAccess.Peek, null, handle, a);
			return null != o;
		};

		public boolean not(Object a) {
			return !((Boolean) a);
		};

		public boolean or(Object... b) {
			for (Object m : b) {
				if ((Boolean) m) {
					return true;
				}
			}
			return false;
		};

		public boolean and(Object... b) {
			for (Object m : b) {
				if (!(Boolean) m) {
					return false;
				}
			}
			return true;
		};
	}

}
