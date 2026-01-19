package me.giskard.dust.dev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import me.giskard.dust.Dust;
import me.giskard.dust.utils.DustUtils;

public class DustDevUtils implements DustDevConsts {

	// https://stackoverflow.com/questions/74674/how-do-i-check-cpu-and-memory-usage-in-java
	public static String memInfo() {
		Runtime runtime = Runtime.getRuntime();

		runtime.gc();

		NumberFormat format = NumberFormat.getInstance();
		StringBuilder sb = new StringBuilder();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		sb.append("Free memory: ");
		sb.append(format.format(freeMemory / 1024));
		sb.append("<br/>");
		sb.append("Allocated memory: ");
		sb.append(format.format(allocatedMemory / 1024));
		sb.append("<br/>");
		sb.append("Max memory: ");
		sb.append(format.format(maxMemory / 1024));
		sb.append("<br/>");
		sb.append("Total free memory: ");
		sb.append(format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
		sb.append("<br/>");

		return sb.toString();

	}

	@Deprecated
	protected void loadExtFile(DustObject unit, File extFile) throws IOException, FileNotFoundException {
		if (extFile.isFile()) {
			try (FileInputStream fis = new FileInputStream(extFile); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();

					if (!DustUtils.isEmpty(line)) {
						if (line.startsWith("#")) {
							continue;
						}
						String[] ext = line.split("\\|");

						String[] access = ext[0].trim().split("/");
						DustObject aType = Dust.getObject(unit, DustUtils.getMindMeta(TOKEN_KBMETA_TYPE), access[0], DustOptCreate.Meta);
						DustObject aCfg = Dust.getObject(unit, aType, access[1], DustOptCreate.None);

						if (null == aCfg) {
							Dust.log(TOKEN_LEVEL_WARNING, "No object found for id", ext[0]);
						} else {
							String val = ext[2].trim();
							Object v = val;

							if (val.startsWith("!!")) {
								switch (val) {
								case "!!true":
									v = true;
									break;
								case "!!false":
									v = false;
									break;
								case "!!null":
									v = null;
									break;
								default:
									v = Long.valueOf(val.substring(2));
									break;
								}
							} else if (val.startsWith("!>")) {
								v = Dust.getObject(unit, null, val.substring(2), DustOptCreate.None);
							}

							Dust.access(DustAccess.Set, v, aCfg, (Object[]) ext[1].trim().split("/"));
							Dust.log(TOKEN_LEVEL_TRACE, "change applied", line);
						}
					}
				}
			}
		} else {
			Dust.log(TOKEN_LEVEL_WARNING, "No extension file found", extFile.getName());
		}
	}
}
