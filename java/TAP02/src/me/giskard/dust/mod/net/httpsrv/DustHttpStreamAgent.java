package me.giskard.dust.mod.net.httpsrv;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.net.DustNetUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

public class DustHttpStreamAgent extends DustAgent implements DustNetConsts {

	@Override
	public void process() throws Exception {
		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET, HANDLE_NET_ATT_SRVCALL_RESPONSE);

		if (null != response) {
			String path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET, HANDLE_NET_ATT_SRVCALL_PATHINFO);

			if (DustUtils.isEmpty(path)) {
				path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PATH);
			}

			if (!DustUtilsFile.checkPathBound(path, false)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_REJECT, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_TAGS);
				return;
			}

			Collection<Object> roots = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, HANDLE_MISC_ATT_SOURCE);

			File f = null;

			for (Object root : roots) {
				if (null != (f = DustUtilsFile.optGetFile(root, path))) {
					break;
				}

//				String p = Dust.access(KBAccess.Get, null, root, HANDLE_PATH);
//
//				if (!DustUtils.isEmpty(p)) {
//					String res = Dust.access(KBAccess.Get, path, root, HANDLE_CHILDMAP, path);
//					if (null != (f = DustStreamUtils.optGetFile(p, res))) {
//						break;
//					}
//				}
			}

			if (null == f) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_REJECT, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_TAGS);
//				return HANDLE_MIND_TAG_RESULT_REJECT;
			}

			String ct = DustNetUtils.getContentType(f);

			response.setContentType(ct);

			OutputStream out = response.getOutputStream();
			Files.copy(f.toPath(), out);
			out.flush();
		}

		Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_ACCEPT, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_TAGS);
//		return HANDLE_MIND_TAG_RESULT_ACCEPT;
	}

}
