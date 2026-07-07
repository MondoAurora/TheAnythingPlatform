package me.giskard.dust.core.dev;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Collection;

import me.giskard.dust.core.Dust;

@SuppressWarnings("rawtypes")
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

	public static void loadConstHandles(Collection<String> classes) throws Exception {
		for (String cn : classes) {
			Class cc = Class.forName(cn);

			for (Field f : cc.getDeclaredFields()) {
				String fn = f.getName();
				Class<? extends String> fc = fn.getClass();
				if ( fn.startsWith("TOKEN") && String.class.equals(fc)) {
					String fv = (String) f.get(null);
					DustHandle h = Dust.getHandle(null, null, fv, DustOptCreate.Meta);
					Dust.access(DustAccess.Set, fn, h, TOKEN_NAME);
				}
			}
		}
	}
}
