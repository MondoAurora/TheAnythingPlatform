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

	String defRoot;

	public DustStreamSrcFileAgent() {
		this(".");
	}

	public DustStreamSrcFileAgent(String defRoot) {
		this.defRoot = defRoot;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_CMD);

		String root = Dust.access(DustAccess.Peek, defRoot, null, TOKEN_STREAM_ATT_ROOTFOLDER);
		File r = getRootFolder(root);

		String path = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_PATH);
		File f = DustUtils.isEmpty(path) ? r : new File(r, path);

		String token = null;
		switch (cmd) {
		case TOKEN_MISC_TAG_CMD_LOAD:
			token = TOKEN_STREAM_ATT_INPUT;
			break;
		case TOKEN_MISC_TAG_CMD_SAVE:
			token = TOKEN_STREAM_ATT_OUTPUT;
			break;
		case TOKEN_MISC_TAG_CMD_INFO:
			DustUtilsFile.checkPathBound(f, r, true);

			Dust.access(DustAccess.Reset, null, DustContext.Input, TOKEN_MISC_ATT_MEMBERS);
			int rpl = r.getCanonicalPath().length();

			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
						Dust.access(DustAccess.Insert, ff.getCanonicalPath().substring(rpl), DustContext.Input, TOKEN_MISC_ATT_MEMBERS);
				}
			} else {
				String unitName = DustUtils.cutPostfix(f.getName(), ".");
				Dust.access(DustAccess.Insert, unitName, DustContext.Input, TOKEN_MISC_ATT_MEMBERS);
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

	protected File getRootFolder(String root) {
		File r = DustUtils.isEmpty(root) ? null : new File(root);
		return r;
	}

	@Override
	public <StreamType> StreamType optGetStream(String cmd, String root, String path) throws Exception {
		File r = getRootFolder(root);
		File f = (null == r) ? new File(path) : new File(r, path);

		return createStream(cmd, r, f);
	}

	@SuppressWarnings("unchecked")
	public <StreamType> StreamType createStream(String cmd, File r, File f) throws Exception {
		DustUtilsFile.checkPathBound(f, r, true);

		Object stream = null;

		switch (cmd) {
		case TOKEN_MISC_TAG_CMD_LOAD:
			stream = f.isFile() ? new FileInputStream(f) : null;
			break;
		case TOKEN_MISC_TAG_CMD_SAVE:
			File p = f.getParentFile();
			DustUtilsFile.ensureDir(p);
			stream = new FileOutputStream(f);
			break;
		}

		return (StreamType) stream;
	}
}
