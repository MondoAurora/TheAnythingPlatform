package me.giskard.dust.mod.android.stream;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.mod.android.TAPAndroidUtils;

public class DustStreamSrcBinaryAgent extends DustAgent implements DustStreamConsts {

	@Override
	public void init() throws Exception {
		Enumeration<URL> sysRes = this.getClass().getClassLoader().getResources("");

		while ( sysRes.hasMoreElements() ) {
			URL u = sysRes.nextElement();

			Dust.log ( HANDLE_MISC_TAG_LEVEL_TRACE, u);
		}
	}

	@Override
	public void process() throws Exception {

		String root = Dust.access(DustAccess.Peek, "", null, HANDLE_STREAM_ATT_ROOTFOLDER);

		// DustHandle hTarget = Dust.access(DustAccess.Peek, null, null,
		// HANDLE_MISC_ATT_TARGET);

		String name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_KEY);

		String resPath = root + "/" + name + Dust.DUST_EXT_JSON;

		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
		String path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PATH);

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			try (InputStream is = TAPAndroidUtils.getAssets().open(resPath)) {
				if (null == is) {
					DustException.wrap(null, "Binary loader stream not found", resPath);
				} else {
					DustStreamUtils.copyStream(is, new FileOutputStream(path));
				}
			}
		}
		// else if (HANDLE_MISC_TAG_CMD_INFO.equals(cmd)) {
		// Dust.access(DustAccess.Reset, null, null, HANDLE_MISC_ATT_MEMBERS);
		//
		// if (f.isDirectory()) {
		// for (File ff : f.listFiles()) {
		// Dust.access(DustAccess.Insert, ff, HANDLE_DUST_ATT_CTX_MSG,
		// HANDLE_MISC_ATT_MEMBERS);
		// }
		// } else {
		// String unitName = DustUtils.cutPostfix(f.getName(), ".");
		// Dust.access(DustAccess.Insert, unitName, HANDLE_DUST_ATT_CTX_MSG,
		// HANDLE_MISC_ATT_MEMBERS);
		// }
		// }

	}

}
