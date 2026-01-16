package me.giskard.dust.mind;

import java.util.Collections;
import java.util.Map;

import me.giskard.dust.Dust;

@SuppressWarnings({ "unchecked" })
//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMindUtils implements DustMindConsts {
		
	public static Integer getUnitSize(DustObject unit) {
		return Dust.access(DustAccess.Peek, 0, unit, TOKEN_MEMBERS, KEY_SIZE);
	}
	
	public static Iterable<DustObject> getUnitMembers(DustObject unit) {
		Map<String, DustObject> m = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, unit, TOKEN_MEMBERS);
		return m.values();
	}

	public static void loadObject(DustObject target, DustObject from, boolean deep, String... atts) {
		if (deep) {
			Dust.log(TOKEN_LEVEL_WARNING, "DustKBObject deep load not supported");
		}
		
		if ( null == atts ) {
			return;
		}

		DustMindIdea t = (DustMindIdea) target;
		DustMindIdea kbo = (DustMindIdea) from;

		if (0 == atts.length) {
			for (String a : (Iterable<String>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, from, KEY_MAP_KEYS)) {
				t.content.put(a, kbo.content.get(a));
			}
		} else {
			for (String a : atts) {
				t.content.put(a, kbo.content.get(a));
			}
		}
	}


}
