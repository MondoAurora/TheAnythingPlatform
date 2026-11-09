package me.giskard.dust.mod.mvel;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustExprMvelAgent extends DustAgent implements DustExprMvelConsts {
	Map data = new HashMap();
	
	public static class MvelConn {
		public DustHandle getHandle(String id) {
			return Dust.getHandle(id, null);
		}

		public DustHandle getHandle(String id, DustHandle type) {
			return Dust.getHandle(id, type);
		}

		public <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
			return Dust.access(access, val, root, path);
		}
		
		public void log(DustHandle eventId, Object... params) {
			Dust.log(eventId, params);
		}
	}
	
	MvelConn conn = new MvelConn();

	@Override
	public void process() throws Exception {
		
		data.put("Dust", conn);
		
		addEnum(data, DustAccess.class);

		String command = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_SOURCE, HANDLE_TEXT_ATT_TEXT);
		DustHandle hTarget = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);
		
		String log = Dust.access(DustAccess.Peek, null, hTarget, HANDLE_TEXT_ATT_TEXT);

		Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "MVEL executor called", command, hTarget);
		
		StringBuilder sb = DustUtils.sbAppend(null, "\n", false, log, "> " + command);

		Object ret = DustExprMvelUtils.eval(command, null, data, "???");
		
		String result = DustUtils.toString(ret);
		Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "MVEL result", result);
		
		DustUtils.sbAppend(sb, "\n", false, result);
		
		Dust.access(DustAccess.Set, sb.toString(), hTarget, HANDLE_TEXT_ATT_TEXT);

		// TODO Auto-generated method stub
	}

	private void addEnum(Map data, Class<? extends Enum> enumClass) {
		Map values= new TreeMap();
		
		for (Enum e : enumClass.getEnumConstants()) {
			values.put(e.name(), e);
		}
		data.put(enumClass.getSimpleName(), values);
		
	}

//	void test(Map<String, Object> call) throws Exception {
//
//		String template = Dust.access(MindAccess.Peek, null, getConfig(), XDC_path);
//
//		Map<String, Object> metaColl = new TreeMap<>();
//
//		Map<String, String> m = Dust.access(MindAccess.Peek, Collections.EMPTY_MAP, getConfig(), XDC_meta);
//		for (Map.Entry<String, String> me : m.entrySet()) {
//			metaColl.put(me.getKey(), DustStreamJson.readJson(me.getValue()));
//		}
//
//		Map<String, Object> params = new TreeMap<>();
//		Map<String, Object> item = (Map<String, Object>) call.get("item");
//		params.put("item", item);
//
//		item.put("id", "hmm");
//		item.put("userEntity", "SeqCarbon");
//
//		loadParams(params, metaColl, XDC_type);
//		loadParams(params, metaColl, XDC_attributes);
//
//		params.put(XDC_target, Dust.access(MindAccess.Peek, null, getConfig(), XDC_target));
//
//		OutputStream os = Dust.access(MindAccess.Peek, null, call, XDC_targetStream);
//		boolean local = null == os;
//
//		if (local) {
//			os = new FileOutputStream("test.html");
//		}
//
//		CompiledTemplate compiled = TemplateCompiler.compileTemplate(new File(template));
//		TemplateRuntime.execute(compiled, (Map<String, Object>) params, os);
//
//		if (local) {
//			os.flush();
//			os.close();
//		}
//	}

}
