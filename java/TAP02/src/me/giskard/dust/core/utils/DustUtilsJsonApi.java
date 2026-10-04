package me.giskard.dust.core.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.dust.api.DustException;
import me.giskard.dust.core.Dust;
import me.giskard.dust.core.machine.DustMachineConsts;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.tokens_new.giskard_me.DustGenHandles_dust_1;
import me.giskard.tokens_new.giskard_me.DustGenHandles_mind_1;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustUtilsJsonApi implements DustMachineConsts, DustUtilsConstsJson, DustGenHandles_mind_1, DustGenHandles_dust_1 {

	private static final Set<Object> SKIP_KEYS = new HashSet<Object>();


	public static void storeUnit(DustHandle unit, OutputStream os) throws Exception {
		Map<String, Object> target = storeUnit(unit);
		DustUtilsJson.writeJson(os, target, DUST_CHARSET_UTF8);
	}

	public static Map<String, Object> storeUnit(DustHandle unit) {
		SKIP_KEYS.add(HANDLE_MIND_ATT_ID);
		SKIP_KEYS.add(HANDLE_MIND_ATT_TYPE);
		SKIP_KEYS.add(HANDLE_MIND_ATT_UNIT);
		SKIP_KEYS.add(HANDLE_MIND_ATT_UNIT_STATE);
		SKIP_KEYS.add(HANDLE_DUST_ATT_WRAPPEDOBJECT);
		SKIP_KEYS.add(HANDLE_DUST_ATT_UNIT_OBJECTS);
		SKIP_KEYS.add(HANDLE_DUST_ATT_UNIT_HANDLES);
		SKIP_KEYS.add(HANDLE_DUST_ATT_UNIT_REFS);

		Map<String, Object> target = new HashMap<>();

		Dust.access(DustAccess.Set, JSONAPI_VERSION, target, JsonApiMember.jsonapi, JsonApiMember.version);

		ArrayList data = new ArrayList();

		Dust.access(DustAccess.Set, data, target, JsonApiMember.data);

		for (DustHandle h : DustMachineUtils.getUnitMembers(unit, null)) {
			Map<String, Object> item = storeFull(h);
			data.add(item);
		}

		Dust.access(DustAccess.Set, data.size(), target, JsonApiMember.meta, JsonApiMember.count);
		Dust.access(DustAccess.Set, DustUtils.strTime(), target, JsonApiMember.meta, "date");
		Dust.access(DustAccess.Set, storeFull(unit), target, JsonApiMember.meta, EXT_JSONAPI_UNIT_INFO);

		return target;
	}

	private static Map<String, Object> storeHead(DustHandle h) {
		Comparator<String> ic = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return JsonApiMember.valueOf(o1).compareTo(JsonApiMember.valueOf(o2));
			}
		};
		Map<String, Object> item = new TreeMap<>(ic);
		item.put(JsonApiMember.type.name(), h.getType().getId());
		item.put(JsonApiMember.id.name(), h.getId());
		return item;
	}
	
	private static Map storeRelation(Map<String, Object> item, String key, Object val, Object metaKey) {
		Map head = storeHead((DustHandle) val);
		
		Map m = DustUtils.safeGet(item, SORTEDMAP_CREATOR, JsonApiMember.relationships.name());

		if (null == metaKey) {
			Dust.access(DustAccess.Set, head, m, key, JsonApiMember.data);
		} else {
			Dust.access(DustAccess.Insert, head, m, key, JsonApiMember.data, KEY_ADD);

			if (!DustUtils.isEqual(-1, metaKey)) {
				Dust.access(DustAccess.Set, metaKey, head, JsonApiMember.meta, EXT_JSONAPI_KEY);
//			} else {
//				Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "hmm");
			}
		}
		return head;
	}

	private static Map<String, Object> storeFull(DustHandle h) {
		Map<String, Object> item = storeHead(h);

		for (Object o : (Iterable<String>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, KEY_MAP_KEYS)) {
			DustHandle hk = (o instanceof DustHandle) ? (DustHandle)o : null;
			String key = (null == hk) ? (String) o : hk.getId();
			
			if (SKIP_KEYS.contains(key)) {
				continue;
			}
			Object val = Dust.access(DustAccess.Peek, null, h, (null == hk) ? key : hk);

			if (val instanceof DustHandle) {
				storeRelation(item, key, val, null);
				val = null;
			} else if (val instanceof Collection) {
				Collection coll = (Collection) val;
				if (coll.isEmpty()) {
					continue;
				}

				Object sample = Dust.access(DustAccess.Peek, null, coll, 0);
				if (sample instanceof DustHandle) {
					int idx = 0;
					
					if (coll instanceof Set) {
						idx = -1;
						coll = new TreeSet(coll);
					}
					for (DustHandle co : (Collection<DustHandle>) coll) {
						storeRelation(item, key, co, (-1 == idx) ? -1 : idx++);
					}
					val = null;
				}
			} else if (val instanceof Map) {
				Map coll = (Map) val;
				if (coll.isEmpty()) {
					continue;
				}
				for (Map.Entry<String, Object> ce : ((Map<String, Object>) coll).entrySet()) {
					Object cv = ce.getValue();
					if (cv instanceof DustHandle) {
						storeRelation(item, key, cv, ce.getKey());
						val = null;
					} else {
						break;
					}
				}
			}

			if (null != val) {
				Map m = DustUtils.safeGet(item, SORTEDMAP_CREATOR, JsonApiMember.attributes.name());
				Dust.access(DustAccess.Set, val, m, key);
			}
		}
		
		return item;
	}

	public static void loadUnit(DustHandle unit, InputStream is) throws Exception {
		if (null == is) {
			return;
		}
		Map<String, Object> content = DustUtilsJson.readJson(is, DUST_CHARSET_UTF8);

		String str;

		str = DustUtils.simpleGet(content, JsonApiMember.jsonapi, JsonApiMember.version);

		if (!DustUtils.isEqual(JSONAPI_VERSION, str)) {
			DustException.wrap(null, "Loading JSON:API version", str, "does not match", JSONAPI_VERSION);
		}

		Map<String, Object> unitData = DustUtils.simpleGet(content, JsonApiMember.meta, EXT_JSONAPI_UNIT_INFO);
		if (null != unitData) {
			loadDataContent(unit, unit, unitData, false);
		}
		
		loadSegment(unit, content, JsonApiMember.data);
		loadSegment(unit, content, JsonApiMember.included);

//		for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content, JsonApiMember.data))) {
//			loadDataSegment(unit, ca, false);
//		}
//		for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content, JsonApiMember.included))) {
//			loadDataSegment(unit, ca, true);
//		}
	}

	public static void loadSegment(DustHandle unit, Map<String, Object> content, JsonApiMember member) {
		Collection<Map<String, Object>> segment = DustUtils.simpleGet(content, member);
		if ( null != segment ) {
			for (Map<String, Object> ca : segment) {
				loadDataSegment(unit, ca, JsonApiMember.included == member);
			}
		}
	}

	private static void loadDataSegment(DustHandle unit, Map<String, Object> data, boolean included) {
		String type = DustUtils.simpleGet(data, JsonApiMember.type);
		DustHandle tType = Dust.getHandle(unit, HANDLE_MIND_ASP_ASPECT, type, true);

		String id = DustUtils.simpleGet(data, JsonApiMember.id);
		DustHandle target = Dust.getHandle(unit, tType, id, true);

		loadDataContent(target, unit, data, included);
	}

	private static void loadDataContent(DustHandle target, DustHandle unit, Map<String, Object> data, boolean included) {
		Map<String, Object> atts = DustUtils.simpleGet(data, JsonApiMember.attributes);
		if (null != atts) {
			for (Map.Entry<String, Object> ae : atts.entrySet()) {
				String rk = ae.getKey();
				DustHandle tAtt = Dust.getHandle(unit, HANDLE_MIND_ASP_ATTRIBUTE, rk, true);

				if (SKIP_KEYS.contains(rk)) {
					continue;
				}
				Dust.access(DustAccess.Set, ae.getValue(), target, tAtt);
			}
		}

		Map<String, Object> rels = DustUtils.simpleGet(data, JsonApiMember.relationships);
		if (null != rels) {
			for (Map.Entry<String, Object> re : rels.entrySet()) {
				String rk = re.getKey();
				DustHandle tAtt = Dust.getHandle(unit, HANDLE_MIND_ASP_ATTRIBUTE, rk, true);

				Object rv = re.getValue();
				Object rd = DustUtils.simpleGet(rv, JsonApiMember.data);

				if (rd instanceof Collection) {
					for (Object rdd : (Collection) rd) {
						String rt = DustUtils.simpleGet(rdd, JsonApiMember.type);
						DustHandle tTypeRef = Dust.getHandle(unit, HANDLE_MIND_ASP_ASPECT, rt, true);

						String ri = DustUtils.simpleGet(rdd, JsonApiMember.id);

						Object key = DustUtils.simpleGet(rdd, JsonApiMember.meta, EXT_JSONAPI_KEY);

						DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, true);
//						DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, DustOptCreate.Reference);

						if (null == key) {
							Dust.access(DustAccess.Insert, rh, target, tAtt);
						} else {
							Dust.access(DustAccess.Set, rh, target, tAtt, (key instanceof Number) ? ((Number) key).intValue() : key);
						}
					}
				} else {
					String rt = DustUtils.simpleGet(rd, JsonApiMember.type);
					DustHandle tTypeRef = Dust.getHandle(unit, HANDLE_MIND_ASP_ASPECT, rt, true);
					String ri = DustUtils.simpleGet(rd, JsonApiMember.id);

					DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, true);
//					DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, DustOptCreate.Reference);

					Dust.access(DustAccess.Set, rh, target, tAtt);
				}
			}
		}
	}
}
