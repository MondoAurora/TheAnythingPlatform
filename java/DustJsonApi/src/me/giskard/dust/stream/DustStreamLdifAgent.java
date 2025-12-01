package me.giskard.dust.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts;
import me.giskard.dust.DustException;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.ldap.DustLDAPConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFile;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustStreamLdifAgent extends DustConsts.DustAgentBase implements DustStreamConsts, DustKBConsts, DustLDAPConsts {

	FileFilter ffLdif = new DustUtilsFile.ExtFilter(DUST_EXT_LDIF);

	@Override
	protected Object process(Map<String, Object> cfg, Object params) throws Exception {

		KBStore kb = Dust.getAgent(DustUtils.simpleGet(cfg, TOKEN_KB_KNOWLEDGEBASE));

		String cmd = DustUtils.simpleGet(params, TOKEN_CMD);
		Map<String, Object> ser = DustUtils.simpleGet(params, TOKEN_SERIALIZER);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			String unitId = DustUtils.simpleGet(params, TOKEN_UNIT);
			KBUnit unitMeta = kb.getUnit(unitId, true);

			String fn = DustKBUtils.access(KBAccess.Peek, null, cfg, TOKEN_META);

			if (!DustUtils.isEmpty(fn)) {
				File f = new File(fn);

				DustUtilsFile.FileProcessor fp = new DustUtilsFile.FileProcessor() {
					@Override
					public boolean processFile(File f) {
						try {
							readSchemaLdif(unitMeta, f);
						} catch (Throwable e) {
							DustException.swallow(e, "Processing schema LDIF", fn);
						}
						return true;
					}
				};

				DustUtilsFile.procRecursive(f, fp, ffLdif);
				
				if (null != ser) {
					DustKBUtils.access(KBAccess.Set, unitMeta, ser, TOKEN_PARAMS, TOKEN_UNIT);
					DustKBUtils.access(KBAccess.Set, unitId, ser, TOKEN_PARAMS, TOKEN_KEY);
					Dust.sendMessage(ser);
				}
			}

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) DustKBUtils.access(KBAccess.Visit, Collections.EMPTY_LIST, params, TOKEN_SOURCE))) {
				Map<String, Object> p = new TreeMap<>(cfg);
				p.putAll((Map) params);
				p.putAll(src);

				String fileName = DustKBUtils.access(KBAccess.Peek, null, p, TOKEN_PATH);
				File f = new File(fileName);

				unitId = DustUtils.simpleGet(p, TOKEN_UNIT);
				KBUnit unit = kb.getUnit(unitId, true);

				readDataLdif(unit, unitMeta, p, f);

				if (null != ser) {
					DustKBUtils.access(KBAccess.Set, unit, ser, TOKEN_PARAMS, TOKEN_UNIT);
					DustKBUtils.access(KBAccess.Set, unitId, ser, TOKEN_PARAMS, TOKEN_KEY);
					Dust.sendMessage(ser);
				}
			}

			break;
		case TOKEN_CMD_SAVE:

			break;

		}
		return null;
	}

	protected void readSchemaLdif(KBUnit unit, File f) throws Exception {
		Pattern ptName = Pattern.compile(".*NAME\\s+'([^']+)'.*");
		Pattern ptDesc = Pattern.compile(".*DESC\\s+'([^']*)'.*");
		Pattern ptMust = Pattern.compile(".*MUST\\s+\\(([^)]*)\\).*");
		Pattern ptMay = Pattern.compile(".*MAY\\s+\\(([^)]*)\\).*");

		KBStore kb = unit.getStore();
		String typeAtt = kb.getMetaTypeId(TOKEN_KBMETA_ATTRIBUTE);
		String typeType = kb.getMetaTypeId(TOKEN_KBMETA_TYPE);

		try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String line;

			KBObject o = null;

			String type = null;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				String val = line;
				String str;

				int sep = line.indexOf(":");
				if (0 <= sep) {
					String key = line.substring(0, sep).trim().toLowerCase();
					val = line.substring(sep + 1).trim();

					switch (key) {
					case TOKEN_LDAP_ATTRIBUTE_TYPES:
						type = typeAtt;
						break;
					case TOKEN_LDAP_OBJECT_CLASSES:
						type = typeType;
						break;
					default:
						continue;
					}
				}

				Matcher m = ptName.matcher(val);

				if (m.matches()) {
					str = m.group(1);
					o = unit.getObject(type, str);
					DustKBUtils.access(KBAccess.Set, str, o, TOKEN_KEY);
					DustKBUtils.access(KBAccess.Set, DustUtils.cutPostfix(f.getName(), "."), o, TOKEN_PARENT);
				}

				m = ptDesc.matcher(val);
				if (m.matches()) {
					DustKBUtils.access(KBAccess.Set, m.group(1), o, TOKEN_DESC);
				}

				m = ptMust.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						KBObject ao = unit.getObject(typeAtt, a.trim());
						DustKBUtils.access(KBAccess.Insert, ao, o, TOKEN_LDAP_MUST, KEY_ADD);
						DustKBUtils.access(KBAccess.Insert, o, ao, TOKEN_LDAP_APPEARS, KEY_ADD);
					}
				}

				m = ptMay.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						KBObject ao = unit.getObject(typeAtt, a.trim());
						DustKBUtils.access(KBAccess.Insert, ao, o, TOKEN_LDAP_MAY, KEY_ADD);
						DustKBUtils.access(KBAccess.Insert, o, ao, TOKEN_LDAP_APPEARS, KEY_ADD);
					}
				}

				if (val.contains(TOKEN_LDAP_SINGLE_VALUE)) {
					DustKBUtils.access(KBAccess.Set, true, o, TOKEN_LDAP_SINGLE_VALUE);
					DustKBUtils.access(KBAccess.Set, true, o, TOKEN_LDAP_SINGLE_VALUE);
				}
			}
		}
	}

	public void readDataLdif(KBUnit unit, KBUnit unitMeta, Object params, File f) throws Exception {

		String type = DustKBUtils.access(KBAccess.Peek, "???", params, TOKEN_KBMETA_TYPE);
//		String at = unit.getStore().getMetaTypeId(TOKEN_KBMETA_ATTRIBUTE);
		String encoding = DustKBUtils.access(KBAccess.Peek, DUST_CHARSET_UTF8, params, TOKEN_STREAM_ENCODING);

		int lc = 0;

		try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			String line;

			KBObject o = null;
			
			String key = null;
			boolean base64 = false;
//			boolean multi = false;
			StringBuilder sb = new StringBuilder();
			String val;
			
//			Set<String> keys = new TreeSet<String>();

			while ((line = br.readLine()) != null) {
				if ( 0 == (++lc % 10000) ) {
					Dust.log(TOKEN_LEVEL_TRACE, "line", lc);
				}

				if (line.trim().isEmpty() || line.startsWith("#")) {
					continue;
				}

				if (line.startsWith(" ")) {
					sb.append(line.substring(1));
					continue;
				}

				int sep = line.indexOf(":");

				if (-1 == sep) {
					continue;
				}
				
				o = optAddVal(unit, type, o, key, sb, base64);

				key = line.substring(0, sep).trim();
				val = line.substring(sep + 1).trim();
				
				if ( base64 = val.startsWith(":") ) {
					val = val.substring(1).trim();
				}
				
				sb.append(val);
			}
			
			optAddVal(unit, type, o, key, sb, base64);
		}
	}

	public KBObject optAddVal(KBUnit unit, String type, KBObject o, String key, StringBuilder sb, boolean base64) {
		if ( !DustUtils.isEmpty(key) ) {
			String vv = sb.toString();
			
			if ( base64 ) {
				vv = new String( Base64.getDecoder().decode(vv));
			}
			sb.setLength(0);
			
			if (TOKEN_LDAP_DN.equals(key)) {
				o = unit.getObject(type, vv);
			}
			
			Object v = DustKBUtils.access(KBAccess.Peek, null, o, key);
			
			if ( null == v ) {
				DustKBUtils.access(KBAccess.Set, vv, o, key);
			} else {
				if ( ! (v instanceof Collection) ) {
					DustKBUtils.access(KBAccess.Delete, null, o, key);
					DustKBUtils.access(KBAccess.Insert, v, o, key, KEY_ADD);
				}
				DustKBUtils.access(KBAccess.Insert, vv, o, key, KEY_ADD);
			}
		}
		return o;
	}

}
