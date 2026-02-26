package me.giskard.dust.mod.net.httpsrv;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConstsJson;
import me.giskard.dust.mod.mvel.DustExprMvelUtils;
import me.giskard.dust.mod.net.DustNetConsts;

//@SuppressWarnings({ "rawtypes", "unchecked" })
//@SuppressWarnings({ "unchecked" })
public class DustHttpJsonapiAgent extends DustAgent implements DustNetConsts, DustStreamConsts, DustUtilsConstsJson {
//	String infoType;

	@Override
	protected Object process(DustAccess access) throws Exception {

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_RESPONSE);

		if (null != response) {
			String pi = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET, TOKEN_NET_SRVCALL_PATHINFO);

			String[] path = pi.split("/");
			int pl = path.length;

//			infoType = Dust.getMetaMeta(TOKEN_INFO);

			DustHandle unit = null;

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
				try {
					Pattern ptFilter = Pattern.compile("fields\\[(.*)\\]");
					Map<String, String> params = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, TOKEN_TARGET, TOKEN_PAYLOAD);
					Map<String, String[]> atts = new TreeMap<String, String[]>();

					String cmd = path[0];

					switch (cmd) {
					case "unit":
						DustHandle source = Dust.getUnit(path[1], true);
						String type = (pl > 2) ? path[2] : null;
						String defMeta = (null == type) ? null : DustUtils.getPrefix(type, DUST_SEP_TOKEN);

						JsonApiFilter filter = null;

						for (Map.Entry<String, String> pe : params.entrySet()) {
							String pk = pe.getKey();

							if ("filter".equals(pk)) {
								filter = new JsonApiFilter(pe.getValue());
							} else {
								Matcher matcher = ptFilter.matcher(pk);
								if (matcher.matches()) {
									String tn = matcher.group(1);
									if (-1 == tn.indexOf(DUST_SEP_TOKEN)) {
										tn = defMeta + DUST_SEP_TOKEN + tn;
									}

									String[] attList = pe.getValue().split(",");

									if (0 == attList.length) {
										attList = null;
									} else {
										for (int i = attList.length; i-- > 0;) {
											String a = attList[i].trim();
											if (-1 == a.indexOf(DUST_SEP_TOKEN)) {
												a = defMeta + DUST_SEP_TOKEN + a;
											}
											attList[i] = a;
										}
									}
									atts.put(tn, attList);
								}
							}
						}

						unit = Dust.getUnit(null, true);

						if (pl > 3) {
							StringBuilder sb = null;
							for (int i = 3; i < pl; ++i) {
								sb = DustUtils.sbAppend(sb, "/", true, path[i]);
							}
							String id = sb.toString();
							DustHandle h = Dust.getHandle(source, null, id.toString(), DustOptCreate.None);
							if (null != h) {
								cloneData(unit, h, atts);
							}
						} else {
							for (DustHandle h : DustMindUtils.getUnitMembers(source)) {
								if ((null == type) || DustUtils.isEqual(type, h.getType().getId())) {

									if (null != filter) {
										filter.setHandle(h);
										Boolean eval = DustExprMvelUtils.eval(filter.getCondition(), filter, filter.getValues(), false);
										if (!eval) {
											continue;
										}
									}

									cloneData(unit, h, atts);
								}
							}
						}
						break;
					}

					if (null != unit) {
						response.setCharacterEncoding(DUST_CHARSET_UTF8);
						response.setContentType(MEDIATYPE_JSONAPI);
						PrintWriter out = response.getWriter();

						Object ser = Dust.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);

						Map<String, Object> ps = new HashMap<>();
						ps.put(TOKEN_CMD, TOKEN_CMD_SAVE);
						ps.put(TOKEN_DATA, unit);
						ps.put(TOKEN_STREAM_WRITER, out);

						Dust.access(DustAccess.Process, ps, ser);

						out.flush();

						DustDevUtils.memInfo();
					}
				} finally {
					Dust.releaseUnit(unit);
				}
			}
		}

		return TOKEN_RESULT_ACCEPT;
	}

	private static final String[] NOFILTER = new String[] {};

	private void cloneData(DustHandle unit, DustHandle h, Map<String, String[]> atts) {
		DustHandle ot = h.getType();
		DustHandle to = Dust.getHandle(unit, ot, h.getId(), DustOptCreate.Primary);
		String[] a = atts.getOrDefault(ot.getId(), NOFILTER);
		DustMindUtils.loadData(to, h, false, a);
	}
}
