package me.giskard.dust.net.httpsrv;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustHttpServletDispatcher extends HttpServlet implements DustNetConsts, DustStreamConsts {
	private static final long serialVersionUID = 1L;

	Collection<Map> agents;
	
	public DustHttpServletDispatcher(Object dispatch) {
		super();
		agents = (Collection<Map>) dispatch;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		long ts = System.currentTimeMillis();
		Throwable exception = null;
		String pathInfo = request.getPathInfo();

		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}

		try {
			synchronized (agents) { // quick and dirty

				Map target = null;
				
				for (Map agent : agents) {
					String p = DustKBUtils.access(DustAccess.Get, "@@@", agent, TOKEN_PREFIX);

					if (pathInfo.startsWith(p)) {
						target = agent;
						pathInfo = pathInfo.substring(p.length());
						if ( pathInfo.startsWith("/")) {
							pathInfo = pathInfo.substring(1);
						}
						break;
					}
				}

				if (null == target) {
					response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
					return;
				}
							
				Map msg = new HashMap(target);
				Map params = msg;

//				Map params = new HashMap();
//				
//				Map p = (Map) target.get(TOKEN_PARAMS);
//				if ( null != p ) {
//					params.putAll(p);
//				}
//				msg.put(TOKEN_PARAMS, params);

				DustKBUtils.access(DustAccess.Set, pathInfo, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

				DustKBUtils.access(DustAccess.Set, request, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_REQUEST);
				DustKBUtils.access(DustAccess.Set, response, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

				DustKBUtils.access(DustAccess.Set, request.getMethod(), params, TOKEN_TARGET,
						TOKEN_NET_SRVCALL_METHOD);

				Enumeration<String> ee;
				String n = null;

				for (ee = request.getAttributeNames(); ee.hasMoreElements();) {
					n = ee.nextElement();
					optAdd(params, TOKEN_NET_SRVCALL_ATTRIBUTES, n, request.getAttribute(n));
				}

				for (ee = request.getParameterNames(); ee.hasMoreElements();) {
					n = ee.nextElement();
					optAdd(params, TOKEN_PAYLOAD, n, request.getParameter(n));
				}

				for (ee = request.getHeaderNames(); ee.hasMoreElements();) {
					n = ee.nextElement();
					optAdd(params, TOKEN_NET_SRVCALL_HEADERS, n, request.getHeader(n));
				}
				
				String cmd = (String) params.get(TOKEN_CMD);
				if ( null == cmd ) {
					cmd = DustUtils.getPrefix(pathInfo, "/");
					params.put(TOKEN_CMD, cmd);
				}
				
				Dust.sendMessage(msg);

				int status = DustKBUtils.access(DustAccess.Peek, HttpServletResponse.SC_OK, params, TOKEN_TARGET,
						TOKEN_NET_SRVCALL_STATUS);

				response.setStatus(status);
			}
		} catch (Throwable t) {
			exception = t;
		} finally {
			Dust.log(null, "Http request: " + pathInfo, "Process time: " + (System.currentTimeMillis() - ts) + " msec",
					(null == exception) ? "Success" : "error: " + exception);
		}

		if (null != exception) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void optAdd(Object params, Object kind, String name, Object val) {
		DustKBUtils.access(DustAccess.Set, val, params, TOKEN_TARGET, kind, name);
	}
}