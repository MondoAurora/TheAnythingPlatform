package me.giskard.dust.dev;

import java.text.NumberFormat;

public class DustDevUtils {
	
	//https://stackoverflow.com/questions/74674/how-do-i-check-cpu-and-memory-usage-in-java
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
}
