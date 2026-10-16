package me.giskard.dust.mod.net.httpsrv;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConstsJson;

//@SuppressWarnings({ "rawtypes", "unchecked" })
//@SuppressWarnings({ "unchecked" })
public class DustHttpGeneralServiceAgent extends DustAgent
		implements DustNetConsts, DustStreamConsts, DustUtilsConstsJson {

	@Override
	public void process() throws Exception {
		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SRVCALL_RESPONSE);

		DustHandle hCmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

		if (HANDLE_MISC_TAG_CMD_RESPOND.equals(hCmd)) {
			String mt = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_MIMETYPE);
			response.setContentType(mt);
			OutputStream out = response.getOutputStream();

			InputStream is = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_INPUT);
			DustStreamUtils.copyStream(is, out);
			out.close();

			return;
		}

		if (null != response) {
			String pi = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SRVCALL_PATHINFO);

			String[] path = pi.split("/");
			int pl = path.length;

			if ((1 == pl) && DustUtils.isEmpty(path[0].trim())) {
				pl = 0;
			}

			String srcName = Dust.access(DustAccess.Peek, "", null, HANDLE_MISC_ATT_SOURCE);
			DustHandle source = Dust.getHandle(srcName);

			DustHandle unit = null;
			StringBuilder sb = null;

			if (0 == pl) {
//				String m = Dust.access(DustAccess.Peek, null, null, HANDLE_TARGET, HANDLE_NET_ATT_SRVCALL_METHOD);

				sb = new StringBuilder("<!doctype html>\n" + "<html lang=\"en\">\n"
						+ "<head>\n<meta charset=\"utf-8\">\n<title>Service list</title>\n</head>\n" + "<body>");

				sb.append("<h2>Services</h2><ul>");

				String url = Dust.access(DustAccess.Peek, "", null, HANDLE_STREAM_ATT_URL);

				for (DustHandle h : DustMachineUtils.getUnitMembers(source)) {
					DustHandle t = h.getType();
					if (DustUtils.isEqual(HANDLE_MIND_ASP_MESSAGE, t.getId())) {
						String i = h.getId();
						Object params = Dust.access(DustAccess.Peek, null, h, HANDLE_MISC_ATT_PAYLOAD);

						String c = (null == params) ? "exec/" : "show/";
						String e = (null == params) ? " [!!!]" : "";

						sb.append("\n<li><a href=\"/" + url + "/" + c + i + "\">" + DustUtils.getPostfix(i, DUST_SEP_TOKEN) + e
								+ "</a></li>");
					}
				}

				sb.append("\n</ul>");
				sb.append("\n</body>\n</html>");

			} else {
				try {
					String cmd = path[0];
					DustHandle svc = Dust.getHandle(path[1], source);
					if (null == svc) {
						Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_REJECT, null, HANDLE_DUST_ATT_CTX_MSG,
								HANDLE_MIND_ATT_TAGS);

//						return HANDLE_MIND_TAG_RESULT_REJECT;
					}

					switch (cmd) {
					case "exec":
						Map<String, String> params = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PAYLOAD);
						Dust.access(DustAccess.Set, params, svc, HANDLE_MISC_ATT_PAYLOAD);
						Dust.access(DustAccess.Commit, null, svc);
						break;
					case "show":
						Map<String, String> input = Dust.access(DustAccess.Peek, null, svc, HANDLE_MISC_ATT_PAYLOAD);

						String i = DustUtils.getPostfix(svc.getId(), DUST_SEP_TOKEN);

						sb = new StringBuilder("<!doctype html>\n" + "<html lang=\"en\">\n"
								+ "<head>\n<meta charset=\"utf-8\">\n<title>" + i + "</title>\n</head>\n" + "<body>");

						sb.append("<h2>" + i + " parameters</h2><form action=\"/svc/exec/" + svc.getId() + "\" method=\"post\">");

						for (Map.Entry<String, String> ei : input.entrySet()) {
							String key = ei.getKey();
							String id = ei.getValue();
							sb.append("\n<label for=\"" + id + "\">" + key + "</label><br> <input type=\"text\" id=\"" + id
									+ "\" name=\"" + id + "\"><br>");
						}

						sb.append("\n<input type=\"submit\" value=\"Execute!\">\n</form>");
						sb.append("\n</body>\n</html>");
						break;
					}
				} finally {
					Dust.access(DustAccess.Delete, null, unit);
				}
			}

			if (null != sb) {
				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(sb.toString());

			}
		}
		Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_ACCEPT, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_TAGS);

//		return HANDLE_MIND_TAG_RESULT_ACCEPT;
	}

}
