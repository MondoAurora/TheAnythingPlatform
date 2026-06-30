package me.giskard.dust.sandbox.text;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.stream.DustStreamConsts.StreamProcessor;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.sandbox.db.DustSandboxSQLAgent;

@SuppressWarnings("unchecked")
public class DustSandboxTextAgent extends DustAgent implements DustSandboxTextConsts {

	DustHandle defaultSerializer;

	String unitId;
	DustHandle hUnit;
	DustHandle hRes;
	DustHandle hStrings;

	Set<DustHandle> toSave = new HashSet<DustHandle>();

	String resPath;
	File resRoot;

	DustHandle hDoc;
	Map<String, String> styles = new TreeMap<>();
	Map<DustHandle, DustHandle> events = new HashMap<>();
	Map<DustHandle, Set<DustHandle>> streamRefs = new HashMap<>();
	DustSandboxSQLAgent sqla;

	DustHandle hLayout;

	DustHandle hLang;
	Map<DustHandle, Map<DustHandle, DustHandle>> mapStrings = new HashMap<>();

	File docPath;
	URL docUrl;

	public DustSandboxTextAgent() throws Exception {
		init();
	}

	@Override
	protected void init() throws Exception {
		docPath = new File("localStore");
		docUrl = docPath.toURI().toURL();

		resPath = "res";
		resRoot = new File(docPath, resPath);

		hRes = Dust.getUnit("streams.1", true);
	}

	public void load(String unitId, DustHandle hLayout, DustHandle hLang) {
		this.unitId = unitId;

		hUnit = Dust.getUnit(unitId, true);
		hDoc = null;
		styles.clear();
		events.clear();
		streamRefs.clear();

		Set<DustHandle> tc = new HashSet<>();

		for (DustHandle h : DustMindUtils.getUnitMembers(hUnit)) {
			String ht = h.getType().getId();

			switch (ht) {
			case TOKEN_TEXT_DOC:
				hDoc = h;
				setLayout(Dust.access(DustAccess.Peek, hLayout, h, TOKEN_LAYOUT_LAYOUT));
				setLang(Dust.access(DustAccess.Peek, hLang, h, TOKEN_TEXT_LANG));
				break;
			case TOKEN_TEXT_STYLE:
				updateStyleDef(h);
				break;
			case TOKEN_EVENT:
				events.put(Dust.access(DustAccess.Peek, null, h, TOKEN_TARGET), h);
				break;
			case TOKEN_TEXT_TRANSCLUSION:
				tc.add(h);
				if (null != Dust.access(DustAccess.Peek, null, h, TOKEN_TARGET)) {
					Dust.access(DustAccess.Insert, h, h, TOKEN_TARGET, TOKEN_APPEARS);
					Dust.access(DustAccess.Delete, h, h, TOKEN_TARGET);
					toSave.add(hRes);
				}
				break;
			}
		}

		if (!tc.isEmpty()) {
			for (DustHandle hr : DustMindUtils.getUnitMembers(hRes)) {
				for (DustHandle tr : (Collection<DustHandle>) Dust.access(DustAccess.Peek, Collections.EMPTY_SET, hr, TOKEN_APPEARS)) {
					if (tc.contains(tr)) {
						Dust.access(DustAccess.Insert, hr, streamRefs, tr);
					}
				}
			}
		}

		if (null == hDoc) {
			hDoc = Dust.getHandle(hUnit, TOKEN_TEXT_DOC, null, DustOptCreate.Primary);
		}

		StringBuilder stringUnit = new StringBuilder(DustUtils.getPrefix((String) unitId, DUST_SEP_TOKEN));
		int i = stringUnit.indexOf(".");
		stringUnit.insert(i, "_str");

		hStrings = Dust.getUnit(stringUnit.toString(), true);

		for (DustHandle hr : DustMindUtils.getUnitMembers(hStrings)) {
			DustHandle hl = Dust.access(DustAccess.Peek, null, hr, TOKEN_TEXT_LANG);
			DustHandle ht = Dust.access(DustAccess.Peek, null, hr, TOKEN_TARGET);
			Dust.access(DustAccess.Set, hr, mapStrings, hl, ht);
		}

	}

	public void updateStyleDef(DustHandle hStyle) {
		String name = Dust.access(DustAccess.Peek, "", hStyle, TOKEN_NAME);
		Map<String, String> def = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, hStyle, TOKEN_TEXT_STYLE_DEF);

		if (!DustUtils.isEmpty(name) && !def.isEmpty()) {
			StringBuilder sb = null;
			for (Map.Entry<String, String> de : def.entrySet()) {
				sb = DustUtils.sbAppend(sb, "", false, de.getKey(), ": ", de.getValue(), "; ");
			}
			styles.put(name, sb.toString());
		}
	};

	public void save() {
		Map<String, Object> sp = new HashMap<String, Object>();
		sp.put(TOKEN_CMD, TOKEN_CMD_SAVE);

		if (null == defaultSerializer) {
			DustHandle app = Dust.getUnit("sandbox.1", false);
			DustHandle mind = Dust.getHandle(app, null, TOKEN_MIND, DustOptCreate.None);
			defaultSerializer = Dust.access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);
		}

		toSave.add(hUnit);
		toSave.add(hStrings);
		for (DustHandle h : toSave) {
			sp.put(TOKEN_KEY, h.getId());
			sp.put(TOKEN_DATA, h);
			Dust.access(DustAccess.Process, sp, defaultSerializer);
		}

		toSave.clear();
	};

	public DustHandle insertNode(DustHandle hParent, int idx, String type) {
		DustHandle hRet = Dust.getHandle(hUnit, type, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Insert, hRet, hParent, TOKEN_MEMBERS, idx + 1);
		return hRet;
	}

	public DustHandle getNode(String id) {
		return Dust.getHandle(hUnit, null, id, DustOptCreate.None);
	}

	public void setLayout(DustHandle hLayout) {
		this.hLayout = hLayout;
	}

	public void setLang(DustHandle hLang) {
		this.hLang = hLang;

	}

	public URL getResUrl() {
		return docUrl;
	}

	String accessText(DustAccess access, String val, DustHandle hTxt) {
		return accessTextExt(access, hLang, val, hTxt);
//		DustHandle hNode = Dust.getHandle(hUnit, null, key, DustOptCreate.None);
//		return Dust.access(access, val, hNode, TOKEN_TEXT_TEXT);
	};

	String accessTextExt(DustAccess access, DustHandle hLang, String val, DustHandle hTxt) {
		DustHandle hString = Dust.access(DustAccess.Peek, null, mapStrings, hLang, hTxt);

		if (null == hString) {
			if (access.creator) {
				hString = Dust.getHandle(hStrings, TOKEN_TEXT_STRING, null, DustOptCreate.Primary);
				Dust.access(DustAccess.Set, hTxt, hString, TOKEN_TARGET);
				Dust.access(DustAccess.Set, hLang, hString, TOKEN_TEXT_LANG);
				Dust.access(DustAccess.Set, hString, mapStrings, hLang, hTxt);
			}
		}

		return Dust.access(access, val, hString, TOKEN_TEXT_TEXT);
	};

	public String getShortText(DustHandle h, int maxLen) {
		String ht = h.getType().getId();
		int c = Dust.access(DustAccess.Peek, 0, h, TOKEN_MEMBERS, KEY_SIZE);
		String ph = (0 < c) ? "Inline group" : "placeholder";
		String txt = accessText(DustAccess.Peek, DustUtils.isEqual(TOKEN_TEXT_BLOCK, ht) ? ph : ht, h);
		if (txt.length() > maxLen) {
			txt = txt.substring(0, maxLen - 3) + "...";
		} else {
			txt = txt.trim();
		}
		return txt;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void reset() {
		Dust.access(DustAccess.Reset, null, hDoc, TOKEN_MEMBERS);
	}

	public void delete(DustHandle parent, Collection<DustHandle> selected) {
		for (DustHandle node : selected) {
			Dust.access(DustAccess.Delete, node, parent, TOKEN_MEMBERS, KEY_MEMBEROF);
			Dust.access(DustAccess.Insert, node, parent, TOKEN_TEXT_ORPHANS);
		}
	}

	public void underFirst(DustHandle parent, Collection<DustHandle> selected) {
		DustHandle np = null;

		for (DustHandle node : selected) {
			if (null == np) {
				np = node;
			} else {
				Dust.access(DustAccess.Delete, node, parent, TOKEN_MEMBERS, KEY_MEMBEROF);
				Dust.access(DustAccess.Insert, node, np, TOKEN_MEMBERS, KEY_ADD);
			}
		}
	}

	public void setGroupType(DustHandle hTxt, String grpToken) {
		Dust.access(DustAccess.Set, DustUtils.CONST_HANDLES.get(grpToken, TOKEN_KBMETA_TAG), hTxt, TOKEN_TEXT_GROUP);
	}

	public DustHandle insertImage(DustHandle hParent, DustHandle hThis, Image image) throws Exception {
		int width = image.getWidth(null);
		int height = image.getHeight(null);

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = bi.getGraphics();
		g.drawImage(image, 0, 0, null);

		String imgType = "jpg";

		DustHandle hImg = Dust.getHandle(hRes, TOKEN_STREAM, null, DustOptCreate.Primary);
		String path = resPath + "/" + DustUtils.getPostfix(hImg.getId(), DUST_SEP_TOKEN) + "." + imgType;
		Dust.access(DustAccess.Set, path, hImg, TOKEN_PATH);
		Dust.access(DustAccess.Insert, DustUtils.CONST_HANDLES.get(TOKEN_STREAM_IMAGE, TOKEN_KBMETA_TAG), hImg, TOKEN_TAGS);

		toSave.add(hRes);

		ImageIO.write(bi, imgType, new File(docPath, path));

		DustHandle hRef = Dust.getHandle(hUnit, TOKEN_TEXT_TRANSCLUSION, null, DustOptCreate.Primary);

		Dust.access(DustAccess.Set, hRef, hImg, TOKEN_APPEARS);

		int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		Dust.access(DustAccess.Insert, hRef, hParent, TOKEN_MEMBERS, idx + 1);

		return hImg;
	}

	public void insertLongText(DustHandle hParent, DustHandle hThis, String str) {
		if (null == hParent) {
			hParent = hDoc;
		}

		int len = Dust.access(DustAccess.Peek, 0, hParent, TOKEN_MEMBERS, KEY_SIZE);
		int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		boolean insert = (++idx < len);
		boolean override = DustUtils.isEqual(hThis, hParent) ? false : DustUtils.isEmpty(accessText(DustAccess.Peek, null, hThis));

		String[] lines = str.split("\n");
		StringBuilder sb = new StringBuilder();

		for (String l : lines) {
			l = l.trim();
			if (0 < l.length()) {
				DustUtils.sbAppend(sb, " ", false, l);
				{
					String id;

					if (override) {
						id = hThis.getId();
						override = false;
					} else {
						id = null;
					}
					DustHandle h = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, id, DustOptCreate.Primary);
					accessText(DustAccess.Set, sb.toString(), h);
					sb.setLength(0);

					if (override) {
						override = false;
					} else {
						Dust.access(DustAccess.Insert, h, hParent, TOKEN_MEMBERS, insert ? (idx++) : KEY_ADD);
					}
				}
			}
		}

	}

	public void split(DustHandle hParent, DustHandle hThis, String remain, String move) {
		int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		DustHandle hNew = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Insert, hNew, hParent, TOKEN_MEMBERS, idx + 1);

		accessText(DustAccess.Set, remain, hThis);
		accessText(DustAccess.Set, move, hNew);
	}

	public void insertTable(DustHandle hResp, DustHandle hCurrentLayout, Long dataRows, Long dataCols, Long headRows, Long headCols) {
		Long cols = dataCols + headCols;
		Long rows = dataRows + headRows;

		ArrayList<Long> tblSpan = new ArrayList<>();
		tblSpan.add(rows);
		tblSpan.add(cols);

		ArrayList<Long> tblDataOffset = new ArrayList<>();
		tblDataOffset.add(headRows);
		tblDataOffset.add(headCols);

		DustHandle hTbl = insertNode(hResp, KEY_ADD, TOKEN_LAYOUT_TABLE);
		Dust.access(DustAccess.Insert, hTbl, hResp, TOKEN_OPTIONS);

		Dust.access(DustAccess.Set, hCurrentLayout, hTbl, TOKEN_LAYOUT_LAYOUT);
		Dust.access(DustAccess.Set, tblSpan, hTbl, TOKEN_SPAN);
		Dust.access(DustAccess.Set, tblDataOffset, hTbl, TOKEN_POSITION);

		ArrayList<Long> pos = new ArrayList<>();
		pos.add(0L);
		pos.add(0L);

		for (long c = 1; c <= dataCols; ++c) {
			pos.set(1, c);
			for (long i = 1; i <= rows; ++i) {
				pos.set(0, i);
				addCell(hResp, hTbl, pos, ((i <= headRows) ? "hdrRow " : (c <= headCols) ? "hdrCol " : "Cell ") + pos);
			}
		}
	}

	public void addCell(DustHandle hResp, DustHandle hTbl, ArrayList<Long> pos, String txt) {
		DustHandle hTxt = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, null, DustOptCreate.Primary);
		accessText(DustAccess.Set, txt, hTxt);
		Dust.access(DustAccess.Insert, hTxt, hResp, TOKEN_MEMBERS, KEY_ADD);

		DustHandle hCell = Dust.getHandle(hUnit, TOKEN_LAYOUT_CELL, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Set, new ArrayList<Long>(pos), hCell, TOKEN_POSITION);
		Dust.access(DustAccess.Set, hTxt, hCell, TOKEN_TARGET);

		Dust.access(DustAccess.Insert, hCell, hTbl, TOKEN_MEMBERS, KEY_ADD);
	}

	public void splitInline(DustHandle hParent, DustHandle hThis, String origText, int selBegin, int selEnd) {
		DustHandle hTagInline = DustUtils.CONST_HANDLES.get(TOKEN_TEXT_GROUP_INLINE, TOKEN_KBMETA_TAG);
		boolean inlineNow = Dust.access(DustAccess.Check, hTagInline, hParent, TOKEN_TEXT_GROUP);

		int idx;
		if (!inlineNow) {
			Dust.access(DustAccess.Set, hTagInline, hThis, TOKEN_TEXT_GROUP);
			accessText(DustAccess.Set, null, hThis);
			hParent = hThis;

			hThis = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, null, DustOptCreate.Primary);
			Dust.access(DustAccess.Insert, hThis, hParent, TOKEN_MEMBERS, KEY_ADD);
			idx = 0;
		} else {
			idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
		}

		String before = origText.substring(0, selBegin).trim();
		String txt = origText.substring(selBegin, selEnd).trim();
		String after = origText.substring(selEnd, origText.length() - 1).trim();

		if (!DustUtils.isEmpty(before)) {
			hThis = optAddText(hParent, idx++, hThis, before);
		}
		if (!DustUtils.isEmpty(txt)) {
			hThis = optAddText(hParent, idx++, hThis, txt);
		}
		if (!DustUtils.isEmpty(after)) {
			hThis = optAddText(hParent, idx++, hThis, after);
		}
	}

	private DustHandle optAddText(DustHandle hParent, int idx, DustHandle hTxt, String txt) {
		if (!DustUtils.isEmpty(txt)) {
			if (null == hTxt) {
				hTxt = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, null, DustOptCreate.Primary);
				Dust.access(DustAccess.Insert, hTxt, hParent, TOKEN_MEMBERS, idx);
			}
			accessText(DustAccess.Set, txt, hTxt);
			hTxt = null;
		}

		return hTxt;
	}

	public boolean translate(DustHandle tLan, DustHandle hLang, ArrayList<DustHandle> hSel) throws Exception, IOException, BadLocationException {
		if (hSel.isEmpty()) {
			return false;
		}

		StringBuilder sb = new StringBuilder("<html><body>\n");
		for (DustHandle s : hSel) {
			String id = s.getId();
			String txt = accessText(DustAccess.Peek, "", s);
			if (!DustUtils.isEmpty(txt)) {
				DustUtils.sbAppend(sb, "", false, "<span id=\"", id, "\">", txt, "</span>\n");
			}
		}

		sb.append("\n</body></html>");

		String ttxt = DustSandboxTextUtils.translateLibreLocal(hLang, tLan, "html", sb.toString());

		DustSandboxTextUtils.processTranslated(this, ttxt, tLan, hSel);

		save();

		return true;
	}

	public DustHandle optCreateTextEvent(DustHandle hParent, int idx, String txt, Date dStart, long duration, DustHandle durationUnit) {
		if (DustUtils.isEmpty(txt)) {
			return null;
		}

		Dust.log(TOKEN_LEVEL_TRACE, txt);

		DustHandle hTxt = Dust.getHandle(hUnit, TOKEN_TEXT_BLOCK, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Insert, hTxt, hParent, TOKEN_MEMBERS, idx);
		accessText(DustAccess.Set, txt, hTxt);

		DustHandle hEvt = DustUtilsData.createEvent(hUnit, hTxt, dStart, duration, durationUnit);

		events.put(hTxt, hEvt);

		return hTxt;
	}

	public void manageEvent(EventCommand eCmd, DustHandle hTxt, int pos, DustHandle hParent) throws Exception {
		int idx = Dust.access(DustAccess.Peek, hTxt, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		DustHandle eTxt = events.get(hTxt);
		Date timeTxt = DustUtilsData.getEventDate(eTxt);
		long durationTxt = Dust.access(DustAccess.Peek, -1L, eTxt, TOKEN_EVENT_DURATION);

		String txt = accessText(DustAccess.Peek, null, hTxt);
		double ratio = 1 - ((double) pos / (double) txt.length());
		long delta = (long) (ratio * (double) durationTxt);

		DustHandle e2;
		Date time2;
		long duration2;

		DustHandle hUnit;

		DustHandle h2;
		String t2;

		String tt;

		Dust.log(TOKEN_LEVEL_TRACE, "manageEvent", eCmd, hTxt, pos, hParent, idx, txt);

		switch (eCmd) {
		case evtMergeNext:
			break;
		case evtMergePrev:
			break;
		case evtSplit:
			tt = txt.substring(pos).trim();

			accessText(DustAccess.Set, txt.substring(0, pos).trim(), hTxt);
			durationTxt -= delta;
			Dust.access(DustAccess.Set, durationTxt, eTxt, TOKEN_EVENT_DURATION);

			hUnit = Dust.access(DustAccess.Peek, null, eTxt, TOKEN_EVENT_DURATION_UNIT);

			time2 = new Date(timeTxt.getTime() + durationTxt);
			optCreateTextEvent(hParent, idx + 1, tt, time2, delta, hUnit);

			break;
		case evtToNext:
			h2 = Dust.access(DustAccess.Peek, null, hParent, TOKEN_MEMBERS, idx + 1);
			if (null == h2) {

			} else {
				t2 = accessText(DustAccess.Peek, null, h2);

				tt = txt.substring(pos).trim();
				t2 = tt + " " + t2.trim();

				accessText(DustAccess.Set, t2, h2);
				accessText(DustAccess.Set, txt.substring(0, pos).trim(), hTxt);

				Dust.access(DustAccess.Set, durationTxt - delta, eTxt, TOKEN_EVENT_DURATION);

				e2 = events.get(h2);
				time2 = DustUtilsData.getEventDate(e2);
				Date dd = new Date(time2.getTime() - delta);
				DustUtilsData.setEventDate(e2, dd);
				duration2 = Dust.access(DustAccess.Peek, -1L, e2, TOKEN_EVENT_DURATION);
				Dust.access(DustAccess.Set, duration2 + delta, e2, TOKEN_EVENT_DURATION);
			}
			break;
		case evtToPrev:
			h2 = Dust.access(DustAccess.Peek, null, hParent, TOKEN_MEMBERS, idx - 1);
			if (null == h2) {

			} else {
				tt = txt.substring(0, pos).trim();

				t2 = accessText(DustAccess.Peek, null, h2);
				t2 = t2.trim() + " " + tt;

				accessText(DustAccess.Set, t2, h2);
				accessText(DustAccess.Set, txt.substring(pos).trim(), hTxt);

				ratio = ((double) pos / (double) txt.length());
				delta = (long) (ratio * (double) durationTxt);

				e2 = events.get(h2);
				duration2 = Dust.access(DustAccess.Peek, -1L, e2, TOKEN_EVENT_DURATION);
				Dust.access(DustAccess.Set, duration2 + delta, e2, TOKEN_EVENT_DURATION);

				Date dd = new Date(timeTxt.getTime() + delta);
				DustUtilsData.setEventDate(eTxt, dd);
				Dust.access(DustAccess.Set, durationTxt - delta, eTxt, TOKEN_EVENT_DURATION);
			}

			break;

		}
	}

	public void translateSubtitles(DustHandle hLangTo) throws Exception {
		String txt;
		int pos;
		boolean cancel = false;

		for (DustHandle hTxt : new HashSet<DustHandle>(events.keySet())) {
			txt = accessText(DustAccess.Peek, "", hTxt).trim();

			if (DustUtils.isEmpty(txt)) {
				continue;
			} else {
				txt = txt.substring(0, txt.length() - 1);
			}

			do {
				pos = DustUtils.getLastOf(txt, SENTENCE_SPLIT);
				if (0 < pos) {
					if ((pos + 1 < txt.length()) && Character.isWhitespace(txt.charAt(pos + 1))) {
						int reply = JOptionPane.showConfirmDialog(null, txt + " after " + pos, "Split this?", JOptionPane.YES_NO_CANCEL_OPTION);
						switch (reply) {
						case JOptionPane.YES_OPTION:
							Dust.log(TOKEN_LEVEL_TRACE, "Splitting text", txt, "after pos", pos);
							manageEvent(EventCommand.evtSplit, hTxt, pos + 1, hDoc);
							break;
						case JOptionPane.CANCEL_OPTION:
							cancel = true;
							break;
						}
					}
					txt = txt.substring(0, pos - 1);
				}
			} while (!cancel && (0 < pos));

			if (cancel) {
				break;
			}
		}

		if (!cancel) {
			save();
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<html><body>\n");

		boolean begin = true;

		for (DustHandle hTxt : (Iterable<DustHandle>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, hDoc, TOKEN_MEMBERS)) {
//			for (DustHandle hTxt : events.keySet()) {
			txt = accessText(DustAccess.Peek, "", hTxt).trim();
			if (!DustUtils.isEmpty(txt)) {
				if (begin) {
					sb.append("<p>\n");
					begin = false;
				}
				sb.append("  <span id=\"").append(hTxt.getId()).append("\">").append(txt).append("</span>\n");
				int ssp = DustUtils.getLastOf(txt, SENTENCE_SPLIT);
				if (ssp == (txt.length() - 1)) {
					sb.append("</p>\n");
					begin = true;
				}
			}
		}

		sb.append("</body><html>\n");

		String ttxt = DustSandboxTextUtils.translateLibreLocal(hLang, hLangTo, "html", sb.toString());

		DustSandboxTextUtils.processTranslated(this, ttxt, hLangTo, events.keySet());

		save();
	}

	public void exportFile(File f) throws Exception {
		DustUtilsFile.ensureDir(f.getParentFile());
		String type = DustUtils.getPostfix(f.getName(), ".").toLowerCase();

		try (FileWriter fw = new FileWriter(f)) {
			switch (type) {
			case "sbv":
				Map<Date, DustHandle> evts = new TreeMap<>();

				for (DustHandle hEvt : events.values()) {
					evts.put(DustUtilsData.getEventDate(hEvt), hEvt);
				}

				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

				for (Map.Entry<Date, DustHandle> ee : evts.entrySet()) {
					Date d = ee.getKey();
					DustHandle hEvt = ee.getValue();
					DustHandle hTxt = Dust.access(DustAccess.Peek, null, hEvt, TOKEN_TARGET);
					String str = accessText(DustAccess.Peek, null, hTxt);

					if (!DustUtils.isEmpty(str)) {
						long duration = Dust.access(DustAccess.Peek, -1L, hEvt, TOKEN_EVENT_DURATION);
						Date d2 = new Date(d.getTime() + duration);

						fw.append(sdf.format(d)).append(",").append(sdf.format(d2)).append("\n").append(str).append("\n\n");

						fw.flush();
					}
				}
				break;
			}
		}
	}

	public void importFile(File f) {
		String type = DustUtils.getPostfix(f.getName(), ".").toLowerCase();
		Pattern pt1 = Pattern.compile("\\d+:\\d+:\\d+\\.\\d+\\s*,\\s*\\d+:\\d+:\\d+\\.\\d+\\s*");
		SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss.SSS");
		DustHandle hDurationUnit = DustUtils.CONST_HANDLES.get(TOKEN_EVENT_DURATION_UNIT_MSEC, TOKEN_KBMETA_TAG);

		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			StringBuilder sb = null;
			Date dStart = null;
			Date dEnd = null;
			long durationMsec = 0;

			Date prevStart = null;
			Date prevEnd = null;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}

				switch (type) {
				case "sbv":
					Matcher m = pt1.matcher(line);

					if (m.matches()) {
						prevStart = dStart;
						prevEnd = dEnd;

						String[] strRange = line.split(",");
						dStart = df1.parse(strRange[0].trim());
						dEnd = df1.parse(strRange[1].trim());

						if (0 > DustUtils.safeCompare(dStart, prevEnd)) {
							prevEnd = dStart;
						}

						if (null != prevStart) {
							durationMsec = Math.abs(prevEnd.getTime() - prevStart.getTime());
							optCreateTextEvent(hDoc, KEY_ADD, DustUtils.toString(sb), prevStart, durationMsec, hDurationUnit);
						}
						sb = null;
					} else {
						sb = DustUtils.sbAppend(sb, " ", false, line.trim());
					}
					break;
				}
			}

			optCreateTextEvent(hDoc, KEY_ADD, DustUtils.toString(sb), dStart, durationMsec, hDurationUnit);
		} catch (Throwable e) {

		}
	}

	public DustHandle getStream(DustHandle h) {
		DustHandle stream = streamRefs.get(h).iterator().next();
		return stream;
	}

	public void updateDB() throws Exception {
		Set<DustHandle> streamColl = new HashSet<>();
		for (DustHandle h : DustMindUtils.getUnitMembers(hRes)) {
			String ht = h.getType().getId();
			if (DustUtils.isEqual(TOKEN_STREAM, ht)) {
				streamColl.add(h);
			}
		}

		if (!streamColl.isEmpty()) {
			getSqla();
			sqla.update(streamColl);
		}
	}

	public void processStream(DustHandle hStream, StreamProcessor sp) throws Exception {
		getSqla().processStream(hStream, sp);
	}

	public DustSandboxSQLAgent getSqla() throws Exception {
		if (null == sqla) {
			sqla = new DustSandboxSQLAgent();
			sqla.initSql(DustSandboxSQLAgent.TEST_URL, DustSandboxSQLAgent.DEF_TABLE, DustSandboxSQLAgent.DEF_COLS, 2);
		}
		return sqla;
	}
}
