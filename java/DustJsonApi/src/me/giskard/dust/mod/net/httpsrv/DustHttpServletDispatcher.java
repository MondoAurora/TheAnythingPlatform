package me.giskard.dust.mod.net.httpsrv;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustHttpServletDispatcher extends HttpServlet implements DustNetConsts, DustStreamConsts {
	private static final long serialVersionUID = 1L;

	DustHandle hCfg;
	Collection<DustHandle> agents;

	public DustHttpServletDispatcher(Object dispatch) {
		super();
		hCfg = (DustHandle) dispatch;
		agents = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, dispatch, TOKEN_MEMBERS);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long ts = System.currentTimeMillis();
		Throwable exception = null;
		String url = request.getPathInfo();

		if (url.startsWith("/")) {
			url = url.substring(1);
		}

		String pathInfo = url;

		try {
			DustHandle hRelay = null;

			for (DustHandle agent : agents) {
				String p = Dust.access(DustAccess.Get, "@@@", agent, TOKEN_PREFIX);

				if (pathInfo.startsWith(p)) {
					hRelay = agent;
					pathInfo = pathInfo.substring(p.length());
					if (pathInfo.startsWith("/")) {
						pathInfo = pathInfo.substring(1);
					}
					break;
				}
			}

			if (null == hRelay) {
				response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
				return;
			}

//			DustHandle hRelay = Dust.access(DustAccess.Peek, null, hCfg, TOKEN_TARGET);

			synchronized (hRelay) { // quick and dirty

//				Dust.access(DustAccess.Reset, null, hRelay, TOKEN_LISTENERS);
//				
//				DustHandle ao = Dust.getAgentHandle(target.get(TOKEN_AGENT));
//				Dust.access(DustAccess.Insert, ao, hRelay, TOKEN_LISTENERS, KEY_ADD);
//				
				Map params = new HashMap();

				Map<String, Map<String, Object>> mapping = Dust.access(DustAccess.Peek, null, hRelay, TOKEN_MAPPING);

				if (null != mapping) {
					for (Map.Entry<String, Map<String, Object>> mfe : mapping.entrySet()) {
						String field = mfe.getKey();
						Map<String, Object> def = mfe.getValue();
						Object val = null;

						switch ((String) def.get(TOKEN_SOURCE)) {
						case TOKEN_NET_SRVCALL_PATHINFO:
							val = pathInfo;
							break;
						}
						
						switch ((String) def.get(TOKEN_CMD)) {
						case "split":
							String sep = (String) def.get(TOKEN_SEPARATOR);
							int idx = ((Number) def.get(TOKEN_INDEX)).intValue();
							String str = (String)val;
							val = DustUtils.isEmpty(str) ? def.get(TOKEN_DEFAULT) : DustUtils.optGet(str.split(sep), idx, def.get(TOKEN_DEFAULT));
							break;
						}
						
						params.put(field, val);
					}
					
//					Dust.access(DustAccess.Set, response, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);
					Dust.access(DustAccess.Set, response, hRelay, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

				} else {
					Dust.access(DustAccess.Set, url, params, TOKEN_TARGET, TOKEN_STREAM_URL);
					Dust.access(DustAccess.Set, pathInfo, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

					Dust.access(DustAccess.Set, request, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_REQUEST);
					Dust.access(DustAccess.Set, response, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

					Dust.access(DustAccess.Set, request.getMethod(), params, TOKEN_TARGET, TOKEN_NET_SRVCALL_METHOD);

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
					if (null == cmd) {
						cmd = DustUtils.getPrefix(pathInfo, "/");
						params.put(TOKEN_CMD, cmd);
					}
				}
				Dust.access(DustAccess.Process, params, hRelay);

				int status = Dust.access(DustAccess.Peek, HttpServletResponse.SC_OK, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_STATUS);

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
		Dust.access(DustAccess.Set, val, params, TOKEN_TARGET, kind, name);
	}
}