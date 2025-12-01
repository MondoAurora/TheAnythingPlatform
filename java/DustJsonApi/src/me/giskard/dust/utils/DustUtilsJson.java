package me.giskard.dust.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unchecked")
public class DustUtilsJson implements DustUtilsConsts {

	public static <RetType> RetType readJson(String fileName) throws Exception {
		return readJson(new File(fileName));
	}

	public static <RetType> RetType readJson(File f) throws Exception {
		Object ret = null;

		if (f.isFile()) {
			try (FileReader r = new FileReader(f)) {
				JSONParser p = new JSONParser();
				ret = p.parse(r);
			}
		}

		return (RetType) ret;
	}

	public static void writeJson(OutputStream os, Object ob) throws Exception {
		try (Writer w = new OutputStreamWriter(os)) {
			writeJson(w, ob);
			os.flush();
		}
	}

	public static void writeJson(String fileName, Object ob) throws Exception {
		writeJson(new File(fileName), ob);
	}

	public static void writeJson(File f, Object ob) throws Exception {
		DustUtilsFile.ensureDir(f.getParentFile());
		
		try (FileWriter w = new FileWriter(f)) {
			writeJson(w, ob);
			w.flush();
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
