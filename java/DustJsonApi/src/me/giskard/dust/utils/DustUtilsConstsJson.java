package me.giskard.dust.utils;

import java.util.EnumSet;
import java.util.Map;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAccess;
import me.giskard.dust.DustConsts.DustObject;
import me.giskard.dust.DustException;
import me.giskard.dust.mind.DustMindUtils;

public interface DustUtilsConstsJson {

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
		
		public final String condition;
		private DustObject ob;
		Map values;
		
		public JsonApiFilter(String condition) {
			this.condition = condition;
		}
		
		public void setObject(DustObject o) {
			this.ob = o;
			values = DustMindUtils.getValues(o, values, true);
		}
		
		public Map getValues() {
			return values;
		};
		
		public Object get(Object a) {
			Object ret = Dust.access(DustAccess.Peek, null, ob, a);
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

		public boolean contains(Object a, Object b) {
			return ((String) a).contains((String) b);
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

		public int  count(Object a) {
			DustException.wrap(null, "TBD");
			return 0;
		};

		public boolean any(Object a, Object... b ) {
			for ( Object m : b ) {
				if ( DustUtils.isEqual(a, m) ) {
					return true;
				}
			}
			return false;
		};

		public boolean has(Object a) {
			return null != Dust.access(DustAccess.Peek, null, ob, a);
		};

		public boolean not(Object a) {
			return !((Boolean)a);
		};

		public boolean or(Object... b) {
			for ( Object m : b ) {
				if ( (Boolean)m ) {
					return true;
				}
			}
			return false;
		};

		public boolean and(Object... b) {
			for ( Object m : b ) {
				if ( !(Boolean)m ) {
					return false;
				}
			}
			return true;
		};
	}

}
