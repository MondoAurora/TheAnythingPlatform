package me.giskard.dust.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.ldap.DustLDAPConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFile;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustStreamLdifAgent extends DustAgent implements DustStreamConsts, DustKBConsts, DustLDAPConsts {

	FileFilter ffLdif = new DustUtilsFile.ExtFilter(DUST_EXT_LDIF);

	@Override
	protected Object process(DustAction action) throws Exception {
		KBStore kb = Dust.getAgent(DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KB_KNOWLEDGEBASE));

		String cmd = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_CMD);
		Object ser = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			String unitId = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_UNIT);
			KBUnit unitMeta = kb.getUnit(unitId, true);

			String fn = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_META);

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
					
					Map<String, Object> params = new HashMap<>();
					params.put(TOKEN_CMD, TOKEN_CMD_SAVE);
					params.put(TOKEN_UNIT, unitMeta);
					params.put(TOKEN_KEY, unitId);
					
					DustKBUtils.access(DustAccess.Process, params, ser);

//					DustKBUtils.access(DustAccess.Set, unitMeta, ser, TOKEN_UNIT);
//					DustKBUtils.access(DustAccess.Set, unitId, ser, TOKEN_KEY);
//					Dust.sendMessage(ser);
				}
			}

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) DustKBUtils.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {

				String fileName = DustKBUtils.access(DustAccess.Peek, null, src, TOKEN_PATH);
				File f = new File(fileName);

				unitId = DustKBUtils.access(DustAccess.Peek, null, src, TOKEN_UNIT);
				KBUnit unit = kb.getUnit(unitId, true);

				readDataLdif(unit, unitMeta, src, f);

				if (null != ser) {
					
					Map<String, Object> params = new HashMap<>();
					params.put(TOKEN_CMD, TOKEN_CMD_SAVE);
					params.put(TOKEN_UNIT, unit);
					params.put(TOKEN_KEY, unitId);
					
					DustKBUtils.access(DustAccess.Process, params, ser);

//					DustKBUtils.access(DustAccess.Set, unit, ser, TOKEN_UNIT);
//					DustKBUtils.access(DustAccess.Set, unitId, ser, TOKEN_KEY);
//					Dust.sendMessage(ser);
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
					DustKBUtils.access(DustAccess.Set, str, o, TOKEN_KEY);
					DustKBUtils.access(DustAccess.Set, DustUtils.cutPostfix(f.getName(), "."), o, TOKEN_PARENT);
				}

				m = ptDesc.matcher(val);
				if (m.matches()) {
					DustKBUtils.access(DustAccess.Set, m.group(1), o, TOKEN_DESC);
				}

				m = ptMust.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						KBObject ao = unit.getObject(typeAtt, a.trim());
						DustKBUtils.access(DustAccess.Insert, ao, o, TOKEN_LDAP_MUST);
						DustKBUtils.access(DustAccess.Insert, o, ao, TOKEN_LDAP_APPEARS);
					}
				}

				m = ptMay.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						KBObject ao = unit.getObject(typeAtt, a.trim());
						DustKBUtils.access(DustAccess.Insert, ao, o, TOKEN_LDAP_MAY);
						DustKBUtils.access(DustAccess.Insert, o, ao, TOKEN_LDAP_APPEARS);
					}
				}

				if (val.contains(TOKEN_LDAP_SINGLE_VALUE)) {
					DustKBUtils.access(DustAccess.Set, true, o, TOKEN_LDAP_SINGLE_VALUE);
					DustKBUtils.access(DustAccess.Set, true, o, TOKEN_LDAP_SINGLE_VALUE);
				}
			}
		}
	}

	class LdifBlock {
		KBUnit unit;
		KBUnit unitMeta;

		String type;
		String at;
		String tt;
		String encoding;

		KBObject o = null;

		String key = null;
		boolean base64 = false;
		StringBuilder sb = new StringBuilder();

		public LdifBlock(KBUnit unit, KBUnit unitMeta, Object params) {
			this.unit = unit;
			this.unitMeta = unitMeta;

			type = DustKBUtils.access(DustAccess.Peek, "???", params, TOKEN_KBMETA_TYPE);
			at = unit.getStore().getMetaTypeId(TOKEN_KBMETA_ATTRIBUTE);
			tt = unit.getStore().getMetaTypeId(TOKEN_KBMETA_TYPE);
			encoding = DustKBUtils.access(DustAccess.Peek, DUST_CHARSET_UTF8, params, TOKEN_STREAM_ENCODING);
		}

		public void processLine(String line) {
			if (line.trim().isEmpty() || line.startsWith("#")) {
			} else if (line.startsWith(" ")) {
				sb.append(line.substring(1));
			} else {
				int sep = line.indexOf(":");

				if (-1 != sep) {
					optAddVal();

					key = line.substring(0, sep).trim();
					String val = line.substring(sep + 1).trim();

					base64 = val.startsWith(":");
					if (base64) {
						val = val.substring(1).trim();
					}

					sb.append(val);
				}
			}
		}

		public void optAddVal() {
			if (!DustUtils.isEmpty(key)) {
				String vv = sb.toString().trim();

				if (base64) {
					vv = new String(Base64.getDecoder().decode(vv));
				}
				sb.setLength(0);

				if (TOKEN_LDAP_DN.equals(key)) {
					o = unit.getObject(type, vv);
				}

				unitMeta.getObject(at, key);
				if (TOKEN_LDAP_OBJECTCLASS.equals(key)) {
					unitMeta.getObject(tt, vv);
				}

//				String k = unitMeta.getUnitId() + DUST_SEP_TOKEN + key;
				String k = key;
				Object v = DustKBUtils.access(DustAccess.Peek, null, o, k);

				boolean coll = (v instanceof Collection);

				if (coll) {
					if (((Collection) v).contains(vv)) {
						return;
					}
				} else {
					if (DustUtils.isEqual(vv, v)) {
						return;
					}
				}

				if (null == v) {
					DustKBUtils.access(DustAccess.Set, vv, o, k);
				} else {
					if (!coll) {
						DustKBUtils.access(DustAccess.Delete, null, o, k);
						DustKBUtils.access(DustAccess.Insert, v, o, k, KEY_ADD);
					}
					DustKBUtils.access(DustAccess.Insert, vv, o, k, KEY_ADD);
				}
			}
		}

	}

	public void readDataLdif(KBUnit unit, KBUnit unitMeta, Object params, File f) throws Exception {
		LdifBlock block = new LdifBlock(unit, unitMeta, params);

		int lc = 0;

		try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis, block.encoding))) {
			String line;

			while ((line = br.readLine()) != null) {
				if (0 == (++lc % 10000)) {
					Dust.log(TOKEN_LEVEL_TRACE, "line", lc);
				}

				block.processLine(line);
			}

			block.optAddVal();
		}
	}

}
