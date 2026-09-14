package me.giskard.dust.core.stream;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.DustMachine;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.tokens.giskard_me.DustGenTokens_dev_1;

@SuppressWarnings("rawtypes")
public class DustStreamAgent extends DustAgent implements DustMachine.StreamSource, DustStreamConsts, DustNetConsts, DustGenTokens_dev_1 {

	@Override
	protected Object process(DustAccess access) throws Exception {
		DustHandle hCmd = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_CMD);
		DustHandle hNext = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_NEXT);

		DustHandle hTarget = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);
		String name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_KEY);
		
		DustHandle hSource = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_SOURCE);

		if (null != hTarget) {
			switch ( hTarget.getId() ) {
			case TOKEN_MIND_ASP_UNIT:
				hTarget = Dust.getUnit(name, true);
				break;
			}
			Dust.access(DustAccess.Set, hCmd, hSource, TOKEN_MIND_ATT_CMD);
			Dust.access(DustAccess.Set, hTarget, hSource, TOKEN_MISC_ATT_TARGET);
			Dust.access(DustAccess.Set, name, hSource, TOKEN_MISC_ATT_KEY);
			Dust.access(DustAccess.Set, hNext, hSource, TOKEN_MIND_ATT_NEXT);

			Dust.access(DustAccess.Set, hCmd, hNext, TOKEN_MIND_ATT_CMD);
			Dust.access(DustAccess.Set, hTarget, hNext, TOKEN_MISC_ATT_TARGET);

			Dust.access(DustAccess.Process, null, hSource);
		} else {

			Collection<String> unitNames = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_RESOLVER_UNIT_NAMES);

			DustHandle hData = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_DATA);
			ArrayList<DustHandle> options = new ArrayList<>();
			String uName = null;
			DustHandle hRes = null;

			for (String un : unitNames) {
				DustHandle uRes = Dust.getUnit(un, true);

				if (null != uRes) {
					for (DustHandle hr : DustMachineUtils.getUnitMembers(uRes)) {
						boolean found = Dust.access(DustAccess.Check, hData, hr, TOKEN_MISC_ATT_APPEARS);

						if (found) {
							options.add(hr);
							hRes = hr;
							uName = un;
						}
					}
				}
			}

			String p = "localStore/res/59d365a0.jpg";

			if (!options.isEmpty()) {

				String path = Dust.access(DustAccess.Peek, null, hRes, TOKEN_MISC_ATT_PATH);

				p = "localStore/" + uName.substring(0, uName.lastIndexOf("/") + 1) + path;
			}

			try (FileInputStream fi = new FileInputStream(p)) {
				Dust.access(DustAccess.Set, fi, hNext, TOKEN_STREAM_ATT_INPUT);
				Dust.access(DustAccess.Set, TOKEN_DEV_TAG_CMD_TEST, hNext, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, null, hNext);
			}
		}

		if (null != hNext) {
			return null;
		}

		String unit = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_UNIT);
		if (DustUtils.isEmpty(unit)) {
			// access control mock
			return null;
		}

		String author = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_AUTHOR);
		String id = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_ID);

		String unitId = author + "_streams.1";
		String streamId = author + "_streams.1$" + id;

		DustHandle hUnit = Dust.getUnit(unitId, true);
		DustHandle hStream = Dust.getHandle(hUnit, null, streamId, DustOptCreate.None);

		String root = Dust.access(DustAccess.Peek, ".", null, TOKEN_STREAM_ATT_ROOTFOLDER);
		File r = getRootFolder(root);

		String path = Dust.access(DustAccess.Peek, null, hStream, TOKEN_MISC_ATT_PATH);
		File f = DustUtils.isEmpty(path) ? r : new File(r, path);

		String cmd = hCmd.getId();
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

			Dust.access(DustAccess.Reset, null, DustContext.Message, TOKEN_MISC_ATT_MEMBERS);
			int rpl = r.getCanonicalPath().length();

			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
					Dust.access(DustAccess.Insert, ff.getCanonicalPath().substring(rpl), DustContext.Message, TOKEN_MISC_ATT_MEMBERS);
				}
			} else {
				String unitName = DustUtils.cutPostfix(f.getName(), ".");
				Dust.access(DustAccess.Insert, unitName, DustContext.Message, TOKEN_MISC_ATT_MEMBERS);
			}

			break;
		}

		Closeable stream = null;

		if (null != token) {
			stream = optGetStream(cmd, root, path);

			String mimeType = "image/png";

			if (null != stream) {
				try {
					DustHandle target = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);
					Dust.access(DustAccess.Set, stream, target, TOKEN_STREAM_ATT_INPUT);
					Dust.access(DustAccess.Set, mimeType, target, TOKEN_STREAM_ATT_MIMETYPE);

					DustException.wrap(null, "Not followed params - message refactor");
					Map params = DustUtilsData.optLoadMapping(target, null);

					Dust.access(DustAccess.Process, params, target);
				} finally {
					stream.close();
				}

			}

//			HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_ATT_SRVCALL_RESPONSE);
//			if (null == response) {
//				Dust.log(TOKEN_MISC_TAG_LEVEL_ERROR, "no response given?");
//			}
//
//			response.setContentType("image/png");
//			OutputStream out = response.getOutputStream();
//
//			InputStream is = (InputStream) stream;
//			DustStreamUtils.copyStream(is, out);
//			is.close();
//
//			Dust.access(DustAccess.Set, stream, null, token);
		}

		return null;
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
