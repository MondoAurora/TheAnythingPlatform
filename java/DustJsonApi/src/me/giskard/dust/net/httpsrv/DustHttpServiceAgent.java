package me.giskard.dust.net.httpsrv;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.mind.DustMindUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsConstsJson;

//@SuppressWarnings({ "rawtypes", "unchecked" })
//@SuppressWarnings({ "unchecked" })
public class DustHttpServiceAgent extends DustAgent implements DustNetConsts, DustStreamConsts, DustUtilsConstsJson {
//	String infoType;

	@Override
	protected Object process(DustAccess access) throws Exception {
//
//		String accept = Dust.access(DustAccess.Peek, "", null, TOKEN_TARGET, TOKEN_NET_SRVCALL_HEADERS, "Accept");
//		boolean isHtml = accept.toLowerCase().contains("html");

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String pi = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

			String[] path = pi.split("/");
			int pl = path.length;

			if ((1 == pl) && DustUtils.isEmpty(path[0].trim())) {
				pl = 0;
			}

			String srcName = Dust.access(DustAccess.Peek, "", null, TOKEN_SOURCE);
			DustHandle source = Dust.getUnit(srcName, true);

			DustHandle unit = null;
			StringBuilder sb = null;

			if (0 == pl) {
//				String m = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_METHOD);

				sb = new StringBuilder(
						"<!doctype html>\n" + "<html lang=\"en\">\n" + "<head>\n<meta charset=\"utf-8\">\n<title>Service list</title>\n</head>\n" + "<body>");

				sb.append("<h2>Services</h2><ul>");

				String url = Dust.access(DustAccess.Peek, "", null, TOKEN_TARGET, TOKEN_STREAM_URL);

				for (DustHandle h : DustMindUtils.getUnitMembers(source)) {
					DustHandle t = h.getType();
					if (DustUtils.isEqual(TOKEN_TYPE_SERVICE, t.getId())) {
						String i = h.getId();
						Object params = Dust.access(DustAccess.Peek, null, h, TOKEN_PAYLOAD);

						String c = (null == params) ? "exec/" : "show/";
						String e = (null == params) ? " [!!!]" : "";

						sb.append("\n<li><a href=\"/" + url + "/" + c + i + "\">" + DustUtils.getPostfix(i, DUST_SEP_TOKEN) + e + "</a></li>");
					}
				}

				sb.append("\n</ul>");
				sb.append("\n</body>\n</html>");

			} else {
				try {
					String cmd = path[0];
					DustHandle svc = Dust.getHandle(source, null, path[1], DustOptCreate.None);
					if (null == svc) {
						return TOKEN_RESULT_REJECT;
					}

					switch (cmd) {
					case "exec":
						Map<String, String> params = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, TOKEN_TARGET, TOKEN_PAYLOAD);
						Dust.access(DustAccess.Process, params, svc);
						break;
					case "show":
						Map<String, String> input = Dust.access(DustAccess.Peek, null, svc, TOKEN_PAYLOAD);
						
						String i = DustUtils.getPostfix(svc.getId(), DUST_SEP_TOKEN);

						sb = new StringBuilder(
								"<!doctype html>\n" + "<html lang=\"en\">\n" + "<head>\n<meta charset=\"utf-8\">\n<title>" + i + "</title>\n</head>\n" + "<body>");

						sb.append("<h2>" + i + " parameters</h2><form action=\"/svc/exec/" + svc.getId() + "\" method=\"post\">");

						for (Map.Entry<String, String> ei : input.entrySet()) {
							String key = ei.getKey();
							String id = ei.getValue();
							sb.append("\n<label for=\"" + id + "\">" + key + "</label><br> <input type=\"text\" id=\"" + id + "\" name=\"" + id + "\"><br>");
						}

						sb.append("\n<input type=\"submit\" value=\"Execute!\">\n</form>");
						sb.append("\n</body>\n</html>");
						break;
					}
				} finally {
					Dust.releaseUnit(unit);
				}
			}

			if (null != sb) {
				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(sb.toString());

			}
		}

		return TOKEN_RESULT_ACCEPT;
	}

}
