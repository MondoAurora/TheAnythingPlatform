package me.giskard.dust.mod.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import me.giskard.dust.core.utils.DustUtilsConsts;

@SuppressWarnings("unchecked")
public class DustUtilsJson implements DustUtilsConsts {

	public static String toJson(Object ob) {
		return JSONValue.toJSONString(ob);
	}
	
	public static <RetType> RetType parseJson(String str) throws Exception {
		return (RetType) JSONValue.parse(str);
	}

	public static <RetType> RetType readJson(InputStream is, String encoding) throws Exception {
		Object ret = null;

		if (null != is) {
			try (Reader r = new InputStreamReader(is, Charset.forName(encoding))) {
				JSONParser p = new JSONParser();
				ret = p.parse(r);
			}
		}
		
		return (RetType) ret;
	}

	public static void writeJson(OutputStream os, Object ob, String encoding) throws Exception {
		try (Writer w = new OutputStreamWriter(os, encoding)) {
			writeJson(w, ob);
			os.flush();
		}
	}

	public static void writeJson(Writer w, Object ob) throws Exception {
		JSONValue.writeJSONString(ob, w);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean cloneWithJson(Map source, Map MISC_GEN_TARGET, String keySource, String keyMISC_GEN_TARGET) {
		Object ob = source.get(keySource);
		if ( null == ob ) {
			return false;
		}
		String rs= JSONObject.toJSONString((Map) ob);
		Object clone = JSONValue.parse(rs);
		MISC_GEN_TARGET.put(keyMISC_GEN_TARGET, clone);
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static Map cloneWithJson(Map map) {
		String rs= JSONObject.toJSONString(map);
		Object clone = JSONValue.parse(rs);
		return (Map) clone;
	}
}
