package me.giskard.dust.net.httpsrv;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.mind.DustMindUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;

//@SuppressWarnings({ "rawtypes", "unchecked" })
//@SuppressWarnings({ "unchecked" })
public class DustHttpJsonapiAgent extends DustAgent implements DustNetConsts, DustStreamConsts {
//	String infoType;

	@Override
	protected Object process(DustAccess access) throws Exception {

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String pi = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

			String[] path = pi.split("/");
			int pl = path.length;

//			infoType = Dust.getMetaMeta(TOKEN_INFO);

			DustObject unit = null;

			if (0 == pl) {
				String m = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_METHOD);

				StringBuilder sb = new StringBuilder(
						"<!doctype html>\n" + "<html lang=\"en\">\n" + "<head>\n<meta charset=\"utf-8\">\n<title>DustTracer JSON:API</title>\n</head>\n" + "<body>");

				sb.append("<h2>JSONAPI</h2>");

				sb.append("<p>Method: " + m + "</p>");
				sb.append("<p>PathInfo: " + pi + "</p>");

				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(sb.toString());
			} else {

				String cmd = path[0];

				switch (cmd) {
				case "unit":
					DustObject source = Dust.getUnit(path[1], true);
					String type = (pl > 2) ? path[2] : null;

					unit = Dust.getUnit(null, true);

					if (pl > 3) {
						StringBuilder sb = null;
						for (int i = 3; i < pl; ++i) {
							sb = DustUtils.sbAppend(sb, "/", true, path[i]);
						}
						String id = sb.toString();
						DustObject o = Dust.getObject(source, null, id.toString(), DustOptCreate.None);
						if (null != o) {
							cloneObj(unit, o);
						}
					} else {
						for (DustObject o : DustMindUtils.getUnitMembers(source) ) {
							if ((null == type) || DustUtils.isEqual(type, o.getType().getId())) {
								cloneObj(unit, o);
							}
						}
					}
					break;
				}
			}

			if (null != unit) {
				response.setCharacterEncoding(DUST_CHARSET_UTF8);
				response.setContentType(MEDIATYPE_JSONAPI);
				PrintWriter out = response.getWriter();

				Object ser = Dust.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);
				
				Map<String, Object> params = new HashMap<>();
				params.put(TOKEN_CMD, TOKEN_CMD_SAVE);
				params.put(TOKEN_DATA, unit);
				params.put(TOKEN_STREAM_WRITER, out);
				
				Dust.access(DustAccess.Process, params, ser);

				out.flush();
			}
		}

		return TOKEN_RESULT_ACCEPT;
	}

	public void cloneObj(DustObject unit, DustObject o) {
		DustObject to = Dust.getObject(unit, o.getType(), o.getId(), DustOptCreate.Primary);
		DustMindUtils.loadObject(to, o, false);

//		MindObject info = Dust.getObject(unit, infoType, unit.getUnitId(), MindOptCreate.None);
//
//		if (null != info) {
//			for (String a : (Iterable<String>) Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, to, KEY_MAP_KEYS)) {
//				Long c = Dust.access(DustAccess.Peek, 0L, info, a, TOKEN_COUNT);
//				Dust.access(DustAccess.Set, c + 1, info, a, TOKEN_COUNT);
//			}
//		}
	}
}
