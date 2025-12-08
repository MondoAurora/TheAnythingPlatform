package me.giskard.dust.kbtools;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts;
import me.giskard.dust.DustException;
import me.giskard.dust.kb.DustKBStore;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustKBToolsSynchroniserAgent extends DustConsts.DustAgentBase implements DustKBToolsConsts, DustNetConsts {

	DustKBStore kb;

	@Override
	protected void init() throws Exception {
		kb = Dust.getAgent(DustUtils.simpleGet(cfg, "KnowledgeBase"));
	}

	@Override
	protected Object process(Map<String, Object> cfg, Object params) throws Exception {
		String cmd = ((Map<String, String>) params).getOrDefault(TOKEN_CMD, "");
		StringBuilder sb = null;

		switch (cmd) {
		case TOKEN_KBT_CMD_LOADALL:
			KBUnit uTarget = loadAll();
			sb = DustUtils.sbAppend(null, " ", false, uTarget.size());
			break;
		}

		HttpServletResponse response = DustKBUtils.access(KBAccess.Peek, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			response.setContentType(MEDIATYPE_UTF8_HTML);
			PrintWriter out = response.getWriter();

			if (null == sb) {
				out.println("done ?");
			} else {
				out.println(sb.toString());
			}
		}

		return null;

	}

	public KBUnit loadAll() {
		String uName = DustKBUtils.access(KBAccess.Peek, null, cfg, TOKEN_UNIT);
		String type = DustKBUtils.access(KBAccess.Peek, null, cfg, TOKEN_KBMETA_TYPE);
		KBUnit uTarget = kb.getUnit(uName, true);

		DustCreator<KBObject> coreCreator = new DustCreator<KBObject>() {
			@Override
			public KBObject create(Object key, Object... hints) {
				return uTarget.getObject(type, DustUtils.toString(uTarget.size()));
			}
		};

		DustUtilsFactory<String, DustUtilsFactory<String, KBObject>> index = new DustUtilsFactory<String, DustUtilsFactory<String, KBObject>>(
				new DustCreator<DustUtilsFactory<String, KBObject>>() {
					@Override
					public DustUtilsFactory<String, KBObject> create(Object key, Object... hints) {
						return new DustUtilsFactory<String, KBObject>(coreCreator);
					}
				});

		Collection<String> ids = (Collection<String>) cfg.get(TOKEN_INDEX);

		for (KBObject o : uTarget.objects()) {
			for (String i : ids) {
				Collection<String> ic = o.access(KBAccess.Peek, Collections.EMPTY_SET, i);
				for (String v : ic) {
					if (!DustUtils.isEmpty(v)) {
						index.get(i).put(v, o);
					}
				}
			}
		}

		for (Object src : (Collection<Object>) cfg.get(TOKEN_SOURCE)) {
			Map<String, Object> mapping = DustKBUtils.access(KBAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_MAPPING);
			if (mapping.isEmpty() || mapping.containsKey("")) {
				continue;
			}

			KBUnit u = kb.getUnit(DustKBUtils.access(KBAccess.Peek, null, src, TOKEN_UNIT), false);
			Dust.log(TOKEN_LEVEL_INFO, "Reading unit", u.getUnitId());

			String id = DustKBUtils.access(KBAccess.Peek, null, src, TOKEN_ID);
			int lc = 0;

			for (KBObject o : u.objects()) {
				if (0 == (++lc % 10000)) {
					Dust.log(TOKEN_LEVEL_TRACE, "item", lc);
				}

				KBObject target = null;
				String idVal;

				for (String i : ids) {
					idVal = getFirstValue(o, mapping, i);
					if (!DustUtils.isEmpty(idVal)) {
						target = index.get(i).peek(idVal);
						if (null != target) {
							break;
						}
					}
				}

				if (null == target) {
					idVal = getFirstValue(o, mapping, id);

					if (DustUtils.isEmpty(idVal)) {
						Dust.log(TOKEN_LEVEL_WARNING, "Missing ID", o);
						continue;
					}
					target = index.get(id).get(idVal);
				}

				for (String i : ids) {
					idVal = getFirstValue(o, mapping, i);
					if (!DustUtils.isEmpty(idVal)) {
						index.get(i).put(idVal, target);
					}
				}

				target.access(KBAccess.Insert, DustUtils.sbAppend(null, "/", true, u.getUnitId(), o.getType(), o.getId()).toString(), TOKEN_SOURCE);
				target.access(KBAccess.Insert, u.getUnitId(), TOKEN_UNIT);

				for (Map.Entry<String, Object> me : mapping.entrySet()) {
					String fTarget = me.getKey();
					Object fSource = me.getValue();
					Object v;

					if (fSource instanceof String) {
						v = o.access(KBAccess.Peek, null, fSource);
						if (null != v) {
							target.access(KBAccess.Insert, v, fTarget);
						}
					} else {
						for (Object fld : (Collection) fSource) {
							v = o.access(KBAccess.Peek, null, fld);
							if (null != v) {
								target.access(KBAccess.Insert, v, fTarget);
							}
						}
					}
				}
			}
		}

		Map<String, Object> ser = DustUtils.simpleGet(cfg, TOKEN_SERIALIZER);
		if (null != ser) {
			DustKBUtils.access(KBAccess.Set, uTarget, ser, TOKEN_PARAMS, TOKEN_UNIT);
			DustKBUtils.access(KBAccess.Set, uName, ser, TOKEN_PARAMS, TOKEN_KEY);
			Dust.sendMessage(ser);
		}

		return uTarget;
	}

	public String getFirstValue(KBObject o, Map<String, Object> mapping, String fldTarget) {
		String val = null;

		Object fldSource = mapping.get(fldTarget);

		if (null != fldSource) {

			if (fldSource instanceof String) {
				val = o.access(KBAccess.Peek, null, fldSource);
			} else {
				for (Object fld : (Collection) fldSource) {
					String s = o.access(KBAccess.Peek, null, fld);
					if (!DustUtils.isEmpty(s)) {
						val = s;
						break;
					}
				}
			}
		}

		if (null != val) {
			String preProcess = DustKBUtils.access(KBAccess.Peek, "", cfg, TOKEN_PREPROCESS, fldTarget);

			switch (preProcess) {
			case TOKEN_UPPERCASE:
				val = val.toUpperCase();
				break;
			case "":
				// do nothing
				break;
			default:
				DustException.wrap(null, "Unknown preprocess is set for", fldTarget);
				break;
			}
		}

		return val;
	}
}
