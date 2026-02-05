package me.giskard.dust.net.httpsrv;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.mind.DustMindUtils;
import me.giskard.dust.mvel.DustExprMvelUtils;
import me.giskard.dust.net.DustNetConsts;
import me.giskard.dust.stream.DustStreamConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsConstsJson;

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
				try {
					Pattern ptFilter = Pattern.compile("fields\\[(.*)\\]");
					Map<String, String> params = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, TOKEN_TARGET, TOKEN_PAYLOAD);
					Map<String, String[]> atts = new TreeMap<String, String[]>();

					String cmd = path[0];

					switch (cmd) {
					case "unit":
						DustObject source = Dust.getUnit(path[1], true);
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
							DustObject o = Dust.getObject(source, null, id.toString(), DustOptCreate.None);
							if (null != o) {
								cloneObj(unit, o, atts);
							}
						} else {
							for (DustObject o : DustMindUtils.getUnitMembers(source)) {
								if ((null == type) || DustUtils.isEqual(type, o.getType().getId())) {

									if (null != filter) {
										filter.setObject(o);
										Boolean eval = DustExprMvelUtils.eval(filter.getCondition(), filter, filter.getValues(), false);
										if (!eval) {
											continue;
										}
									}

									cloneObj(unit, o, atts);
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

	public void cloneObj(DustObject unit, DustObject o, Map<String, String[]> atts) {
		DustObject ot = o.getType();
		DustObject to = Dust.getObject(unit, ot, o.getId(), DustOptCreate.Primary);
		String[] a = atts.getOrDefault(ot.getId(), NOFILTER);
		DustMindUtils.loadObject(to, o, false, a);

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
