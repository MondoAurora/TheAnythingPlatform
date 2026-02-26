package me.giskard.dust.core.stream;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;

public class DustStreamUtils implements DustUtilsConsts, DustMindConsts {

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

	public static String csvOptEscape(String valStr, String sepChar) {
		if ( null == valStr ) {
			return "";
		}

		String ret = valStr.trim();
		
		if ( valStr.startsWith("\"") && valStr.endsWith("\"")) {
			return ret;
		}

		if ( valStr.contains(sepChar) || valStr.contains("\"") || valStr.contains("\n") ) {
			ret = csvEscape(valStr, true);
		}
		return ret;
	}
	
	static Pattern PT_ESC = Pattern.compile("\\s+", Pattern.MULTILINE);

	public static String csvEscape(String valStr, boolean addQuotes) {
		String ret = "";
		
		if (null != valStr) {
			
			ret = valStr.replace("\"", "\"\"");
			ret = PT_ESC.matcher(ret).replaceAll(" ");
		}

		if ( addQuotes ) {
			ret = "\"" + ret + "\"";
		}

		return ret;
	}

	public static String csvOptUnEscape(String valStr, boolean removeQuotes) {
		if ( DustUtils.isEmpty(valStr) || !valStr.contains("\"") ) {
			return valStr;
		}

		String ret = valStr;
		if ( removeQuotes ) {
			if ( ret.startsWith("\"") ) {
				ret = ret.substring(1);
			}
			if ( ret.endsWith("\"") ) {
				ret = ret.substring(0, ret.length() - 1);
			}
		}

		ret = ret.replace("\"\"", "\"");

		return ret;
	}

	public static class CsvLineReader {
		final char sep;
		final Collection<String> target;

		private StringBuilder sb;
		private boolean inQuote;
		private boolean prevQuote;
		private int pos;

		public CsvLineReader(String sep, Collection<String> target) {
			this.sep = sep.charAt(0);
			this.target = target;
		}

		void throwError(String line, String msg) {
			DustException.wrap(null, "CSV - " + msg + " in line", line, "at pos", pos);
		}

		public boolean csvReadLine(String line) {
			pos = 0;

			for (char c : line.toCharArray()) {
				++pos;

				switch ( c ) {
				case 65279:
					// BOM?
					break;
				case '\"':
					if ( null == sb ) {
						inQuote = true;
						sb = new StringBuilder();
						prevQuote = false;
					} else if ( inQuote ) {
						if ( prevQuote ) {
							sb.append(c);
							prevQuote = false;
						} else {
							prevQuote = true;
						}
					} else {
						throwError("Quotation mark in unquoted field!", line);
					}
					break;
				default:
					if ( c == sep ) {
						if ( null != sb ) {
							if ( inQuote && !prevQuote ) {
								sb.append(c);
							} else {
								target.add(sb.toString());
								sb = null;
							}
						} else if ( !prevQuote ) {
							target.add("");
						}
						prevQuote = false;
					} else {
						prevQuote = false;
						if ( null == sb ) {
							if ( Character.isWhitespace(c) ) {
								break;
							}
							sb = new StringBuilder();
							inQuote = false;
						}
						sb.append(c);
					}
					break;
				}
			}

			if ( null != sb ) {
				if ( inQuote && !prevQuote ) {
					sb.append("\n");
					return false;
				} else {
					target.add(sb.toString());
					sb = null;
				}
			} else if ( 0 < pos ) {
				target.add("");
			}

			return true;
		}
	}

}
