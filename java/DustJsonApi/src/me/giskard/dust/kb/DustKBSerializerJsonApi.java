package me.giskard.dust.kb;

import java.io.File;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsConstsJson;
import me.giskard.dust.utils.DustUtilsJson;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustKBSerializerJsonApi extends DustAgent implements DustKBConsts, DustUtilsConstsJson, DustStreamConsts {

	@Override
	protected Object process(DustAction action) throws Exception {

		String unitId = access(DustAccess.Peek, null, null, TOKEN_KEY);
		DustKBUnit unit = access(DustAccess.Peek, null, null, TOKEN_UNIT);
		access(DustAccess.Delete, null, null, TOKEN_UNIT);
		File f = null;

		if (null == unit) {
			KBStore kb = Dust.getAgent(access(DustAccess.Peek, null, null, TOKEN_KB_KNOWLEDGEBASE));
			unit = (DustKBUnit) kb.getUnit(unitId, true);
		}

		if (!DustUtils.isEmpty(unitId)) {
			String fn = access(DustAccess.Peek, unitId, null, TOKEN_ALIAS);
			if (fn.contains("{")) {
				fn = MessageFormat.format(fn, unitId);
			}
			String fileName = DustUtils.sbAppend(null, "/", false, access(DustAccess.Peek, null, null, TOKEN_STREAM_ROOTFOLDER), fn + DUST_EXT_JSON).toString();

			f = new File(fileName);
		}
		String cmd = access(DustAccess.Peek, null, null, TOKEN_CMD);
		switch (cmd) {
		case TOKEN_CMD_LOAD:
			loadFile(unit, f);

			break;
		case TOKEN_CMD_SAVE:
			Map<String, Object> target = new HashMap<>();

			DustKBUtils.access(DustAccess.Set, JSONAPI_VERSION, target, JsonApiMember.jsonapi, JsonApiMember.version);

			ArrayList data = new ArrayList();

			DustKBUtils.access(DustAccess.Set, data, target, JsonApiMember.data);

			for (KBObject o : unit.objects()) {
				Map<String, Object> item = getJsonHead(o);

				for (String key : (Iterable<String>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, o, KEY_MAP_KEYS)) {
					Object val = DustKBUtils.access(DustAccess.Peek, null, o, key);

					if (val instanceof KBObject) {
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

						Object sample = DustKBUtils.access(DustAccess.Peek, null, coll, 0);
						if (sample instanceof KBObject) {
							int idx = (coll instanceof Set) ? -1 : 0;
							for (KBObject co : (Collection<KBObject>) coll) {
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
							if (cv instanceof KBObject) {
								addRelation(item, key, cv, ce.getKey());
								val = null;
							} else {
								break;
							}
						}
					}

					if (null != val) {
						DustKBUtils.access(DustAccess.Set, val, item, JsonApiMember.attributes, key);
					}
				}

				data.add(item);
			}

			DustKBUtils.access(DustAccess.Set, data.size(), target, JsonApiMember.meta, JsonApiMember.count);

			if (null == f) {
				Writer w = access(DustAccess.Peek, null, null, TOKEN_STREAM_WRITER);
				DustUtilsJson.writeJson(w, target);
			} else {
				DustUtilsJson.writeJson(f, target);
			}
			break;

		}
		return null;
	}

	private Map addRelation(Map<String, Object> item, String key, Object val, Object metaKey) {
		Map head = getJsonHead((KBObject) val);
		DustKBUtils.access(DustAccess.Insert, head, item, JsonApiMember.relationships, key, JsonApiMember.data, KEY_ADD);

		if (null != metaKey) {
			DustKBUtils.access(DustAccess.Set, metaKey, head, JsonApiMember.meta, TOKEN_KEY);
		} else {
//			Dust.log(TOKEN_LEVEL_TRACE, "hmm");
		}
		return head;
	}

	private Map<String, Object> getJsonHead(KBObject o) {
		Map<String, Object> item = new HashMap<>();
		item.put(JsonApiMember.type.name(), o.getType());
		item.put(JsonApiMember.id.name(), o.getId());
		return item;
	}

	static void loadFile(DustKBUnit unit, File f) throws Exception {
		if (f.isFile()) {
			Map<String, Object> content = DustUtilsJson.readJson(f);

			String str;

			str = DustUtils.simpleGet(content, JsonApiMember.jsonapi, JsonApiMember.version);

			if (!DustUtils.isEqual(JSONAPI_VERSION, str)) {
				DustException.wrap(null, "Loading JSON:API version", str, "does not match", JSONAPI_VERSION);
			}

			for (Map<String, Object> ca : ((Collection<Map<String, Object>>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, content,
					JsonApiMember.data))) {
				loadData(unit, ca, false);
			}
			for (Map<String, Object> ca : ((Collection<Map<String, Object>>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, content,
					JsonApiMember.included))) {
				loadData(unit, ca, true);
			}
		}
	}

	static void loadData(KBUnit unit, Map<String, Object> data, boolean included) {
		String type = DustUtils.simpleGet(data, JsonApiMember.type);
		String id = DustUtils.simpleGet(data, JsonApiMember.id);

		KBObject target = unit.getObject(type, id, KBOptCreate.Primary);

		Map<String, Object> atts = DustUtils.simpleGet(data, JsonApiMember.attributes);
		if (null != atts) {
			for (Map.Entry<String, Object> ae : atts.entrySet()) {
				DustKBUtils.access(DustAccess.Set, ae.getValue(), target, ae.getKey());
			}
		}

		Map<String, Object> rels = DustUtils.simpleGet(data, JsonApiMember.relationships);
		if (null != rels) {
			for (Map.Entry<String, Object> re : rels.entrySet()) {
				String rk = re.getKey();
				Object rv = re.getValue();
				Object rd = DustUtils.simpleGet(rv, JsonApiMember.data);

				if (rd instanceof Collection) {
					for (Object rdd : (Collection) rd) {
						String rt = DustUtils.simpleGet(rdd, JsonApiMember.type);
						String ri = DustUtils.simpleGet(rdd, JsonApiMember.id);

						Object key = DustUtils.simpleGet(rdd, JsonApiMember.meta, TOKEN_KEY);

						KBObject ro = unit.getObject(rt, ri, KBOptCreate.Reference);

						if (null == key) {
							DustKBUtils.access(DustAccess.Insert, ro, target, rk);
						} else {
							DustKBUtils.access(DustAccess.Set, ro, target, rk, (key instanceof Number) ? ((Number) key).intValue() : key);
						}
					}
				} else {
					String rt = DustUtils.simpleGet(rd, JsonApiMember.type);
					String ri = DustUtils.simpleGet(rd, JsonApiMember.id);

					KBObject ro = unit.getObject(rt, ri, KBOptCreate.Reference);

					DustKBUtils.access(DustAccess.Set, ro, target, rk);
				}
			}
		}
	}
}
