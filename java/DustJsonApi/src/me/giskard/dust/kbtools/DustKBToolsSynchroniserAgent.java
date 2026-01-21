package me.giskard.dust.kbtools;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.mind.DustMindUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustKBToolsSynchroniserAgent extends DustAgent implements DustKBToolsConsts, DustNetConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_CMD);
		StringBuilder sb = null;

		switch (cmd) {
		case TOKEN_KBT_CMD_LOADALL:
			DustObject uTarget = loadAll();
			int size = DustMindUtils.getUnitSize(uTarget);
			sb = DustUtils.sbAppend(null, " ", false, size);
			break;
		}

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

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

	public DustObject loadAll() {
		DustObject attType = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);

		String mName = Dust.access(DustAccess.Peek, null, null, TOKEN_META);
		DustObject uMeta = Dust.getUnit(mName, true);
		
		String tName = Dust.access(DustAccess.Peek, null, null, TOKEN_KBMETA_TYPE);
		DustObject type = Dust.getObject(uMeta, null, tName, DustOptCreate.Meta);
//		String type = uMeta.getId() + DUST_SEP_TOKEN + targetType.getId();

		String uName = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);
		DustObject uTarget = Dust.getUnit(uName, true);

		DustCreator<DustObject> coreCreator = new DustCreator<DustObject>() {
			@Override
			public DustObject create(Object key, Object... hints) {
				int size = DustMindUtils.getUnitSize(uTarget);
				return Dust.getObject(uTarget, type, DustUtils.toString(size), DustOptCreate.Primary);
			}
		};

		DustUtilsFactory<String, DustUtilsFactory<String, DustObject>> index = new DustUtilsFactory<String, DustUtilsFactory<String, DustObject>>(
				new DustCreator<DustUtilsFactory<String, DustObject>>() {
					@Override
					public DustUtilsFactory<String, DustObject> create(Object key, Object... hints) {
						return new DustUtilsFactory<String, DustObject>(coreCreator);
					}
				});

		Collection<String> ids = Dust.access(DustAccess.Peek, null, null, TOKEN_INDEX);

		for (DustObject o : DustMindUtils.getUnitMembers(uTarget)) {
			for (String i : ids) {
				Collection<String> ic = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, o, i);
				for (String v : ic) {
					if (!DustUtils.isEmpty(v)) {
						index.get(i).put(v, o);
					}
				}
			}
		}

		for (Object src : (Collection<Object>) Dust.access(DustAccess.Peek, null, null, TOKEN_SOURCE)) {
			Map<String, Object> mapping = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_MAPPING);
			if (mapping.isEmpty() || mapping.containsKey("")) {
				continue;
			}

			DustObject m = Dust.getUnit(Dust.access(DustAccess.Peek, null, src, TOKEN_META), true);
			String srcPrefix = m.getId() + DUST_SEP_TOKEN;

			DustObject u = Dust.getUnit(Dust.access(DustAccess.Peek, null, src, TOKEN_DATA), true);
			Dust.log(TOKEN_LEVEL_INFO, "Reading unit", u.getId());

			String id = Dust.access(DustAccess.Peek, null, src, TOKEN_ID);
			int lc = 0;

			for (DustObject o : DustMindUtils.getUnitMembers(u)) {
				if (0 == (++lc % 10000)) {
					Dust.log(TOKEN_LEVEL_TRACE, "item", lc);
				}

				DustObject target = null;
				String idVal;

				for (String i : ids) {
					idVal = getFirstValue(o, mapping, srcPrefix, i);
					if (!DustUtils.isEmpty(idVal)) {
						target = index.get(i).peek(idVal);
						if (null != target) {
							break;
						}
					}
				}

				if (null == target) {
					idVal = getFirstValue(o, mapping, srcPrefix, id);

					if (DustUtils.isEmpty(idVal)) {
						Dust.log(TOKEN_LEVEL_WARNING, "Missing ID", o);
						continue;
					}
					target = index.get(id).get(idVal);
				}

				for (String i : ids) {
					idVal = getFirstValue(o, mapping, srcPrefix, i);
					if (!DustUtils.isEmpty(idVal)) {
						index.get(i).put(idVal, target);
					}
				}

				Dust.access(DustAccess.Insert, DustUtils.sbAppend(null, "/", true, u.getId(), o.getType().getId(), o.getId()).toString(), target, TOKEN_SOURCE);
				Dust.access(DustAccess.Insert, u.getId(), target, TOKEN_DATA);

				for (Map.Entry<String, Object> me : mapping.entrySet()) {
					String fTarget = me.getKey();
					Object fSource = me.getValue();
					Object v;

					DustObject targetAtt = Dust.getObject(uMeta, attType, fTarget, DustOptCreate.None);
					if (null == targetAtt) {
						targetAtt = Dust.getObject(uMeta, attType, fTarget, DustOptCreate.Primary);
						Dust.access(DustAccess.Set, targetAtt, type, TOKEN_CHILDMAP, fTarget);
						Dust.access(DustAccess.Insert, type, targetAtt, TOKEN_APPEARS);
					}
					Dust.access(DustAccess.Insert, srcPrefix + fSource, targetAtt, TOKEN_SOURCE);

					if (fSource instanceof String) {
						v = Dust.access(DustAccess.Peek, null, o, srcPrefix + fSource);
						if (null != v) {
							Dust.access(DustAccess.Insert, v, target, targetAtt);
						}
					} else {
						for (Object fld : (Collection) fSource) {
							v = Dust.access(DustAccess.Peek, null, o, srcPrefix + fld);
							if (null != v) {
								Dust.access(DustAccess.Insert, v, target, targetAtt);
							}
						}
					}
				}
			}
		}

		Object ser = Dust.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);
		if (null != ser) {
			Map<String, Object> params = new HashMap<>();
			params.put(TOKEN_CMD, TOKEN_CMD_SAVE);

			params.put(TOKEN_DATA, uMeta);
			params.put(TOKEN_KEY, mName);
			Dust.access(DustAccess.Process, params, ser);

			params.put(TOKEN_DATA, uTarget);
			params.put(TOKEN_KEY, uName);
			Dust.access(DustAccess.Process, params, ser);
		}

		return uTarget;
	}

	public String getFirstValue(DustObject o, Map<String, Object> mapping, String srcPrefix, String fldTarget) {
		String val = null;

		Object fldSource = mapping.get(fldTarget);

		if (null != fldSource) {
			if (fldSource instanceof String) {
				val = Dust.access(DustAccess.Peek, null, o, srcPrefix + fldSource);
			} else {
				for (Object fld : (Collection) fldSource) {
					String s = Dust.access(DustAccess.Peek, null, o, srcPrefix + fld);
					if (!DustUtils.isEmpty(s)) {
						val = s;
						break;
					}
				}
			}
		}

		if (null != val) {
			String preProcess = Dust.access(DustAccess.Peek, "", null, TOKEN_PREPROCESS, fldTarget);

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
