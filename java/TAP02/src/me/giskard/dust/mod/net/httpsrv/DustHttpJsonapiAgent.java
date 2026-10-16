package me.giskard.dust.mod.net.httpsrv;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.stream.DustStreamConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConstsJson;
import me.giskard.dust.mod.mvel.DustExprMvelUtils;

//@SuppressWarnings({ "rawtypes", "unchecked" })
//@SuppressWarnings({ "unchecked" })
public class DustHttpJsonapiAgent extends DustAgent implements DustNetConsts, DustStreamConsts, DustUtilsConstsJson {
//	String infoType;

	@Override
	public void process() throws Exception {

		HttpServletResponse response = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SRVCALL_RESPONSE);

		if ( null != response ) {
			String pi = Dust.access(DustAccess.Peek, "", null, HANDLE_NET_ATT_SRVCALL_PATHINFO);

			if ( DustUtils.isEmpty(pi) ) {
				String m = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SRVCALL_METHOD);

				StringBuilder sb = new StringBuilder("<!doctype html>\n" + "<html lang=\"en\">\n" + "<head>\n<meta charset=\"utf-8\">\n<title>TAP02 General JSON:API interface</title>\n</head>\n" + "<body>");

				sb.append("<h2>JSONAPI</h2>");

				sb.append("<p>Method: " + m + "</p>");
				sb.append("<p>PathInfo: " + pi + "</p>");
				sb.append("</body></html>");

				response.setContentType(MEDIATYPE_UTF8_HTML);
				PrintWriter out = response.getWriter();

				out.println(sb.toString());
			} else {
				String[] path = pi.split("/");
				int pl = path.length;
				DustHandle unit = null;

				try {
					Map<String, String> params = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, HANDLE_MISC_ATT_PAYLOAD);
					Map<DustHandle, Iterable<DustHandle>> atts = new TreeMap<DustHandle, Iterable<DustHandle>>();

					String cmd = path[0];

					switch ( cmd ) {
					case "unit":
						String unitId = path[1] + "/" + path[2];
						DustHandle source = Dust.getHandle(unitId, HANDLE_MIND_ASP_UNIT);
						String type = (pl > 3) ? path[3] : null;

						Pattern ptFilter = null;
						JsonApiFilter filter = null;

						for (Map.Entry<String, String> pe : params.entrySet()) {
							String pk = pe.getKey();

							if ( "filter".equals(pk) ) {
								filter = new JsonApiFilter(pe.getValue());
							} else {
								if ( null == ptFilter ) {
									ptFilter = Pattern.compile("fields\\[(.*)\\]");
								}
								Matcher matcher = ptFilter.matcher(pk);
								if ( matcher.matches() ) {
									String tn = matcher.group(1);

									DustHandle ht = Dust.getHandle(tn);

									if ( null == ht ) {
										Dust.log(HANDLE_MISC_TAG_LEVEL_ERROR, "undefined type in filter", tn);
									} else {

										String[] attList = pe.getValue().split(",");

										if ( 0 != attList.length ) {
											Set<DustHandle> ta = new TreeSet<DustHandle>();

											for (int i = attList.length; i-- > 0;) {
												String a = attList[i].trim();
												DustHandle ha = Dust.getHandle(a);
												if ( null != ha ) {
													ta.add(ha);
												}
											}
											atts.put(ht, ta);
										}
									}
								}
							}
						}

						unit = Dust.getHandle(null, HANDLE_MIND_ASP_UNIT);

						if ( pl > 3 ) {
							StringBuilder sb = null;
							for (int i = 3; i < pl; ++i) {
								sb = DustUtils.sbAppend(sb, "/", true, path[i]);
							}
							String id = sb.toString();
							DustHandle h = Dust.getHandle(id.toString(), source);
							if ( null != h ) {
								cloneData(unit, h, atts);
							}
						} else {
							for (DustHandle h : DustMachineUtils.getUnitMembers(source)) {
								if ( (null == type) || DustUtils.isEqual(type, h.getType().getId()) ) {

									if ( null != filter ) {
										filter.setHandle(h);
										Boolean eval = DustExprMvelUtils.eval(filter.getCondition(), filter, filter.getValues(), false);
										if ( !eval ) {
											continue;
										}
									}

									cloneData(unit, h, atts);
								}
							}
						}
						break;
					}

					if ( null != unit ) {
						response.setCharacterEncoding(DUST_CHARSET_UTF8);
						response.setContentType(MEDIATYPE_JSONAPI);
						PrintWriter out = response.getWriter();

						Object ser = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_SERIALIZER);

						Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_SAVE, ser, HANDLE_MIND_ATT_CMD);
						Dust.access(DustAccess.Set, unit, ser, HANDLE_MISC_ATT_TARGET);
						Dust.access(DustAccess.Set, out, ser, HANDLE_STREAM_ATT_WRITER);

						Dust.access(DustAccess.Commit, null, ser);

						out.flush();

						DustDevUtils.memInfo();
					}
				} finally {
					Dust.access(DustAccess.Delete, null, unit);
				}
			}
		}
		Dust.access(DustAccess.Insert, HANDLE_MIND_TAG_RESULT_ACCEPT, null, HANDLE_DUST_ATT_CTX_MSG, HANDLE_MIND_ATT_TAGS);

//		return HANDLE_MIND_TAG_RESULT_ACCEPT;
	}

	private void cloneData(DustHandle unit, DustHandle h, Map<DustHandle, Iterable<DustHandle>> atts) {
		Dust.access(DustAccess.Set, h, unit, HANDLE_DUST_ATT_UNIT_HANDLES, h.getId());
//		DustHandle ot = h.getType();
//		DustHandle to = Dust.getHandle(h.getId(), unit);
//		DustMachineUtils.loadData(to, h, atts.get(ot));
	}
}