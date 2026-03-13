package me.giskard.dust.core.stream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;

//@SuppressWarnings({ "unchecked", "rawtypes" })
@SuppressWarnings({ "unchecked" })
public class DustStreamCsvAgent extends DustAgent implements DustStreamConsts, DustMindConsts {

	DustHandle typeAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
	DustHandle typeType = DustUtils.getMindMeta(TOKEN_KBMETA_TYPE);

	@Override
	protected Object process(DustAccess access) throws Exception {

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		switch (cmd) {
		case TOKEN_CMD_LOAD:

			for (Map<String, Object> src : ((Collection<Map<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, null, TOKEN_SOURCE))) {
				String fileName = Dust.access(DustAccess.Peek, null, src, TOKEN_PATH);
				String grp = DustUtils.getPostfix(fileName, "/");

				String metaId = Dust.access(DustAccess.Peek, grp + "Meta.1", src, TOKEN_META);
				DustHandle meta = Dust.getUnit(metaId, true);

				Object streamSource = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_SOURCE);
				Map<String, Object> sp = new HashMap<String, Object>();

				sp.put(TOKEN_CMD, TOKEN_CMD_INFO);
				sp.put(TOKEN_PATH, fileName);

				Dust.access(DustAccess.Process, sp, streamSource);

				Collection<String> fileNames = (Collection<String>) sp.get(TOKEN_MEMBERS);

				boolean dir = fileName.length() > 1;

				for (String fn : fileNames) {
					if (fn.endsWith(DUST_EXT_CSV)) {
						try (InputStream is = DustStreamUtils.getStream(TOKEN_CMD_LOAD, fn, streamSource)) {
							if (dir) {
								String postfix = Dust.access(DustAccess.Peek, null, src, TOKEN_POSTFIX);
								String prefix = DustUtils.getPostfix(fn, "/");

								String type = prefix;
								if (type.endsWith(postfix)) {
									type = type.substring(0, type.length() - postfix.length());
								}
								type = grp + "_" + type;

								Dust.access(DustAccess.Set, type, src, TOKEN_TYPE);
								Dust.access(DustAccess.Set, type + ".1", src, TOKEN_DATA);
							}

							loadFile(is, src, meta);
						}
					}
				}

//				File f = new File(fileName);
//				
//
//				if (f.isFile()) {
//					loadFile(f, src, meta);
//				} else if (f.isDirectory()) {
//					String postfix = Dust.access(DustAccess.Peek, null, src, TOKEN_POSTFIX);
//					String prefix = f.getName();
//
//					DustUtilsFile.procRecursive(f, new DustUtilsFile.FileProcessor() {
//
//						@Override
//						public boolean processFile(File f) {
//							try {
//								String type = f.getName();
//								if (type.endsWith(postfix)) {
//									type = type.substring(0, type.length() - postfix.length());
//								}
//								type = prefix + "_" + type;
//
//								Dust.access(DustAccess.Set, type, src, TOKEN_TYPE);
//								Dust.access(DustAccess.Set, type + ".1", src, TOKEN_DATA);
//								loadFile(f, src, meta);
//							} catch (Throwable e) {
//								DustException.swallow(e, "Processing csv file", f.getAbsolutePath());
//							}
//							return true;
//						}
//					}, new DustUtilsFile.ExtFilter(DUST_EXT_CSV));
//				}
			}

			break;
		case TOKEN_CMD_SAVE:

			break;

		}
		return null;
	}

	public void loadFile(InputStream is, Map<String, Object> src, DustHandle meta) throws Exception {
		DustHandle tType = null;

		String unitId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
		DustHandle unit = Dust.getUnit(unitId, true);

		String encoding = Dust.access(DustAccess.Peek, DUST_CHARSET_UTF8, src, TOKEN_STREAM_ENCODING);
		String colSep = Dust.access(DustAccess.Peek, ",", src, TOKEN_STREAM_COLSEP);

		String keyCol = Dust.access(DustAccess.Peek, null, src, TOKEN_ID);
		Collection<String> altKeyCols = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, src, TOKEN_ALIAS);
//				Map<String, String> preProcess = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_PREPROCESS);

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding))) {
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
						for (String fval : items) {
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

							DustHandle att = DustUtilsData.getAtt(meta, tType, colName);

							fields.add(att);
						}

						if (-1 == keyIdx) {
							for (int i = 0; i < items.size(); ++i) {
								String colName = items.get(i).trim();
								if (altKeyCols.contains(colName)) {
									keyIdx = i;
									break;
								}
							}
						}

						Dust.log("Reading unit", unitId, fields);
					}
				}

				items.clear();
			}
		}
	}
}
