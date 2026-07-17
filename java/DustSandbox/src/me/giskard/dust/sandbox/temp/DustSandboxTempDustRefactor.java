package me.giskard.dust.sandbox.temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.core.utils.DustUtilsFile.FileProcessor;
import me.giskard.dust.mod.utils.DustUtilsJson;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DustSandboxTempDustRefactor implements DustConsts {

	public static void main(String[] args) throws Exception {
		String srcName = "../DustJsonApi/gen/me/giskard/tokens/DustGenTokens_dust_1.java";

		File f = new File(srcName);

		if (!f.isFile()) {
			Dust.log(TOKEN_MISC_TAG_LEVEL_ERROR, "refactor source not found", f.getCanonicalPath());
			return;
		}

		PrintStream ps = System.out;

		Pattern ptTokenDef = Pattern.compile(".*TOKEN_([a-zA-Z]*)_.*\"(.*)\".*");

		Map<String, String> refactor = new TreeMap<>();
		Map<String, File> units = new TreeMap<>();

		try (InputStream is = new FileInputStream(f)) {

			try (BufferedReader br = new BufferedReader(new InputStreamReader(is, DUST_CHARSET_UTF8))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (!DustUtils.isEmpty(line)) {

						Matcher m = ptTokenDef.matcher(line);

						if (m.matches()) {
							String labelUnit = m.group(1).toLowerCase();
							String id = m.group(2);

							if (id.toLowerCase().startsWith(labelUnit)) {
								continue;
							}

							int sep = id.indexOf(".");

							String toId = labelUnit + id.substring(sep);

							refactor.put(id, toId);

							units.put(DustUtils.getPrefix(id, DUST_SEP_TOKEN), null);
							units.put(DustUtils.getPrefix(toId, DUST_SEP_TOKEN), null);

							line = line.replace(id, toId);
						}
					}

					ps.println(line);
				}
			}
		}

		File root = new File("localStore");

		DustUtilsFile.procRecursive(root, new FileProcessor() {

			@Override
			public boolean processFile(File f) {
				try {
					String fn = f.getName();

					ps.println("\n\n Reading file" + fn);

					ArrayList<String> content = new ArrayList<String>();
					boolean changed = false;

					try (InputStream is = new FileInputStream(f)) {

						try (BufferedReader br = new BufferedReader(new InputStreamReader(is, DUST_CHARSET_UTF8))) {
							String line = null;

							while ((line = br.readLine()) != null) {
								if (!DustUtils.isEmpty(line)) {
									for (Map.Entry<String, String> re : refactor.entrySet()) {
										String key = re.getKey();

										if (line.contains(key)) {
											changed = true;
											line = line.replace(key, re.getValue());
										}
									}
								}

								content.add(line);
							}
						}
					}

					if (changed) {
						ps.println("\n\n*** BEGIN ***\n");

//						f.renameTo(new File(f.getAbsolutePath() + ".bak"));

//						try (PrintWriter fw = new PrintWriter(f)) {
							for (String c : content) {
//								fw.println(c);
								ps.println(c);
							}

//						}

						ps.println("*** END  ***");

						String un = DustUtils.cutPostfix(fn, ".");
						if (units.containsKey(un)) {
							units.put(un, f);
						}
					} else {
						ps.println("\n\n*** UNCHANGED ***\n");

					}
				} catch (Throwable ex) {
					DustException.wrap(ex, "in file", f.getAbsolutePath());
				}

				return true;
			}
		}, new DustUtilsFile.ExtFilter(".json"));

		Map<String, Map> unitData = new TreeMap<String, Map>();

		for (Map.Entry<String, File> ue : units.entrySet()) {
			try (FileInputStream fis = new FileInputStream(ue.getValue())) {
				unitData.put(ue.getKey(), DustUtilsJson.readJson(fis, DUST_CHARSET_UTF8));
			} catch (Throwable ex) {
				DustException.wrap(ex, ue.getKey(), ue.getValue().getAbsolutePath());
			}
		}

		for (Map.Entry<String, Map> ud : unitData.entrySet()) {
			String uid = ud.getKey();

			ArrayList<Map> data = (ArrayList<Map>) ud.getValue().get("data");

			for (Iterator<Map> di = data.iterator(); di.hasNext();) {
				Map idea = di.next();
				String id = (String) idea.get("id");

				if (!id.startsWith(uid)) {
					di.remove();

					String tid = DustUtils.getPrefix(id, DUST_SEP_TOKEN);

					Dust.access(DustAccess.Insert, idea, unitData, tid, "data", KEY_ADD);
				}
			}
		}
	}
}
