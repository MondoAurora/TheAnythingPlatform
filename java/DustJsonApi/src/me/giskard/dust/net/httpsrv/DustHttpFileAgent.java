package me.giskard.dust.net.httpsrv;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.DustConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.net.DustNetUtils;
import me.giskard.dust.stream.DustStreamUtils;
import me.giskard.dust.utils.DustUtils;

public class DustHttpFileAgent extends DustConsts.DustAgentBase implements DustNetConsts {

	@Override
	protected Object process(Map<String, Object> cfg, Object params) throws Exception {
		HttpServletResponse response = DustKBUtils.access(KBAccess.Peek, null, params, TOKEN_TARGET,
				TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String path = DustKBUtils.access(KBAccess.Get, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);
			
			if ( DustUtils.isEmpty(path) ) {
				 path = DustKBUtils.access(KBAccess.Get, null, params, TOKEN_PATH);
			}
			
			if (!DustStreamUtils.checkPathBound(path)) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}

			Collection<Object> roots = DustKBUtils.access(KBAccess.Get, Collections.EMPTY_LIST, cfg,
					TOKEN_SOURCE);

			File f = null;

			for (Object root : roots) {
				if (null != (f = DustStreamUtils.optGetFile(root, path))) {
					break;
				}

//				String p = DustKBUtils.access(KBAccess.Get, null, root, TOKEN_PATH);
//
//				if (!DustUtils.isEmpty(p)) {
//					String res = DustKBUtils.access(KBAccess.Get, path, root, TOKEN_CHILDMAP, path);
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
