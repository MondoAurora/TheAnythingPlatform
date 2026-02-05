package me.giskard.dust.mind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.Dust;

//@SuppressWarnings({ "unchecked" })
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMindUtils implements DustMindConsts {

	public static Integer getUnitSize(DustObject unit) {
		return Dust.access(DustAccess.Peek, 0, unit, TOKEN_UNIT_OBJECTS, KEY_SIZE);
	}

	public static Iterable<DustObject> getUnitMembers(DustObject unit) {
		Map<String, DustObject> m = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, unit, TOKEN_UNIT_REFS);
		return m.values();
	}

	public static Iterable<String> getAttNames(DustObject ob) {
		Iterable<String> ret = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, ob, KEY_MAP_KEYS);
		return ret;
	}

	public static void loadObject(DustObject target, DustObject from, boolean deep, String... atts) {
		if (deep) {
			Dust.log(TOKEN_LEVEL_WARNING, "DustKBObject deep load not supported");
		}

		if (null == atts) {
			return;
		}
		
		DustMindAgent mind = ((DustMindObject) target).mind;
		Map mFrom = mind.getContent(from);
		Map mTarget = mind.getContent(target);

		if (0 == atts.length) {
			for (String a : getAttNames(from)) {
				mTarget.put(a, mFrom.get(a));
			}
		} else {
			for (String a : atts) {
				mTarget.put(a, mFrom.get(a));
			}
		}
	}

	public static Map getValues(DustObject from, Map target, boolean cutPrefix, String... atts) {
		if (null == target) {
			target = new HashMap();
		} else {
			target.clear();
		}

		if (null != atts) {
			Map mFrom = ((DustMindObject) from).mind.getContent(from);

			if (0 == atts.length) {
				for (String a : getAttNames(from)) {
					String k = cutPrefix  ? a.substring(a.indexOf(DUST_SEP_TOKEN)+1) : a;
					target.put(k, mFrom.get(a));
				}
			} else {
				for (String a : atts) {
					String k = cutPrefix  ? a.substring(a.indexOf(DUST_SEP_TOKEN)+1) : a;
					target.put(k, mFrom.get(a));
				}
			}
		}
		
		return target;
	}

}
