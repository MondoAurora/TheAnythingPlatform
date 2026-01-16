package me.giskard.dust.mvel;

import me.giskard.dust.DustConsts.DustAgent;

public class DustExprMvelAgent extends DustAgent implements DustExprMvelConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
