package me.giskard.dust.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;

public class DustStreamUtils implements DustUtilsConsts, DustMindConsts, DustStreamConsts {

	public static <RetType> RetType getStream() {
		Object streamSource = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_SOURCE);
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
		String fileName = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);
		
		return getStream(cmd, fileName, streamSource);
	}

	public static <RetType> RetType getStream(String cmd, String fileName) {
		Object streamSource = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_SOURCE);

		return getStream(cmd, fileName, streamSource);
	}

	public static <RetType> RetType getStream(String cmd, String fileName, Object streamSource) {
		Map<String, Object> sp = new HashMap<String, Object>();

		sp.put(TOKEN_CMD, cmd);
		sp.put(TOKEN_PATH, fileName);

		return Dust.access(DustAccess.Process, sp, streamSource);

	}

    public static boolean copyStream(InputStream source, OutputStream target) throws IOException {
        boolean success = false;
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = source.read(dataBuffer, 0, 1024)) != -1) {
            target.write(dataBuffer, 0, bytesRead);
            success = true;
        }
        return success;
    }

    public static String csvOptEscape(String valStr, String sepChar) {
		if (null == valStr) {
			return "";
		}

		String ret = valStr.trim();

		if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
			return ret;
		}

		if (valStr.contains(sepChar) || valStr.contains("\"") || valStr.contains("\n")) {
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

		if (addQuotes) {
			ret = "\"" + ret + "\"";
		}

		return ret;
	}

	public static String csvOptUnEscape(String valStr, boolean removeQuotes) {
		if (DustUtils.isEmpty(valStr) || !valStr.contains("\"")) {
			return valStr;
		}

		String ret = valStr;
		if (removeQuotes) {
			if (ret.startsWith("\"")) {
				ret = ret.substring(1);
			}
			if (ret.endsWith("\"")) {
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

				switch (c) {
				case 65279:
					// BOM?
					break;
				case '\"':
					if (null == sb) {
						inQuote = true;
						sb = new StringBuilder();
						prevQuote = false;
					} else if (inQuote) {
						if (prevQuote) {
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
					if (c == sep) {
						if (null != sb) {
							if (inQuote && !prevQuote) {
								sb.append(c);
							} else {
								target.add(sb.toString());
								sb = null;
							}
						} else if (!prevQuote) {
							target.add("");
						}
						prevQuote = false;
					} else {
						prevQuote = false;
						if (null == sb) {
							if (Character.isWhitespace(c)) {
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

			if (null != sb) {
				if (inQuote && !prevQuote) {
					sb.append("\n");
					return false;
				} else {
					target.add(sb.toString());
					sb = null;
				}
			} else if (0 < pos) {
				target.add("");
			}

			return true;
		}
	}

	// https://stackoverflow.com/questions/1265282/what-is-the-recommended-way-to-escape-html-symbols-in-plain-java
	// Apache text brings too many dependecies for one single function.
	
	public static String escapeHTML(String s) {
    StringBuilder out = new StringBuilder(Math.max(16, s.length()));
    
    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
            out.append("&#");
            out.append((int) c);
            out.append(';');
        } else {
            out.append(c);
        }
    }
    return out.toString();
}
}
