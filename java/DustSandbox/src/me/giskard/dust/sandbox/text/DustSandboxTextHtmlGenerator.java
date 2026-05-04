package me.giskard.dust.sandbox.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked" })
public class DustSandboxTextHtmlGenerator implements DustSandboxTextConsts {

	String placeholder = "<span name=\"placeholder\">&lt;type statement here&gt;</span>";
	String noLayout = "<span name=\"placeholder\">&lt;no layout found&gt;</span>";
	Stack<Integer> headStack = new Stack<Integer>();
	int lp = 0;

	String generateHtml(DustSandboxTextAgent txtAgent) {
		StringBuilder sb = new StringBuilder();
		headStack.clear();

		sb.append("<html><head>\n");
		sb.append("<style>");

		for (Map.Entry<String, String> de : txtAgent.styles.entrySet()) {
			sb.append("\n  ").append(de.getKey()).append(" { ").append(de.getValue()).append("}");
		}
//		sb.append("\n  div { padding-top: 5px; }");
//		sb.append("\n  div.test { background-color: beige; }");

		sb.append("\n</style>");

		sb.append("</head><body>\n");

//		sb.append("<img src=\"save/temp.jpg\" class=\"tap_sideimage\"/>\n");

		appendHandle(txtAgent, txtAgent.hDoc, sb, null);

		sb.append("</body>");

		sb.append("</html>");

		return sb.toString();
	};

	void appendHandle(DustSandboxTextAgent txtAgent, DustHandle h, StringBuilder sb, String specGroup) {
		String id = h.getId();
		String ht = h.getType().getId();
		Collection<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS);
		boolean empty = members.isEmpty();
		String container = "div";

		DustHandle hLayoutParent = null;

		if (DustUtils.isEqual(TOKEN_STREAM_REF, ht)) {
			String path = Dust.access(DustAccess.Peek, null, h, TOKEN_TARGET, TOKEN_PATH);

			if (!DustUtils.isEmpty(path)) {
				DustUtils.sbAppend(sb, "", false, "<img src=\"", path, "\" ", "id=\"" + id + "\" ");
				optAddStyleClass(h, sb);
				sb.append("/>\n");
			}

			return;
		}

		if (DustUtils.isEqual(TOKEN_LAYOUT_RESPONSIVE, ht)) {
			Collection<DustHandle> options = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_OPTIONS);
			for (DustHandle ho : options) {
				DustHandle hl = Dust.access(DustAccess.Peek, txtAgent.hLayout, ho, TOKEN_LAYOUT_LAYOUT);
				if (txtAgent.hLayout == hl) {
					hLayoutParent = h;
					h = ho;
					ht = h.getType().getId();
					members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS);
					break;
				}
			}

			if (null == hLayoutParent) {
				return;
			}
		}

		if (DustUtils.isEqual(TOKEN_LAYOUT_TABLE, ht)) {
			container = "table";
			DustUtilsFactory<Long, Map<Long, String>> factTableRows = new DustUtilsFactory.Simple<Long, Map<Long, String>>(true,
					(Class<? extends Map<Long, String>>) TreeMap.class);

			DustUtils.sbAppend(sb, "", false, "<", container, " id=\"", id, "\"", "style=\"padding-left:", lp, "px\" >\n");

			for (DustHandle hc : members) {
				ArrayList<Long> pos = Dust.access(DustAccess.Peek, null, hc, TOKEN_POSITION);
				StringBuilder sbc = new StringBuilder();
				DustHandle targetTxt = Dust.access(DustAccess.Peek, null, hc, TOKEN_TARGET);
				appendHandle(txtAgent, targetTxt, sbc, null);
				factTableRows.get(pos.get(0)).put(pos.get(1), sbc.toString());
			}

// will be needed for missing / spanned cells
//			ArrayList<Long> tblSpan = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_SPAN);

			ArrayList<Long> tblDataOffset = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_POSITION);

			Long headOffsetRow = tblDataOffset.get(0);
			Long headOffsetCol = tblDataOffset.get(1);

			if (0 < headOffsetRow) {
				sb.append("<thead>\n");
			} else {
				sb.append("<tbody>\n");
			}

			for (Long r : factTableRows.keys()) {
				sb.append("<tr>\n");

				for (Map.Entry<Long, String> ce : factTableRows.peek(r).entrySet()) {
					String cc = (r <= headOffsetRow) || (ce.getKey() <= headOffsetCol) ? "th" : "td";
					DustUtils.sbAppend(sb, "", false, "<", cc, "> ", ce.getValue(), "</", cc, ">\n");
				}

				sb.append("</tr>\n");

				if (r == headOffsetRow) {
					sb.append("</thead>\n<tbody>\n");
				}
			}
			sb.append("</tbody>\n");
		} else {
			int depth = headStack.size();
			boolean hdr = !empty && (0 < depth);
			int mi = 0;

			String hGroup = Dust.access(DustAccess.Peek, null, h, TOKEN_TEXT_GROUP, TOKEN_ID);

			if (null != hGroup) {
				hdr = false;
			}

			if (DustUtils.isEqual(TOKEN_TEXT_GROUP_INLINE, specGroup)) {

//			if (editor.hTagInline == specGroup) {
				container = "span";
			}

			String txt = txtAgent.accessText(DustAccess.Peek, null, id);
			if (DustUtils.isEmpty(txt) || Character.isLetterOrDigit(txt.charAt(0))) {
				sb.append("\n");
			}

			DustUtils.sbAppend(sb, "", false, "<", container, " id=\"", id, "\"");

			if ("div".equals(container)) {
				DustUtils.sbAppend(sb, "", false, " style=\"padding-left:", lp, "px\" ");
			}

			optAddStyleClass(h, sb);

			sb.append(">");

			if (null != txt) {
				if (hdr) {
					StringBuilder headNum = null;
					for (Integer i : headStack) {
						if (null == headNum) {
							headNum = new StringBuilder(i.toString());
						} else {
							headNum.append(".").append(i);
						}
					}
					DustUtils.sbAppend(sb, "", false, "<span class=\"tap_head", depth, "\"> ", headNum, " ");
				} else if (null != specGroup) {
					if (DustUtils.isEqual(TOKEN_TEXT_GROUP_BULLET, specGroup)) {
						sb.append(" - ");
					} else if (DustUtils.isEqual(TOKEN_TEXT_GROUP_NUMBER, specGroup)) {
						DustUtils.sbAppend(sb, "", false, " ", headStack.peek(), ". ");
					}
				}
				sb.append(DustStreamUtils.escapeHTML(txt));
				if (hdr) {
					DustUtils.sbAppend(sb, "", false, "</span>");
				}

				empty = false;
			}

			for (DustHandle hc : members) {
				int cc = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, hc, TOKEN_MEMBERS, KEY_SIZE);
				if ((0 < cc) || (null != hGroup)) {
					try {
						++mi;
						headStack.push(mi);
						if (null != hGroup) {
							lp += 5;
						}
						appendHandle(txtAgent, hc, sb, hGroup);
					} finally {
						if (null != hGroup) {
							lp -= 5;
						}
						headStack.pop();
					}
				} else {
					appendHandle(txtAgent, hc, sb, hGroup);
				}
				empty = false;
			}

			if (empty) {
				sb.append(placeholder);
			}
		}

		DustUtils.sbAppend(sb, "", false, "</", container, ">");
	}

	private void optAddStyleClass(DustHandle h, StringBuilder sb) {
		Collection<DustHandle> styles = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, h, TOKEN_TEXT_STYLES);

		if (!styles.isEmpty()) {
			StringBuilder sc = null;
			for (DustHandle hs : styles) {
				String name = (String) Dust.access(DustAccess.Peek, "", hs, TOKEN_NAME);
				name = DustUtils.getPostfix(name, ".");
				sc = DustUtils.sbAppend(sc, " ", false, name);
//					sb.append(name);
//					sb.append(" ");
			}

			sb.append(" class=\"").append(sc).append("\" ");
		}
	};

}
