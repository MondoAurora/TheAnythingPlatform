package me.giskard.dust.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;

public class DustUtilsFile extends DustUtils implements DustUtilsConsts {
	
	public static File ensureDir(String fName) throws Exception {
		if (DustUtils.isEmpty(fName)) {
			return null;
		}
		File f = new File(fName);
		ensureDir(f);
		return f;
	}


	public static void ensureDir(File f) throws Exception {
		if ((null != f) && !f.isDirectory() && !f.mkdirs()) {
			throw new IOException("failed to create directory " + f);
		}
	}
	
	public static File optGetFile(Object... path) throws IOException {
		String p = DustUtils.toString(DustUtils.sbAppend(null, File.separator, false, path));
		File f = new File(p);
		return f.isFile() ? f : null;
	}

	public static File getFile(Object root, Object... path) throws Exception {

		String fileName = Dust.access(DustAccess.Peek, null, root, path);
		Dust.log(TOKEN_LEVEL_TRACE, "Accessing file", fileName);

		File f;
		if ( fileName.startsWith(File.separator) ) {
			f = new File(fileName);
		} else {
			File home = new File(System.getProperty("user.home"));
			f = DustUtils.isEmpty(fileName) ? home : new File(home, fileName);
		}

		return f;
	}


	public static String addHash2(String str) {
		return addHash2(str, ".");
	}

	public static String addHash2(String str, String sep) {
		return new StringBuilder(getHash2(str, sep)).append(File.separator).append(str).toString();
	}

	public static String getHash2(String str) {
		return getHash2(str, ".");
	}

	public static String getHash2(String str, String sep) {
		return DustUtils.getHash2(cutPostfix(str, sep), File.separator);
	}

	public static boolean exists(Object... pathSegments) {
		String path = DustUtils.sbAppend(null, File.separator, false, pathSegments).toString();

		return new File(path).exists();
	}

	public static final FileFilter FF_DIR = new FileFilter() {
		@Override
		public boolean accept(File f) {
			return f.isDirectory();
		}
	};

	public static class ExtFilter implements FileFilter, FilenameFilter {
		String ext;

		public ExtFilter(String ext) {
			this.ext = ext.toLowerCase();
		}

		@Override
		public boolean accept(File f) {
			return accept(f, f.getName());
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(ext);
		}
	}

	public static enum FileProcessMode {
		files(true, false), directories(false, true), all(true, true);

		public final boolean f;
		public final boolean d;

		FileProcessMode(boolean f, boolean d) {
			this.f = f;
			this.d = d;
		}
	}

	public static abstract class FileProcessor {
		public final FileProcessMode mode;

		public FileProcessor(FileProcessMode mode) {
			this.mode = mode;
		}

		public FileProcessor() {
			this(FileProcessMode.files);
		}

		public abstract boolean processFile(File f);
	}

	// returns count of processed files on success, negative if proc terminated the
	// visit
	public static int procRecursive(File f, FileProcessor proc, FileFilter ff) {
		int count = 0;

		if (null != f) {
			if (f.isFile()) {
				if (((null == ff) || ff.accept(f)) && proc.mode.f) {
					++count;
					if (!proc.processFile(f)) {
						return -count;
					}
				}
			} else {
				for (File fl : f.listFiles()) {
					int c = procRecursive(fl, proc, ff);
					if (c < 0) {
						return (-count) + c;
					} else {
						count += c;
					}
				}
			}
		}

		return count;
	}

	public static boolean checkPathBound(String path, boolean throwEx) throws IOException {
		return checkPathBound(path, new File("."), throwEx);
	}

	public static boolean checkPathBound(String path, File root, boolean throwEx) throws IOException {
		return checkPathBound(new File(root, path), root, throwEx);
	}

	public static boolean checkPathBound(File f, File root, boolean throwEx) throws IOException {
		if ( null == root ) {
			return true;
		}
		
		String fcp = f.getCanonicalPath();
		String rcp = root.getCanonicalPath();
		boolean ok = fcp.startsWith(rcp);
		if (throwEx && !ok) {
			return DustException.wrap(null, "File path bounds check failed", fcp, "root", rcp);
		}
		return ok;
	}

	public static String fileToString(File f) throws Exception {
		if (f.isFile()) {
			StringBuilder resultStringBuilder = new StringBuilder();
			try (FileInputStream fis = new FileInputStream(f); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				String line;
				while ((line = br.readLine()) != null) {
					resultStringBuilder.append(line).append("\n");
				}
			}
			return resultStringBuilder.toString();
		}

		return null;
	}

}
