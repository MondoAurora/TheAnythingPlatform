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
import me.giskard.dust.ldap.DustLDAPConsts;
import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtils;

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
				String metaId = Dust.access(DustAccess.Peek, null, src, TOKEN_META);
				DustHandle meta = Dust.getUnit(metaId, true);

				String type = Dust.access(DustAccess.Peek, null, src, TOKEN_TYPE);
				DustHandle tType = Dust.getHandle(meta, typeType, type, DustOptCreate.Meta);

				String fileName = Dust.access(DustAccess.Peek, null, src, TOKEN_PATH);
				File f = new File(fileName);

				String unitId = Dust.access(DustAccess.Peek, null, src, TOKEN_DATA);
				DustHandle unit = Dust.getUnit(unitId, true);

				String encoding = Dust.access(DustAccess.Peek, DUST_CHARSET_UTF8, src, TOKEN_STREAM_ENCODING);
				String colSep = Dust.access(DustAccess.Peek, ",", src, TOKEN_STREAM_COLSEP);

				String keyCol = Dust.access(DustAccess.Peek, null, src, TOKEN_ID);
//				Map<String, String> preProcess = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, src, TOKEN_PREPROCESS);
				
				try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
					String line;
					ArrayList<String> items = new ArrayList<>();
					DustStreamUtils.CsvLineReader lineReader = new DustStreamUtils.CsvLineReader(colSep, items);

					ArrayList<DustHandle> fields = new ArrayList<>();
					Set<String> keys = new HashSet<>();
					int lc = 0;
					String str;
					int keyIdx = 0;

					while ((line = br.readLine()) != null) {
						if (!lineReader.csvReadLine(line)) {
							continue;
						}

						if (0 == (++lc % 10000)) {
//							Dust.log(TOKEN_LEVEL_TRACE, "reading line", lc);
						}

						int colCount = items.size();

						if (colCount == fields.size()) {
							str = items.get(keyIdx).trim();

							if (!keys.add(str)) {
//								Dust.log(TOKEN_LEVEL_WARNING, "key collision", str);
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

								Dust.log("Reading unit", f.getName(), fields);
							}
						}

						items.clear();
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
