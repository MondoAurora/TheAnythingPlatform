package me.giskard.dust.net.httpsrv;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;

//@SuppressWarnings({ "rawtypes", "unchecked" })
@SuppressWarnings({  "unchecked" })
public class DustHttpJsonapiAgent extends DustConsts.DustAgentBase implements DustNetConsts, DustStreamConsts, DustKBConsts {
	String infoType;

	@Override
	protected Object process(Map<String, Object> cfg, Object params) throws Exception {

		HttpServletResponse response = DustKBUtils.access(KBAccess.Peek, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String pi = DustKBUtils.access(KBAccess.Get, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

			String[] path = pi.split("/");
			int pl = path.length;

			KBStore kb = Dust.getAgent(DustUtils.simpleGet(cfg, TOKEN_KB_KNOWLEDGEBASE));
			infoType = kb.getMetaTypeId(TOKEN_INFO);

			KBUnit unit = null;

			if (0 == pl) {
				String m = DustKBUtils.access(KBAccess.Get, null, params, TOKEN_TARGET, TOKEN_NET_SRVCALL_METHOD);

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
					KBUnit source = kb.getUnit(path[1], false);
					String type = (pl > 2) ? path[2] : null;

					unit = kb.getUnit(null, true);

					if (pl > 3) {
						StringBuilder sb = null;
						for (int i = 3; i < pl; ++i) {
							sb = DustUtils.sbAppend(sb, "/", true, path[i]);
						}
						String id = sb.toString();
						KBObject o = source.getObject(type, id.toString(), KBOptCreate.None);
						if (null != o) {
							cloneObj(unit, o);
						}
					} else {
						for (KBObject o : source.objects()) {
							if ((null == type) || DustUtils.isEqual(type, o.getType())) {
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

				Map<String, Object> ser = DustUtils.simpleGet(cfg, TOKEN_SERIALIZER);
				DustKBUtils.access(KBAccess.Set, unit, ser, TOKEN_PARAMS, TOKEN_UNIT);
				DustKBUtils.access(KBAccess.Set, out, ser, TOKEN_PARAMS, TOKEN_STREAM_WRITER);

				Dust.sendMessage(ser);

				out.flush();
			}
		}

		return TOKEN_RESULT_ACCEPT;
	}

	public void cloneObj(KBUnit unit, KBObject o) {
		KBObject to = unit.getObject(o.getType(), o.getId());
		to.load(o, false);

		KBObject info = unit.getObject(infoType, unit.getUnitId(), KBOptCreate.None);

		if (null != info) {
			for (String a : (Iterable<String>) DustKBUtils.access(KBAccess.Peek, Collections.EMPTY_LIST, to, KEY_MAP_KEYS)) {
				Long c = DustKBUtils.access(KBAccess.Peek, 0L, info, a, TOKEN_COUNT);
				DustKBUtils.access(KBAccess.Set, c + 1, info, a, TOKEN_COUNT);
			}
		}
	}
}
