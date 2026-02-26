package me.giskard.dust.mod.net.httpsrv;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.mod.net.DustNetConsts;
import me.giskard.dust.mod.net.DustNetUtils;

public class DustHttpFileAgent extends DustAgent implements DustNetConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String path = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

			if (DustUtils.isEmpty(path)) {
				path = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);
			}

			if (!DustUtilsFile.checkPathBound(path)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			Collection<Object> roots = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_SOURCE);

			File f = null;

			for (Object root : roots) {
				if (null != (f = DustStreamUtils.optGetFile(root, path))) {
					break;
				}

//				String p = Dust.access(KBAccess.Get, null, root, TOKEN_PATH);
//
//				if (!DustUtils.isEmpty(p)) {
//					String res = Dust.access(KBAccess.Get, path, root, TOKEN_CHILDMAP, path);
//					if (null != (f = DustStreamUtils.optGetFile(p, res))) {
//						break;
//					}
//				}
			}

			if (null == f) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return TOKEN_RESULT_REJECT;
			}

			String ct = DustNetUtils.getContentType(f);

			response.setContentType(ct);

			OutputStream out = response.getOutputStream();
			Files.copy(f.toPath(), out);
			out.flush();
		}

		return TOKEN_RESULT_ACCEPT;
	}

}
