package me.giskard.dust.mod.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.DustMind;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

public class DustStreamSrcSftpAgent extends DustAgent implements DustMind.StreamSource, DustNetConsts, DustStreamConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, TOKEN_CMD_INFO, null, TOKEN_CMD);

		String knownHosts = Dust.access(DustAccess.Peek, "~/.ssh/known_hosts", null, TOKEN_NET_KNOWN_HOST);

		String root = Dust.access(DustAccess.Peek, ".", null, TOKEN_STREAM_ROOTFOLDER);
		String p1 = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);

		String path = DustUtils.sbAppend(null, "/", false, root, p1).toString();

		DustHandle accInfo = Dust.access(DustAccess.Peek, null, null, TOKEN_ACCESS);

		String url = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_STREAM_URL);
		String user = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_USER);
		String pass = Dust.access(DustAccess.Peek, null, accInfo, TOKEN_PASSWORD);

		JSch jsch = null;
		Session jschSession = null;
		ChannelSftp channelSftp = null;

		try {
			jsch = new JSch();
			jsch.setKnownHosts(knownHosts);
			jschSession = jsch.getSession(user, url);
			jschSession.setPassword(pass);
			jschSession.setTimeout(10000);
			jschSession.connect();

			channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
			channelSftp.connect();

//			String token = null;
			switch (cmd) {
			case TOKEN_CMD_LOAD:
//				token = TOKEN_INPUT_STREAM;
				break;
			case TOKEN_CMD_SAVE:
//				token = TOKEN_OUTPUT_STREAM;
				break;
			case TOKEN_CMD_INFO:
				try {
					java.util.Vector<ChannelSftp.LsEntry> vv = channelSftp.ls(path);
					if (vv != null) {
						for (ChannelSftp.LsEntry le : vv) {
							Dust.log(TOKEN_INFO, le.getLongname());
							
							String fn = le.getFilename();
							
							if ( fn.startsWith("AHQ")) {
								String fn2 = path + "/" + fn;
								channelSftp.get(fn2, "temp/" + fn);
							}
						}
					}
				} catch (SftpException e) {
					DustException.swallow(e, "listing folder", path);
				}

				break;
			}
		} finally {
			if ((null != channelSftp) && channelSftp.isConnected()) {
				try {
					channelSftp.disconnect();
				} catch (Throwable e) {
					DustException.swallow(e, "Disconnecting channel", path);
				}
			}
			if ((null != jschSession) && jschSession.isConnected()) {
				try {
					jschSession.disconnect();
				} catch (Throwable e) {
					DustException.swallow(e, "Disconnecting session", path);
				}
			}
		}

		Object stream = null;

//		if (null != token) {
//			stream = optGetStream(cmd, root, path);
//			Dust.access(DustAccess.Set, stream, null, token);
//		}

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
