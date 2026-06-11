package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustMind;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.mod.utils.DustUtilsJson;

public class DustStreamJsonApiSerializerAgent extends DustStreamJsonApiAgent implements DustMind.Bootloader {

	@Override
	protected Object process(DustAccess access) throws Exception {

		String unitId = Dust.access(DustAccess.Peek, null, null, TOKEN_KEY);
		DustHandle unit = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);
		Dust.access(DustAccess.Delete, null, null, TOKEN_DATA);

		Object streamSource = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_SOURCE);
		Map<String, Object> sp = new HashMap<String, Object>();

		String fileName = null;

		if (!DustUtils.isEmpty(unitId)) {
			if (null == unit) {
				unit = Dust.getUnit(unitId, true);
			}

			String fn = Dust.access(DustAccess.Peek, unitId, null, TOKEN_ALIAS);
			if (fn.contains("{")) {
				fn = MessageFormat.format(fn, unitId);
			}
			fileName = fn + DUST_EXT_JSON;

//			fileName = DustUtils.sbAppend(null, "/", false, Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ROOTFOLDER), fn + DUST_EXT_JSON).toString();
//			f = new File(fileName);
		}

		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		switch (cmd) {
		case TOKEN_CMD_INFO:

			fileName = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ROOTFOLDER);

			sp.put(TOKEN_CMD, TOKEN_CMD_INFO);
			sp.put(TOKEN_STREAM_ROOTFOLDER, fileName);

			Dust.access(DustAccess.Process, sp, streamSource);

			Object files = sp.get(TOKEN_MEMBERS);
			Dust.access(DustAccess.Set, files, DustContext.Input, TOKEN_MEMBERS);

//			f = new File(fileName);
//
//			Dust.access(DustAccess.Reset, null, DustContext.Input, TOKEN_MEMBERS);
//
//			if (f.isDirectory()) {
//				for (String fn : f.list()) {
//					if (fn.endsWith(DUST_EXT_JSON)) {
//						String unitName = DustUtils.cutPostfix(fn, ".");
//						Dust.access(DustAccess.Insert, unitName, DustContext.Input, TOKEN_MEMBERS);
//					}
//				}
//			}

			break;
		case TOKEN_CMD_LOAD:
//			sp.put(TOKEN_CMD, TOKEN_CMD_LOAD);
//			sp.put(TOKEN_PATH, fileName);
//
//			InputStream is = Dust.access(DustAccess.Process, sp, streamSource);

			try (InputStream is = DustStreamUtils.getStream(TOKEN_CMD_LOAD, fileName)) {
				loadStream(unit, is);
			}
			break;
		case TOKEN_CMD_SAVE:
			Map<String, Object> target = storeUnit(unit);

			if (null == fileName) {
				Writer w = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_WRITER);
				DustUtilsJson.writeJson(w, target);
			} else {
//				sp.put(TOKEN_CMD, TOKEN_CMD_SAVE);
//				sp.put(TOKEN_PATH, fileName);
//
//				OutputStream os = Dust.access(DustAccess.Process, sp, streamSource);

				try (OutputStream os = DustStreamUtils.getStream(TOKEN_CMD_SAVE, fileName)) {
					DustUtilsJson.writeJson(os, target, DUST_CHARSET_UTF8);
				}
			}
			break;

		}
		return null;
	}
	
	@Override
	public void loadStreamBoot(DustHandle unit, InputStream is) throws Exception {
		loadStream(unit, is);
	}

}
