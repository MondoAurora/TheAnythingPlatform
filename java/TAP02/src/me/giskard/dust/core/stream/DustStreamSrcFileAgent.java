package me.giskard.dust.core.stream;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

public class DustStreamSrcFileAgent extends DustAgent implements DustStreamConsts {

	String defRoot;
	File backupFolder;

	public DustStreamSrcFileAgent() {
		this.defRoot = ".";
	}

	@Override
	public void process() throws Exception {

		String root = Dust.access(DustAccess.Peek, defRoot, null, HANDLE_STREAM_ATT_ROOTFOLDER);
		File r = getRootFolder(root);

		String bak = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_BACKUPFOLDER);
		backupFolder = getRootFolder(bak);

		DustHandle hTarget = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);
		
		String path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PATH);
		String name = Dust.access(DustAccess.Peek, hTarget.getId(), null, HANDLE_MISC_ATT_KEY);

		if (null != hTarget) {
			String p = null;

			if (HANDLE_MIND_ASP_UNIT.equals(hTarget.getType())) {
				p = "/" + name + Dust.DUST_EXT_JSON;
			}

			if (null != p) {
				path += p;
			}
		}

		File f = DustUtils.isEmpty(path) ? r : new File(r, path);
		DustUtilsFile.checkPathBound(f, r, false);
//		DustUtilsFile.checkPathBound(f, r, true);

		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
		DustHandle hStreamAtt = null;

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			if (f.isFile()) {
				hStreamAtt = HANDLE_STREAM_ATT_INPUT;
			}
		} else if (HANDLE_MISC_TAG_CMD_SAVE.equals(cmd)) {
			hStreamAtt = HANDLE_STREAM_ATT_OUTPUT;
		} else if (HANDLE_MISC_TAG_CMD_INFO.equals(cmd)) {
			Dust.access(DustAccess.Reset, null, null, HANDLE_MISC_ATT_MEMBERS);

			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
					Dust.access(DustAccess.Insert, ff, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MISC_ATT_MEMBERS);
				}
			} else {
				String unitName = DustUtils.cutPostfix(f.getName(), ".");
				Dust.access(DustAccess.Insert, unitName, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MISC_ATT_MEMBERS);
			}
		}

		if (null != hStreamAtt) {
			DustHandle hNext = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);

			try (Closeable cs = createStream(cmd, f)) {
				Dust.access(DustAccess.Set, cs, hNext, hStreamAtt);
				Dust.access(DustAccess.Set, cmd, hNext, HANDLE_MIND_ATT_CMD);
				Dust.access(DustAccess.Set, name, hNext, HANDLE_MISC_ATT_KEY);
				Dust.access(DustAccess.Commit, null, hNext);
			} finally {
				Dust.access(DustAccess.Delete, null, hNext, hStreamAtt);
				Dust.access(DustAccess.Delete, null, hNext, HANDLE_MIND_ATT_CMD);
				Dust.access(DustAccess.Delete, null, hNext, HANDLE_MISC_ATT_KEY);
			}
		}
	}

	protected File getRootFolder(String root) {
		File r = DustUtils.isEmpty(root) ? null : new File(root);
		return r;
	}

	@SuppressWarnings("unchecked")
	public <StreamType> StreamType createStream(DustHandle cmd, File f) throws Exception {

		Object stream = null;

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			stream = new FileInputStream(f);
		} else if (HANDLE_MISC_TAG_CMD_SAVE.equals(cmd)) {
			if (f.isFile()) {
				DustUtilsFile.optBackup(backupFolder, f);
			} else {
				File p = f.getParentFile();
				DustUtilsFile.ensureDir(p);
			}

			stream = new FileOutputStream(f);
		}

		return (StreamType) stream;
	}
}
