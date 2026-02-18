package me.giskard.dust.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.DustException;
import me.giskard.dust.ldap.DustLDAPConsts;
import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFile;

//@SuppressWarnings({ "unchecked", "rawtypes" })
@SuppressWarnings({ "unchecked" })
public class DustStreamCsvAgent extends DustAgent implements DustStreamConsts, DustMindConsts, DustLDAPConsts {

	DustHandle typeAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
	DustHandle typeType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);

	@Override
	protected Object process(DustAccess access) throws Exception {

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {
				String fileName = Dust.access(DustAccess.Peek, null, src, TOKEN_PATH);
				File f = new File(fileName);
				
				String grp = f.getName();

				String metaId = Dust.access(DustAccess.Peek, grp + "Meta.1", src, TOKEN_META);
				DustHandle meta = Dust.getUnit(metaId, true);

				if (f.isFile()) {
					loadFile(f, src, meta);
				} else if (f.isDirectory()) {
					String postfix = Dust.access(DustAccess.Peek, null, src, TOKEN_POSTFIX);
					String prefix = f.getName();

					DustUtilsFile.procRecursive(f, new DustUtilsFile.FileProcessor() {

						@Override
						public boolean processFile(File f) {
							try {
								String type = f.getName();
								if (type.endsWith(postfix)) {
									type = type.substring(0, type.length() - postfix.length());
								}
								type = prefix + "_" + type;

								Dust.access(DustAccess.Set, type, src, TOKEN_TYPE);
								Dust.access(DustAccess.Set, type + ".1", src, TOKEN_DATA);
								loadFile(f, src, meta);
							} catch (Throwable e) {
								DustException.swallow(e, "Processing csv file", f.getAbsolutePath());
							}
							return true;
						}
					}, new DustUtilsFile.ExtFilter(DUST_EXT_CSV));
				}
			}

			break;
		case TOKEN_CMD_SAVE:

			break;

		}
		return null;
	}

	public void loadFile(File f, Map<String, Object> src, DustHandle meta) throws Exception {
		DustHandle tType = null;

		String unitId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
		DustHandle unit = Dust.getUnit(unitId, true);

		String encoding = Dust.access(DustAccess.Peek, DUST_CHARSET_UTF8, src, TOKEN_STREAM_ENCODING);
		String colSep = Dust.access(DustAccess.Peek, ",", src, TOKEN_STREAM_COLSEP);

		String keyCol = Dust.access(DustAccess.Peek, null, src, TOKEN_ID);
		Collection<String> altKeyCols = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, src, TOKEN_ALIAS);
//				Map<String, String> preProcess = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_PREPROCESS);

		try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			String line;
			ArrayList<String> items = new ArrayList<>();
			DustStreamUtils.CsvLineReader lineReader = new DustStreamUtils.CsvLineReader(colSep, items);

			ArrayList<DustHandle> fields = new ArrayList<>();
			Set<String> keys = new HashSet<>();
			int lc = 0;
			String str;
			int keyIdx = -1;

			while ((line = br.readLine()) != null) {
				if (!lineReader.csvReadLine(line)) {
					continue;
				}

				if (0 == (++lc % 10000)) {
//							Dust.log(TOKEN_LEVEL_TRACE, "reading line", lc);
				}

				int colCount = items.size();

				if (colCount == fields.size()) {
					if (-1 == keyIdx) {
						StringBuilder sb = null;
						for ( String fval : items ) {
							sb = DustUtils.sbAppend(sb, "_", true, fval);
						}
						str = sb.toString();
					} else {
						str = items.get(keyIdx).trim();
					}
					
					if (!keys.add(str)) {
//								Dust.log(TOKEN_LEVEL_WARNING, "key collision", str);
					}

					if (null == tType) {
						String type = Dust.access(DustAccess.Peek, null, src, TOKEN_TYPE);
						tType = Dust.getHandle(meta, typeType, type, DustOptCreate.Meta);
					}

					DustHandle hTarget = Dust.getHandle(unit, tType, str, DustOptCreate.Primary);

					for (int i = 0; i < colCount; ++i) {
						str = items.get(i).trim();
						if (!DustUtils.isEmpty(str)) {
							Dust.access(DustAccess.Set, str, hTarget, fields.get(i));
						}
					}
				} else {
					if (fields.isEmpty()) {
						for (String h : items) {
							String colName = h.trim();

							if (keyCol.equals(colName)) {
								keyIdx = fields.size();
							}

							DustHandle att = getAtt(meta, tType, colName);

							fields.add(att);
						}
						
						if ( -1 == keyIdx ) {
							for ( int i = 0; i < items.size(); ++i ) {
								String colName = items.get(i).trim();
								if ( altKeyCols.contains(colName) ) {
									keyIdx = i;
									break;
								}
							}
						}

						Dust.log("Reading unit", f.getName(), fields);
					}
				}

				items.clear();
			}
		}
	}

	public DustHandle getAtt(DustHandle meta, DustHandle tType, String attName) {
		DustHandle att = Dust.getHandle(meta, DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE), meta.getId() + DUST_SEP_TOKEN + attName, DustOptCreate.Meta);
		Dust.access(DustAccess.Insert, tType, att, TOKEN_APPEARS);
		Dust.access(DustAccess.Set, att, tType, TOKEN_CHILDMAP, attName);
		return att;
	}

}
