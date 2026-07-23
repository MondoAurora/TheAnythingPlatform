package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConstsJson;
import me.giskard.dust.mod.utils.DustUtilsJson;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustStreamJsonApiAgent extends DustAgent implements DustMindConsts, DustUtilsConstsJson, DustStreamConsts {

	private static final Set<String> SKIP_KEYS = new HashSet<String>();

	static {
		SKIP_KEYS.add(TOKEN_MIND_ATT_ID);
		SKIP_KEYS.add(TOKEN_MIND_ATT_TYPE);
		SKIP_KEYS.add(TOKEN_MIND_ATT_UNIT);
	}

	public DustStreamJsonApiAgent() {
	}

	@Override
	protected Object process(DustAccess access) throws Exception {

		String unitId = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_KEY);
		DustHandle unit = (null == unitId) ?  Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_DATA) : Dust.getUnit(unitId, true);
		Dust.access(DustAccess.Delete, null, null, TOKEN_MISC_ATT_DATA);

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_CMD);

		switch (cmd) {
		case TOKEN_MISC_TAG_CMD_LOAD:
			InputStream is = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_INPUT);
			loadStream(unit, is);
			break;
		case TOKEN_MISC_TAG_CMD_SAVE:
			OutputStream os = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_OUTPUT);
			Map<String, Object> target = storeUnit(unit);

			DustUtilsJson.writeJson(os, target, DUST_CHARSET_UTF8);
			break;

		}
		return null;
	}

	private static Map storeRelation(Map<String, Object> item, String key, Object val, Object metaKey) {
		Map head = storeHead((DustHandle) val);

		if (null == metaKey) {
			Dust.access(DustAccess.Set, head, item, JsonApiMember.relationships, key, JsonApiMember.data);
		} else {
			Dust.access(DustAccess.Insert, head, item, JsonApiMember.relationships, key, JsonApiMember.data, KEY_ADD);

			if (!DustUtils.isEqual(-1, metaKey)) {
				Dust.access(DustAccess.Set, metaKey, head, JsonApiMember.meta, EXT_JSONAPI_KEY);
			} else {
				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "hmm");
			}
		}
		return head;
	}

	private static Map<String, Object> storeHead(DustHandle h) {
		Map<String, Object> item = new HashMap<>();
		item.put(JsonApiMember.type.name(), h.getType().getId());
		item.put(JsonApiMember.id.name(), h.getId());
		return item;
	}

	public static Map<String, Object> storeUnit(DustHandle unit) {
		Map<String, Object> target = new HashMap<>();

		Dust.access(DustAccess.Set, JSONAPI_VERSION, target, JsonApiMember.jsonapi, JsonApiMember.version);

		ArrayList data = new ArrayList();

		Dust.access(DustAccess.Set, data, target, JsonApiMember.data);

		for (DustHandle h : DustMindUtils.getUnitMembers(unit)) {
			Map<String, Object> item = storeHead(h);

			for (String key : (Iterable<String>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, KEY_MAP_KEYS)) {
				if (SKIP_KEYS.contains(key)) {
					continue;
				}
				Object val = Dust.access(DustAccess.Peek, null, h, key);

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
						int idx = (coll instanceof Set) ? -1 : 0;
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
					Dust.access(DustAccess.Set, val, item, JsonApiMember.attributes, key);
				}
			}

			data.add(item);
		}

		Dust.access(DustAccess.Set, data.size(), target, JsonApiMember.meta, JsonApiMember.count);
		return target;
	}

	static void loadStream(DustHandle unit, InputStream is) throws Exception {
		if (null == is) {
			return;
		}
		Map<String, Object> content = DustUtilsJson.readJson(is, DUST_CHARSET_UTF8);

		String str;

		str = DustUtils.simpleGet(content, JsonApiMember.jsonapi, JsonApiMember.version);

		if (!DustUtils.isEqual(JSONAPI_VERSION, str)) {
			DustException.wrap(null, "Loading JSON:API version", str, "does not match", JSONAPI_VERSION);
		}

		for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content, JsonApiMember.data))) {
			loadDataSegment(unit, ca, false);
		}
		for (Map<String, Object> ca : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, content, JsonApiMember.included))) {
			loadDataSegment(unit, ca, true);
		}
	}

	static void loadDataSegment(DustHandle unit, Map<String, Object> data, boolean included) {

		String type = DustUtils.simpleGet(data, JsonApiMember.type);
		DustHandle tType = Dust.getHandle(unit, TOKEN_MIND_ASP_ASPECT, type, DustOptCreate.Meta);

		String id = DustUtils.simpleGet(data, JsonApiMember.id);
		DustHandle target = Dust.getHandle(unit, tType, id, DustOptCreate.Primary);

		Map<String, Object> atts = DustUtils.simpleGet(data, JsonApiMember.attributes);
		if (null != atts) {
			for (Map.Entry<String, Object> ae : atts.entrySet()) {
				String rk = ae.getKey();
				DustHandle tAtt = Dust.getHandle(unit, TOKEN_MIND_ASP_ATTRIBUTE, rk, DustOptCreate.Meta);
				Dust.access(DustAccess.Set, ae.getValue(), target, tAtt);
			}
		}

		Map<String, Object> rels = DustUtils.simpleGet(data, JsonApiMember.relationships);
		if (null != rels) {
			for (Map.Entry<String, Object> re : rels.entrySet()) {
				String rk = re.getKey();
				DustHandle tAtt = Dust.getHandle(unit, TOKEN_MIND_ASP_ATTRIBUTE, rk, DustOptCreate.Meta);

				Object rv = re.getValue();
				Object rd = DustUtils.simpleGet(rv, JsonApiMember.data);

				if (rd instanceof Collection) {
					for (Object rdd : (Collection) rd) {
						String rt = DustUtils.simpleGet(rdd, JsonApiMember.type);
						DustHandle tTypeRef = Dust.getHandle(unit, TOKEN_MIND_ASP_ASPECT, rt, DustOptCreate.Meta);

						String ri = DustUtils.simpleGet(rdd, JsonApiMember.id);

						Object key = DustUtils.simpleGet(rdd, JsonApiMember.meta, EXT_JSONAPI_KEY);

						DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, DustOptCreate.Reference);

						if (null == key) {
							Dust.access(DustAccess.Insert, rh, target, tAtt);
						} else {
							Dust.access(DustAccess.Set, rh, target, tAtt, (key instanceof Number) ? ((Number) key).intValue() : key);
						}
					}
				} else {
					String rt = DustUtils.simpleGet(rd, JsonApiMember.type);
					DustHandle tTypeRef = Dust.getHandle(unit, TOKEN_MIND_ASP_ASPECT, rt, DustOptCreate.Meta);
					String ri = DustUtils.simpleGet(rd, JsonApiMember.id);

					DustHandle rh = Dust.getHandle(unit, tTypeRef, ri, DustOptCreate.Reference);

					Dust.access(DustAccess.Set, rh, target, tAtt);
				}
			}
		}
	}
}
