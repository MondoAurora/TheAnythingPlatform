package me.giskard.dust.core.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustMind;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

public class DustStreamSrcFileAgent extends DustAgent implements DustMind.StreamSource, DustStreamConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		String root = Dust.access(DustAccess.Peek, ".", null, TOKEN_STREAM_ROOTFOLDER);
		String path = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);

		File r = new File(root);
		File f = DustUtils.isEmpty(path) ? r : new File(r, path);

		String token = null;
		switch (cmd) {
		case TOKEN_CMD_LOAD:
			token = TOKEN_INPUT_STREAM;
			break;
		case TOKEN_CMD_SAVE:
			token = TOKEN_OUTPUT_STREAM;
			break;
		case TOKEN_CMD_INFO:
			DustUtilsFile.checkPathBound(f, r, true);

			Dust.access(DustAccess.Reset, null, DustContext.Input, TOKEN_MEMBERS);
			int rpl = r.getCanonicalPath().length();

			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
						Dust.access(DustAccess.Insert, ff.getCanonicalPath().substring(rpl), DustContext.Input, TOKEN_MEMBERS);
				}
			} else {
				String unitName = DustUtils.cutPostfix(f.getName(), ".");
				Dust.access(DustAccess.Insert, unitName, DustContext.Input, TOKEN_MEMBERS);
			}

			break;
		}

		Object stream = null;

		if (null != token) {
			stream = optGetStream(cmd, root, path);
			Dust.access(DustAccess.Set, stream, null, token);
		}

		return stream;
	}

	@Override
	public <StreamType> StreamType optGetStream(String cmd, String root, String path) throws Exception {
		File r = DustUtils.isEmpty(root) ? null : new File(root);
		File f = (null == r) ? new File(path) : new File(r, path);

		return createStream(cmd, r, f);
	}

	@SuppressWarnings("unchecked")
	public <StreamType> StreamType createStream(String cmd, File r, File f) throws Exception {
		DustUtilsFile.checkPathBound(f, r, true);

		Object stream = null;

		switch (cmd) {
		case TOKEN_CMD_LOAD:
			stream = f.isFile() ? new FileInputStream(f) : null;
			break;
		case TOKEN_CMD_SAVE:
			File p = f.getParentFile();
			DustUtilsFile.ensureDir(p);
			stream = new FileOutputStream(f);
			break;
		}

		return (StreamType) stream;
	}
}
