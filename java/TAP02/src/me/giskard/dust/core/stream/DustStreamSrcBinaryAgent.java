package me.giskard.dust.core.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;

public class DustStreamSrcBinaryAgent extends DustAgent implements DustStreamConsts {

	@Override
	public void process() throws Exception {

		String root = Dust.access(DustAccess.Peek, "", null, HANDLE_STREAM_ATT_ROOTFOLDER);

//		DustHandle hTarget = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);

		String name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_KEY);

		String resPath = root + "/" + name + Dust.DUST_EXT_JSON;

		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
		String path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PATH);

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			try (InputStream is = ClassLoader.getSystemResourceAsStream(resPath)) {
				DustStreamUtils.copyStream(is, new FileOutputStream(path));
			}
		}
//		else if (HANDLE_MISC_TAG_CMD_INFO.equals(cmd)) {
//			Dust.access(DustAccess.Reset, null, null, HANDLE_MISC_ATT_MEMBERS);
//
//			if (f.isDirectory()) {
//				for (File ff : f.listFiles()) {
//					Dust.access(DustAccess.Insert, ff, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MISC_ATT_MEMBERS);
//				}
//			} else {
//				String unitName = DustUtils.cutPostfix(f.getName(), ".");
//				Dust.access(DustAccess.Insert, unitName, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MISC_ATT_MEMBERS);
//			}
//		}

	}

}
