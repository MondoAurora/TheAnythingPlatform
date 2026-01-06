package me.giskard.dust.kbtools;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.kb.DustKBStore;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustKBToolsSynchroniserAgent extends DustAgent implements DustKBToolsConsts, DustNetConsts {

	DustKBStore kb;

	@Override
	protected void init() throws Exception {
		kb = Dust.getAgent(DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KB_KNOWLEDGEBASE));
	}

	@Override
	protected Object process(DustAction action) throws Exception {
		String cmd = DustKBUtils.access(DustAccess.Peek, "", null, TOKEN_CMD);
//		String cmd = ((Map<String, String>) params).getOrDefault(TOKEN_CMD, "");
		StringBuilder sb = null;

		switch (cmd) {
		case TOKEN_KBT_CMD_LOADALL:
			KBUnit uTarget = loadAll();
			sb = DustUtils.sbAppend(null, " ", false, uTarget.size());
			break;
		}

//		HttpServletResponse response = DustKBUtils.access(DustAccess.Peek, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);
		HttpServletResponse response = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

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
		String tName = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KBMETA_TYPE);
		String mName = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_META);
		KBUnit uMeta = kb.getUnit(mName, true);

		KBObject targetType = uMeta.getObject(kb.getMetaTypeId(TOKEN_KBMETA_TYPE), tName);
		String type = uMeta.getUnitId() + DUST_SEP_TOKEN + targetType.getId();

		String uName = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_UNIT);
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

		Collection<String> ids = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_INDEX);

		for (KBObject o : uTarget.objects()) {
			for (String i : ids) {
				Collection<String> ic = DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_SET, o, i);
				for (String v : ic) {
					if (!DustUtils.isEmpty(v)) {
						index.get(i).put(v, o);
					}
				}
			}
		}

		for (Object src : (Collection<Object>) DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_SOURCE)) {
			Map<String, Object> mapping = DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_MAPPING);
			if (mapping.isEmpty() || mapping.containsKey("")) {
				continue;
			}

			KBUnit m = kb.getUnit(DustKBUtils.access(DustAccess.Peek, null, src, TOKEN_META), true);
			String srcPrefix = m.getUnitId() + DUST_SEP_TOKEN;

			KBUnit u = kb.getUnit(DustKBUtils.access(DustAccess.Peek, null, src, TOKEN_UNIT), true);
			Dust.log(TOKEN_LEVEL_INFO, "Reading unit", u.getUnitId());

			String id = DustKBUtils.access(DustAccess.Peek, null, src, TOKEN_ID);
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

				DustKBUtils.access(DustAccess.Insert, DustUtils.sbAppend(null, "/", true, u.getUnitId(), o.getType(), o.getId()).toString(), target, TOKEN_SOURCE);
				DustKBUtils.access(DustAccess.Insert, u.getUnitId(), target, TOKEN_UNIT);

				for (Map.Entry<String, Object> me : mapping.entrySet()) {
					String fTarget = me.getKey();
					Object fSource = me.getValue();
					Object v;

					KBObject targetAtt = uMeta.getObject(kb.getMetaTypeId(TOKEN_KBMETA_ATTRIBUTE), fTarget, KBOptCreate.None);
					if (null == targetAtt) {
						targetAtt = uMeta.getObject(kb.getMetaTypeId(TOKEN_KBMETA_ATTRIBUTE), fTarget);
						DustKBUtils.access(DustAccess.Insert, targetAtt, targetType, TOKEN_CHILDMAP, fTarget);
						DustKBUtils.access(DustAccess.Set, targetType, targetAtt, TOKEN_PARENT);
					}
					DustKBUtils.access(DustAccess.Insert, srcPrefix + fSource, targetAtt, TOKEN_SOURCE);

					if (fSource instanceof String) {
						v = DustKBUtils.access(DustAccess.Peek, null, o, fSource);
						if (null != v) {
							DustKBUtils.access(DustAccess.Insert, v, target, fTarget);
						}
					} else {
						for (Object fld : (Collection) fSource) {
							v = DustKBUtils.access(DustAccess.Peek, null, o, fld);
							if (null != v) {
								DustKBUtils.access(DustAccess.Insert, v, target, fTarget);
							}
						}
					}
				}
			}
		}

		Object ser = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);
		if (null != ser) {
			Map<String, Object> params = new HashMap<>();
			params.put(TOKEN_CMD, TOKEN_CMD_SAVE);
			
			params.put(TOKEN_UNIT, uMeta);
			params.put(TOKEN_KEY, mName);
			DustKBUtils.access(DustAccess.Process, params, ser);
			
			params.put(TOKEN_UNIT, uTarget);
			params.put(TOKEN_KEY, uName);
			DustKBUtils.access(DustAccess.Process, params, ser);

//			DustKBUtils.access(DustAccess.Set, uMeta, ser, TOKEN_UNIT);
//			DustKBUtils.access(DustAccess.Set, mName, ser, TOKEN_KEY);
//			Dust.sendMessage(ser);
//
//			DustKBUtils.access(DustAccess.Set, uTarget, ser, TOKEN_UNIT);
//			DustKBUtils.access(DustAccess.Set, uName, ser, TOKEN_KEY);
//			Dust.sendMessage(ser);
		}

		return uTarget;
	}

	public String getFirstValue(KBObject o, Map<String, Object> mapping, String fldTarget) {
		String val = null;

		Object fldSource = mapping.get(fldTarget);

		if (null != fldSource) {

			if (fldSource instanceof String) {
				val = DustKBUtils.access(DustAccess.Peek, null, o, fldSource);
			} else {
				for (Object fld : (Collection) fldSource) {
					String s = DustKBUtils.access(DustAccess.Peek, null, o, fld);
					if (!DustUtils.isEmpty(s)) {
						val = s;
						break;
					}
				}
			}
		}

		if (null != val) {
			String preProcess = DustKBUtils.access(DustAccess.Peek, "", null, TOKEN_PREPROCESS, fldTarget);

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
