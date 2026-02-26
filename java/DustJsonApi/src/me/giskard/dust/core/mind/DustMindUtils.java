package me.giskard.dust.core.mind;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;

//@SuppressWarnings({ "unchecked" })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMindUtils implements DustMindConsts {

	public static Integer getUnitSize(DustHandle unit) {
		return Dust.access(DustAccess.Peek, 0, unit, TOKEN_UNIT_OBJECTS, KEY_SIZE);
	}

	public static Iterable<DustHandle> getUnitMembers(DustHandle unit) {
		Map<String, DustHandle> m = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, unit, TOKEN_UNIT_REFS);
		return m.values();
	}

	public static Iterable<String> getAttNames(DustHandle ob) {
		Iterable<String> ret = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, ob, KEY_MAP_KEYS);
		return ret;
	}

	public static void loadData(DustHandle target, DustHandle from, boolean deep, String... atts) {
		if (null == atts) {
			return;
		}

		DustMindAgent mind = ((DustMindHandle) target).mind;
		Map mFrom = mind.getContent(from);
		Map mTarget = mind.getContent(target);

		if (0 == atts.length) {
			for (String a : getAttNames(from)) {
				mTarget.put(a, copy(mFrom.get(a), deep));
			}
		} else {
			for (String a : atts) {
				mTarget.put(a, copy(mFrom.get(a), deep));
			}
		}
	}

	public static Object copy(Object from, boolean deep) {
		Object ret = from;

		if (deep) {
			try {
				if (from instanceof Collection) {
					ret = (Collection) from.getClass().getDeclaredConstructor().newInstance();
					for (Object o : (Collection) from) {
						((Collection) ret).add(copy(o, deep));
					}
				} else if (from instanceof Map) {
					ret = (Map) from.getClass().getDeclaredConstructor().newInstance();
					for (Object o : ((Map) from).entrySet()) {
						Map.Entry e = (Entry) o;
						((Map) ret).put(e.getKey(), copy(e.getValue(), deep));
					}
				}
			} catch (Throwable e) {
				DustException.wrap(e, "In deep copy");
			}
		}

		return ret;
	}

	public static Map getValues(DustHandle from, Map target, boolean cutPrefix, String... atts) {
		if (null == target) {
			target = new HashMap();
		} else {
			target.clear();
		}

		if (null != atts) {
			Map mFrom = ((DustMindHandle) from).mind.getContent(from);

			if (0 == atts.length) {
				for (String a : getAttNames(from)) {
					String k = cutPrefix ? a.substring(a.indexOf(DUST_SEP_TOKEN) + 1) : a;
					target.put(k, mFrom.get(a));
				}
			} else {
				for (String a : atts) {
					String k = cutPrefix ? a.substring(a.indexOf(DUST_SEP_TOKEN) + 1) : a;
					target.put(k, mFrom.get(a));
				}
			}
		}

		return target;
	}

}
