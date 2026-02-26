package me.giskard.dust.mod.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.mod.ldap.DustLdapNewConsts;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustStreamLdifAgent extends DustAgent implements DustStreamConsts, DustMindConsts, DustLdapNewConsts {

	FileFilter ffLdif = new DustUtilsFile.ExtFilter(DUST_EXT_LDIF);

	DustHandle typeAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
	DustHandle typeType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);

	@Override
	protected Object process(DustAccess access) throws Exception {

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			String unitId = Dust.access(DustAccess.Peek, null, null, TOKEN_META, TOKEN_KEY);
			DustHandle unitMeta = Dust.getUnit(unitId, true);

			String fn = Dust.access(DustAccess.Peek, null, null, TOKEN_META, TOKEN_PATH);

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
			}

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {

				String fileName = Dust.access(DustAccess.Peek, null, src, TOKEN_PATH);
				File f = new File(fileName);

				unitId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
				DustHandle unit = Dust.getUnit(unitId, true);

				readDataLdif(unit, unitMeta, src, f);
			}

			break;
		case TOKEN_CMD_SAVE:

			break;

		}
		return null;
	}

	protected void readSchemaLdif(DustHandle unit, File f) throws Exception {
		Pattern ptName = Pattern.compile(".*NAME\\s+'([^']+)'.*");
		Pattern ptDesc = Pattern.compile(".*DESC\\s+'([^']*)'.*");
		Pattern ptMust = Pattern.compile(".*MUST\\s+\\(([^)]*)\\).*");
		Pattern ptMay = Pattern.compile(".*MAY\\s+\\(([^)]*)\\).*");

		try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			String line;

			DustHandle h = null;

			DustHandle type = null;

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
					case LDAP_ATTRIBUTE_TYPES:
						type = typeAtt;
						break;
					case LDAP_OBJECT_CLASSES:
						type = typeType;
						break;
					default:
						continue;
					}
				}

				Matcher m = ptName.matcher(val);

				if (m.matches()) {
					str = m.group(1);
					h = Dust.getHandle(unit, type, str, DustOptCreate.Meta);
					Dust.access(DustAccess.Set, str, h, TOKEN_KEY);
					Dust.access(DustAccess.Set, DustUtils.cutPostfix(f.getName(), "."), h, TOKEN_PARENT);
				}

				m = ptDesc.matcher(val);
				if (m.matches()) {
					Dust.access(DustAccess.Set, m.group(1), h, TOKEN_DESC);
				}

				m = ptMust.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						String attName = a.trim();
						DustHandle ah = Dust.getHandle(unit, typeAtt, attName, DustOptCreate.Primary);
						Dust.access(DustAccess.Insert, ah, h, TOKEN_MANDATORY);
						Dust.access(DustAccess.Insert, h, ah, TOKEN_APPEARS);
						Dust.access(DustAccess.Set, ah, h, TOKEN_CHILDMAP, attName);

					}
				}

				m = ptMay.matcher(val);
				if (m.matches()) {
					String[] members = m.group(1).split("\\$");

					for (String a : members) {
						String attName = a.trim();
						DustHandle ah = Dust.getHandle(unit, typeAtt, attName, DustOptCreate.Primary);
						Dust.access(DustAccess.Insert, ah, h, TOKEN_OPTIONAL);
						Dust.access(DustAccess.Insert, h, ah, TOKEN_APPEARS);
						Dust.access(DustAccess.Set, ah, h, TOKEN_CHILDMAP, attName);
					}
				}

				if (val.contains(LDAP_SINGLE_VALUE)) {
					Dust.access(DustAccess.Set, true, h, TOKEN_COLLTYPE_SINGLE);
				}
			}
		}
	}

	class LdifBlock {
		DustHandle unit;
		DustHandle unitMeta;

		DustHandle type;
		String encoding;

		DustHandle currHandle = null;

		String key = null;
		boolean base64 = false;
		StringBuilder sb = new StringBuilder();

		public LdifBlock(DustHandle unit, DustHandle unitMeta, Object params) {
			this.unit = unit;
			this.unitMeta = unitMeta;

			String typeName = Dust.access(DustAccess.Peek, "???", params, TOKEN_TYPE);
			type = Dust.getHandle(unitMeta, typeType, typeName, DustOptCreate.Meta);

			encoding = Dust.access(DustAccess.Peek, DUST_CHARSET_UTF8, params, TOKEN_STREAM_ENCODING);
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

				String optId = unitMeta.getId() + DUST_SEP_TOKEN + vv;

				if (LDAP_DN.equals(key)) {
					currHandle = Dust.getHandle(unit, type, optId, DustOptCreate.Primary);
				}

				Object k = Dust.getHandle(unitMeta, typeAtt, key, DustOptCreate.Meta);
				Dust.access(DustAccess.Insert, type, k, TOKEN_APPEARS);
				Dust.access(DustAccess.Set, k, type, TOKEN_CHILDMAP, key);

				if (LDAP_OBJECTCLASS.equals(key)) {
					Dust.getHandle(unitMeta, typeType, optId, DustOptCreate.Meta);
				}

//				String k = unitMeta.getUnitId() + DUST_SEP_TOKEN + key;
//				String k = key;
				Object v = Dust.access(DustAccess.Peek, null, currHandle, k);

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
					Dust.access(DustAccess.Set, vv, currHandle, k);
				} else {
					if (!coll) {
						Dust.access(DustAccess.Delete, null, currHandle, k);
						Dust.access(DustAccess.Insert, v, currHandle, k, KEY_ADD);
					}
					Dust.access(DustAccess.Insert, vv, currHandle, k, KEY_ADD);
				}
			}
		}

	}

	public void readDataLdif(DustHandle unit, DustHandle unitMeta, Object params, File f) throws Exception {
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
