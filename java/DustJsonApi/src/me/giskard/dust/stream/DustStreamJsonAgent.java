package me.giskard.dust.stream;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsJson;

//@SuppressWarnings({ "unchecked", "rawtypes" })
@SuppressWarnings({ "unchecked" })
public class DustStreamJsonAgent extends DustAgent implements DustStreamConsts, DustMindConsts {

	DustHandle typeAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
	DustHandle typeType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);

	@Override
	protected Object process(DustAccess access) throws Exception {

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {
				String metaId = Dust.access(DustAccess.Peek, null, src, TOKEN_META);
				DustHandle meta = Dust.getUnit(metaId, true);

				String type = Dust.access(DustAccess.Peek, null, src, TOKEN_TYPE);
				DustHandle tType = Dust.getHandle(meta, typeType, type, DustOptCreate.Meta);

				String fileName = Dust.access(DustAccess.Peek, null, src, TOKEN_PATH);
				File f = new File(fileName);

				String unitId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
				DustHandle unit = Dust.getUnit(unitId, true);

				String encoding = Dust.access(DustAccess.Peek, DUST_CHARSET_UTF8, src, TOKEN_STREAM_ENCODING);
				Map<String, Object> content = DustUtilsJson.readJson(f, encoding);

				String collId = Dust.access(DustAccess.Peek, null, src, TOKEN_SOURCE);
				Collection<Map<String, Object>> arr = DustUtils.simpleGet(content, collId);

				String idKey = Dust.access(DustAccess.Peek, null, src, TOKEN_ID);
				Map<String, String> preProcess = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_PREPROCESS);

				for (Map<String, Object> o : arr) {
					Object id = o.get(idKey);

					DustHandle t = Dust.getHandle(unit, tType, DustUtils.toString(id), DustOptCreate.Meta);

					for (Map.Entry<String, Object> oe : o.entrySet()) {
						String attName = oe.getKey();
						Object val = oe.getValue();

						String pp = preProcess.getOrDefault(attName, "");
						switch (pp) {
						case TOKEN_INNERJSON:
							Map<String, Object> v = DustUtilsJson.parseJson((String) val);
							for (Map.Entry<String, Object> ve : v.entrySet()) {
								DustHandle att = getAtt(meta, tType, attName + "::" + ve.getKey());
								Dust.access(DustAccess.Set, ve.getValue(), t, att);
							}
							break;
						default:
							DustHandle att = getAtt(meta, tType, attName);
							Dust.access(DustAccess.Set, val, t, att);
							break;
						}
					}
				}
			}

			break;
		case TOKEN_CMD_SAVE:

			break;

		}
		return null;
	}

	public DustHandle getAtt(DustHandle meta, DustHandle tType, String attName) {
		DustHandle att = Dust.getHandle(meta, DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE), attName, DustOptCreate.Meta);
		Dust.access(DustAccess.Insert, tType, att, TOKEN_APPEARS);
		Dust.access(DustAccess.Set, att, tType, TOKEN_CHILDMAP, attName);
		return att;
	}

}
