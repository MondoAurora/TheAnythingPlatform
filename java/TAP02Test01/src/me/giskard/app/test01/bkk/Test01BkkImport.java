package me.giskard.app.test01.bkk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

public class Test01BkkImport extends DustAgent implements Test01BkkConsts {

	@Override
	public void process() throws Exception {

		String root = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_ROOT);

		File dir = new File(root);

		Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "In BKK Import ", dir.isDirectory(), dir.getAbsolutePath());
		String colSep = ",";

		String author = "BKK/";
		String uCatName = author + "catalog";
		DustHandle uCat = Dust.getHandle(uCatName, HANDLE_MIND_ASP_UNIT);

		DustHandle tType;

		Object[] skip = { "shapes", "stop_times" };
//		Object[] skip = { "shapes", "stop_times", "trips" };
		Object[] noId = { "calendar_dates", "translations" };

		DustHandle hMsg = Dust.access(DustAccess.Peek, null, null, HANDLE_DUST_ATT_NODE, HANDLE_STREAM_ATT_UNIT_HANDLER);

		for (File f : dir.listFiles((FilenameFilter) new DustUtilsFile.ExtFilter("txt"))) {
			Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "   Read", f.getName(), f.length());

			String tName = DustUtils.cutPostfix(f.getName(), ".");

			if (-1 != DustUtils.indexOf(tName, skip)) {
				continue;
			}

			String uName = author + tName;
			DustHandle unit = Dust.getHandle(uName, HANDLE_MIND_ASP_UNIT);

			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				ArrayList<String> items = new ArrayList<>();
				DustStreamUtils.CsvLineReader lineReader = new DustStreamUtils.CsvLineReader(colSep, items);

				ArrayList<DustHandle> fields = new ArrayList<>();
				Set<Integer> keys = new TreeSet<>();
				int lc = 0;
				String str;

				Set<String> ids = new TreeSet<>();

				tType = Dust.getHandle(uCatName + DUST_SEP_TOKEN + tName, HANDLE_MIND_ASP_ASPECT);

				while ((line = br.readLine()) != null) {
					if (!lineReader.csvReadLine(line)) {
						continue;
					}

					if (0 == (++lc % 10000)) {
						Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "reading line", lc, tName);
					}

					int colCount = items.size();

					if (colCount == fields.size()) {

						if (keys.isEmpty()) {
							str = DustUtils.getNewId(ids, DUST_DEF_ID_BYTES);
						} else {
							StringBuilder sb = null;
							for (Integer i : keys) {
								sb = DustUtils.sbAppend(sb, "_", true, items.get(i).trim());
							}
							str = sb.toString();

							if (!ids.add(str)) {
								Dust.log(HANDLE_MISC_TAG_LEVEL_WARNING, "key collision", uName, str);
							}
						}

						DustHandle hTarget = Dust.getHandle(uName + DUST_SEP_TOKEN + str, tType);

						for (int i = 0; i < colCount; ++i) {
							str = items.get(i).trim();
							if (!DustUtils.isEmpty(str)) {
								Dust.access(DustAccess.Set, str, hTarget, fields.get(i));
							}
						}
					} else {
						if (fields.isEmpty()) {
							boolean inKey = (-1 == DustUtils.indexOf(tName, noId));

							int idx = 0;
							for (String h : items) {
								String colName = h.trim();

								if (inKey && colName.endsWith("id")) {
									keys.add(idx);
								} else {
									inKey = false;
								}

								DustHandle att = Dust.getHandle(uCatName + DUST_SEP_TOKEN + colName, HANDLE_MIND_ASP_ATTRIBUTE);
								
								Dust.access(DustAccess.Set, tType, att, HANDLE_MISC_ATT_PARENT);
								Dust.access(DustAccess.Insert, att, tType, HANDLE_MISC_ATT_MEMBERS);

								fields.add(att);
								++idx;
							}

							Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Reading unit", uName, fields);
						}
					}

					items.clear();
				}

			}

			try {
				Dust.access(DustAccess.Set, unit, hMsg, HANDLE_MISC_ATT_TARGET);
				Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_SAVE, hMsg, HANDLE_MIND_ATT_CMD);
				Dust.access(DustAccess.Commit, null, hMsg);

//			iUnit.content.put(hAttUnitState, hTagStateInSync);
			} finally {
				Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MIND_ATT_CMD);
				Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MISC_ATT_TARGET);

			}
		}

		try {
			Dust.access(DustAccess.Set, uCat, hMsg, HANDLE_MISC_ATT_TARGET);
			Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_SAVE, hMsg, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Commit, null, hMsg);

//		iUnit.content.put(hAttUnitState, hTagStateInSync);
		} finally {
			Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MISC_ATT_TARGET);

		}

	}

}
