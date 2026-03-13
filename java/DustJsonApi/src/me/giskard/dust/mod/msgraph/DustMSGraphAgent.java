package me.giskard.dust.mod.msgraph;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.mod.utils.DustUtilsJson;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMSGraphAgent extends DustAgent implements DustMSGraphConsts, DustStreamConsts {
	DustHandle typeAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
	DustHandle typeType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);

	@Override
	protected Object process(DustAccess access) throws Exception {

		String authority = Dust.access(DustAccess.Peek, null, null, TOKEN_MSGRAPH_AUTHORITY);
		String clientId = Dust.access(DustAccess.Peek, null, null, TOKEN_MSGRAPH_CLIENTID);
		String secret = Dust.access(DustAccess.Peek, null, null, TOKEN_MSGRAPH_SECRET);
		String scope = Dust.access(DustAccess.Peek, null, null, TOKEN_MSGRAPH_SCOPE);

		String defMetaId = Dust.access(DustAccess.Peek, null, null, TOKEN_META);
		String defKey = Dust.access(DustAccess.Peek, null, null, TOKEN_KEY);

		String accessToken = DustMSGraphUtils.getAccessToken(authority, clientId, secret, scope);

		String cmd = Dust.access(DustAccess.Peek, TOKEN_CMD_INFO, null, TOKEN_CMD);

		for (Map<String, Object> src : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {

			String request = Dust.access(DustAccess.Peek, null, src, TOKEN_MSGRAPH_REQUEST);

			String key = Dust.access(DustAccess.Peek, defKey, src, TOKEN_KEY);

			String metaId = Dust.access(DustAccess.Peek, defMetaId, src, TOKEN_META);
			DustHandle meta = Dust.getUnit(metaId, true);

			String dataId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
			DustHandle data = Dust.getUnit(dataId, true);

			String typeId = Dust.access(DustAccess.Peek, null, src, TOKEN_TYPE);
			DustHandle type = Dust.getHandle(meta, typeType, typeId, DustOptCreate.Meta);

			Object response;

			InputStream is = null;
			try {
				is = DustMSGraphUtils.callGraphAPI(accessToken, "GET", request, null);

				response = DustUtilsJson.readJson(is, DUST_CHARSET_UTF8);

//				Dust.log(TOKEN_LEVEL_INFO, response);

			} finally {
				if (null != is) {
					is.close();
				}
			}

			switch (cmd) {
			case TOKEN_CMD_LOAD:
				for (Map<String, Object> ob : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, response, MSGRAPH_value))) {
					Object id = ob.get(key);
					DustHandle hTarget = Dust.getHandle(data, type, (String) id, DustOptCreate.Primary);

					for (Map.Entry<String, Object> oe : ob.entrySet()) {
						Object val = oe.getValue();

						if (null != val) {
							DustHandle att = DustUtilsData.getAtt(meta, type, oe.getKey());
							Dust.access(DustAccess.Set, val, hTarget, att);
						}
					}
				}
				break;
			case TOKEN_CMD_SAVE:
				String localDataId = Dust.access(DustAccess.Peek, null, src, TOKEN_CMD_SAVE);
				if (!DustUtils.isEmpty(localDataId)) {
					DustHandle localUnit = Dust.getUnit(localDataId, true);
					String mail = Dust.access(DustAccess.Peek, null, src, TOKEN_MAIL);
					String principalPostfix = Dust.access(DustAccess.Peek, "", src, TOKEN_POSTFIX);
					String mailPostfix = Dust.access(DustAccess.Peek, "", src, TOKEN_FILTER);

					mailPostfix = mailPostfix.toLowerCase();

					Map<Object, Map<String, Object>> extMap = new HashMap<>();

					for (Map<String, Object> ob : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, response, MSGRAPH_value))) {
						Object id = ob.get(key);
						extMap.put(id, ob);
					}

					Map<String, DustHandle> createMap = new TreeMap<>();

					int lc = 0;
					for (DustHandle h : DustMindUtils.getUnitMembers(localUnit)) {
						Object m = Dust.access(DustAccess.Peek, null, h, mail);
						String goodMail = null;

						if (0 == (++lc % 1000)) {
							Dust.log(TOKEN_LEVEL_TRACE, "item", lc);
						}

						if (m instanceof String) {
							String ma = ((String) m).toLowerCase();
							if (ma.endsWith(mailPostfix)) {
								goodMail = ma.trim();
							}
						} else if (m instanceof Collection) {
							for (Object mo : (Collection<?>) m) {
								if (mo instanceof String) {
									String ma = ((String) mo).toLowerCase();
									if (ma.endsWith(mailPostfix)) {
										goodMail = ma.trim();
									}
								}
							}
						}

						if (!DustUtils.isEmpty(goodMail)) {
							String[] mparts = goodMail.split("@");
							String pkey = mparts[0] + principalPostfix;

							Object ext = extMap.get(pkey);

							if (null == ext) {
								createMap.put(pkey, h);
							} else {
								Dust.log(TOKEN_LEVEL_INFO, "Object found", DustUtilsJson.toJson(ext));
							}
						}
					}

					save(accessToken, request, src, principalPostfix, mailPostfix, createMap);
				}

				break;
			case TOKEN_CMD_INFO:

				break;
			}
		}
		return null;
	}

	public void save(String accessToken, String request, Map<String, Object> src, String principalPostfix, String mailPostfix, Map<String, DustHandle> createMap)
			throws Exception {
		int count = 0;
		int limit = 500;

		long ts = System.currentTimeMillis();

		long apiTime = 0;

		Dust.log(TOKEN_LEVEL_INFO, "Would create", createMap.size(), "items, limited to", limit);

		String metaId = Dust.access(DustAccess.Peek, null, src, TOKEN_META);
		Map<String, String> mapping = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_MAPPING);
		Map<String, Object> consts = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_CONSTS);

		Map<String, Object> saveData = new TreeMap<>();

		for (Map.Entry<String, DustHandle> ce : createMap.entrySet()) {

			saveData.clear();

			DustHandle h = ce.getValue();

			for (Map.Entry<String, String> m : mapping.entrySet()) {
				String key = metaId + DUST_SEP_TOKEN + m.getKey();
				Object v = Dust.access(DustAccess.Peek, null, h, key);

				if (v instanceof Collection) {
					Iterator it = ((Collection) v).iterator();
					v = it.hasNext() ? it.next() : null;
				}

				if (null != v) {
					saveData.put(m.getValue(), v);
				}
			}

			if (!saveData.isEmpty()) {
				String pn = ce.getKey();

				saveData.putAll(consts);

				saveData.put(MSGRAPH_userPrincipalName, pn);

				String mailId = pn.substring(0, pn.length() - principalPostfix.length());

				saveData.put(MSGRAPH_mailNickname, mailId);
				saveData.put(MSGRAPH_mail, mailId + mailPostfix);

				if (count < limit) {
					++count;

					Dust.log(TOKEN_LEVEL_INFO, "Would create", DustUtilsJson.toJson(saveData));
					long ts2 = System.currentTimeMillis();

					InputStream is = DustMSGraphUtils.callGraphAPI(accessToken, "POST", request, saveData);
					Object response = DustUtilsJson.readJson(is, DUST_CHARSET_UTF8);
					Dust.log(TOKEN_LEVEL_INFO, "Creation returned", response);

					apiTime += System.currentTimeMillis() - ts2;
				}
			}
		}

		Dust.log(TOKEN_LEVEL_INFO, "Saved", count, "in", System.currentTimeMillis() - ts, "msec, save API time", apiTime);
	}
}
