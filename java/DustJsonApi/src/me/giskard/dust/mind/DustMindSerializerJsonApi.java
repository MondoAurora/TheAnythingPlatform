package me.giskard.dust.mind;

import java.io.File;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsConstsJson;
import me.giskard.dust.utils.DustUtilsJson;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindSerializerJsonApi extends DustAgent implements DustMindConsts, DustUtilsConstsJson, DustStreamConsts {
	
	private static final Set<String> SKIP_KEYS = new HashSet<String>();
	
	static {
		SKIP_KEYS.add(TOKEN_ID);
		SKIP_KEYS.add(TOKEN_TYPE);
		SKIP_KEYS.add(TOKEN_UNIT);
	}
	
	public DustMindSerializerJsonApi() {
	}

	@Override
	protected Object process(DustAccess access) throws Exception {

		String unitId = Dust.access(DustAccess.Peek, null, null, TOKEN_KEY);
		DustObject unit = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);
		Dust.access(DustAccess.Delete, null, null, TOKEN_DATA);
		File f = null;

		if (null == unit) {
			unit = Dust.getUnit(unitId, true);
		}

		if (!DustUtils.isEmpty(unitId)) {
			String fn = Dust.access(DustAccess.Peek, unitId, null, TOKEN_ALIAS);
			if (fn.contains("{")) {
				fn = MessageFormat.format(fn, unitId);
			}
			String fileName = DustUtils.sbAppend(null, "/", false, Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ROOTFOLDER), fn + DUST_EXT_JSON).toString();

			f = new File(fileName);
		}
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
		switch (cmd) {
		case TOKEN_CMD_LOAD:
			loadFile(unit, f);

			break;
		case TOKEN_CMD_SAVE:
			Map<String, Object> target = new HashMap<>();

			Dust.access(DustAccess.Set, JSONAPI_VERSION, target, JsonApiMember.jsonapi, JsonApiMember.version);

			ArrayList data = new ArrayList();

			Dust.access(DustAccess.Set, data, target, JsonApiMember.data);

			for (DustObject o : DustMindUtils.getUnitMembers(unit)) {
				Map<String, Object> item = getJsonHead(o);

				for (String key : (Iterable<String>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, o, KEY_MAP_KEYS)) {
					if ( SKIP_KEYS.contains(key)) {
						continue;
					}
					Object val = Dust.access(DustAccess.Peek, null, o, key);

					if (val instanceof DustObject) {
						addRelation(item, key, val, null);
						val = null;
					} else if (val instanceof Collection) {
						Collection coll = (Collection) val;
						if (coll.isEmpty()) {
							continue;
						}

//						if ( "MAY".equals(key)) {
//							Dust.log(TOKEN_LEVEL_TRACE, "hmm");
//						}

						Object sample = Dust.access(DustAccess.Peek, null, coll, 0);
						if (sample instanceof DustObject) {
							int idx = (coll instanceof Set) ? -1 : 0;
							for (DustObject co : (Collection<DustObject>) coll) {
								addRelation(item, key, co, (-1 == idx) ? null : idx++);
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
							if (cv instanceof DustObject) {
								addRelation(item, key, cv, ce.getKey());
								val = null;
							} else {
								break;
							}
						}
					}

					if (null != val) {
						Dust.access(DustAccess.Set, val, item, JsonApiMember.attributes, key);
					}
				}

				data.add(item);
			}

			Dust.access(DustAccess.Set, data.size(), target, JsonApiMember.meta, JsonApiMember.count);

			if (null == f) {
				Writer w = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_WRITER);
				DustUtilsJson.writeJson(w, target);
			} else {
				DustUtilsJson.writeJson(f, target);
			}
			break;

		}
		return null;
	}

	private Map addRelation(Map<String, Object> item, String key, Object val, Object metaKey) {
		Map head = getJsonHead((DustObject) val);
		Dust.access(DustAccess.Insert, head, item, JsonApiMember.relationships, key, JsonApiMember.data, KEY_ADD);

		if (null != metaKey) {
			Dust.access(DustAccess.Set, metaKey, head, JsonApiMember.meta, TOKEN_JSONAPI_KEY);
		} else {
//			Dust.log(TOKEN_LEVEL_TRACE, "hmm");
		}
		return head;
	}

	private Map<String, Object> getJsonHead(DustObject o) {
		Map<String, Object> item = new HashMap<>();
		item.put(JsonApiMember.type.name(), o.getType().getId());
		item.put(JsonApiMember.id.name(), o.getId());
		return item;
	}

	static void loadFile(DustObject unit, File f) throws Exception {
		if (f.isFile()) {
			Dust.log(TOKEN_LEVEL_INFO, "Loading unit", unit, "from file", f.getCanonicalPath());
			Map<String, Object> content = DustUtilsJson.readJson(f);

			String str;

			str = DustUtils.simpleGet(content, JsonApiMember.jsonapi, JsonApiMember.version);

			if (!DustUtils.isEqual(JSONAPI_VERSION, str)) {
				DustException.wrap(null, "Loading JSON:API version", str, "does not match", JSONAPI_VERSION);
			}

			for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content,
					JsonApiMember.data))) {
				loadData(unit, ca, false);
			}
			for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content,
					JsonApiMember.included))) {
				loadData(unit, ca, true);
			}
		}
	}

	static void loadData(DustObject unit, Map<String, Object> data, boolean included) {
		DustObject metaType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);
		DustObject metaAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
		
		String type = DustUtils.simpleGet(data, JsonApiMember.type);
		DustObject tType = Dust.getObject(unit, metaType, type, DustOptCreate.Meta);

		String id = DustUtils.simpleGet(data, JsonApiMember.id);
		DustObject target = Dust.getObject(unit, tType, id, DustOptCreate.Primary);

		Map<String, Object> atts = DustUtils.simpleGet(data, JsonApiMember.attributes);
		if (null != atts) {
			for (Map.Entry<String, Object> ae : atts.entrySet()) {
				String rk = ae.getKey();
				DustObject tAtt = Dust.getObject(unit, metaAtt, rk, DustOptCreate.Meta);
				Dust.access(DustAccess.Set, ae.getValue(), target, tAtt);
			}
		}

		Map<String, Object> rels = DustUtils.simpleGet(data, JsonApiMember.relationships);
		if (null != rels) {
			for (Map.Entry<String, Object> re : rels.entrySet()) {
				String rk = re.getKey();
				DustObject tAtt = Dust.getObject(unit, metaAtt, rk, DustOptCreate.Meta);

				Object rv = re.getValue();
				Object rd = DustUtils.simpleGet(rv, JsonApiMember.data);

				if (rd instanceof Collection) {
					for (Object rdd : (Collection) rd) {
						String rt = DustUtils.simpleGet(rdd, JsonApiMember.type);
						DustObject tTypeRef = Dust.getObject(unit, metaType, rt, DustOptCreate.Meta);

						String ri = DustUtils.simpleGet(rdd, JsonApiMember.id);

						Object key = DustUtils.simpleGet(rdd, JsonApiMember.meta, TOKEN_JSONAPI_KEY);

						DustObject ro = Dust.getObject(unit, tTypeRef, ri, DustOptCreate.Reference);

						if (null == key) {
							Dust.access(DustAccess.Insert, ro, target, tAtt);
						} else {
							Dust.access(DustAccess.Set, ro, target, tAtt, (key instanceof Number) ? ((Number) key).intValue() : key);
						}
					}
				} else {
					String rt = DustUtils.simpleGet(rd, JsonApiMember.type);
					DustObject tTypeRef = Dust.getObject(unit, metaType, rt, DustOptCreate.Meta);
					String ri = DustUtils.simpleGet(rd, JsonApiMember.id);

					DustObject ro = Dust.getObject(unit, tTypeRef, ri, DustOptCreate.Reference);

					Dust.access(DustAccess.Set, ro, target, tAtt);
				}
			}
		}
	}
}
