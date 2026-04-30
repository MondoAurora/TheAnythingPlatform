package me.giskard.dust.sandbox.text;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;

public class DustSandboxTextAgent extends DustAgent implements DustSandboxTextConsts {

	DustHandle defaultSerializer;

	String unitId;
	DustHandle hUnit;
	DustHandle hRes;

	String resPath;
	File resRoot;

	DustHandle hDoc;
	Map<String, String> styles = new TreeMap<>();

	DustHandle hLayout;

	DustHandle hLang;
	DustUtilsFactory<DustHandle, DustHandle> factLang = new DustUtilsFactory<DustHandle, DustHandle>(new DustCreator<DustHandle>() {
		@Override
		public DustHandle create(Object key, Object... hints) {
			String langId = DustUtils.getPostfix(hLang.getId(), DUST_SEP_TOKEN);
			int p = unitId.lastIndexOf(".");
			String langUnitId = unitId.substring(0, p) + "_" + langId + unitId.substring(0, p);

			return Dust.getUnit(langUnitId, true);
		}
	}, false);

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

		for (DustHandle h : DustMindUtils.getUnitMembers(hUnit)) {
			String ht = h.getType().getId();

			switch (ht) {
			case TOKEN_TEXT_DOC:
				hDoc = h;
				setLayout(Dust.access(DustAccess.Peek, hLayout, h, TOKEN_LAYOUT_LAYOUT));
				setLang(Dust.access(DustAccess.Peek, hLang, h, TOKEN_TEXT_LANG));
				break;
			case TOKEN_TEXT_STYLE:
				String name = Dust.access(DustAccess.Peek, "", h, TOKEN_NAME);
				Map<String, String> def = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, h, TOKEN_TEXT_STYLE_DEF);

				if (!DustUtils.isEmpty(name) && !def.isEmpty()) {
					StringBuilder sb = null;
					for (Map.Entry<String, String> de : def.entrySet()) {
						sb = DustUtils.sbAppend(sb, "", false, de.getKey(), ": ", de.getValue(), "; ");
					}
					styles.put(name, sb.toString());
				}
				break;
			}
		}

		if (null == hDoc) {
			hDoc = Dust.getHandle(hUnit, TOKEN_TEXT_DOC, null, DustOptCreate.Primary);
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

		sp.put(TOKEN_KEY, hRes.getId());
		sp.put(TOKEN_DATA, hRes);
		Dust.access(DustAccess.Process, sp, defaultSerializer);

		sp.put(TOKEN_KEY, hUnit.getId());
		sp.put(TOKEN_DATA, hUnit);
		Dust.access(DustAccess.Process, sp, defaultSerializer);

	};

	public DustHandle insertNode(DustHandle hParent, int idx, String type) {
		DustHandle hRet = Dust.getHandle(hUnit, type, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Insert, hRet, hParent, TOKEN_MEMBERS, idx + 1);
		return hRet;
	}

	public DustHandle getTextNode(String id) {
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

	String accessText(DustAccess access, String val, String key) {
		DustHandle hNode = Dust.getHandle(hUnit, null, (String) key, DustOptCreate.None);

		return Dust.access(access, val, hNode, TOKEN_TEXT_TEXT);

//		DustHandle hNode = Dust.getHandle(factLang.get(hLang), TOKEN_TEXT_STRING, key, DustOptCreate.Primary);
//
//		if (access.creator) {
//			Dust.access(DustAccess.Set, hLang, hNode, TOKEN_TEXT_LANG);
//		}
//
//		return Dust.access(access, val, hNode, TOKEN_TEXT_TEXT);
	};

	public String getShortText(DustHandle h, int maxLen) {
		String ht = h.getType().getId();
		int c = Dust.access(DustAccess.Peek, 0, h, TOKEN_MEMBERS, KEY_SIZE);
		String ph = (0 < c) ? "Inline group" : "placeholder";
		String txt = accessText(DustAccess.Peek, DustUtils.isEqual(TOKEN_TEXT_BLOCK, ht) ? ph : ht, h.getId());
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

	public void insertImage(DustHandle hParent, DustHandle hThis, Image image) throws Exception {
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

		ImageIO.write(bi, imgType, new File(docPath, path));

		DustHandle hRef = Dust.getHandle(hUnit, TOKEN_STREAM_REF, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Set, hImg, hRef, TOKEN_TARGET);

		int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		Dust.access(DustAccess.Insert, hRef, hParent, TOKEN_MEMBERS, idx + 1);
	}

	public void insertLongText(DustHandle hParent, DustHandle hThis, String str) {
		if (null == hParent) {
			hParent = hDoc;
		}

		int len = Dust.access(DustAccess.Peek, 0, hParent, TOKEN_MEMBERS, KEY_SIZE);
		int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

		boolean insert = (++idx < len);
		boolean override = DustUtils.isEqual(hThis, hParent) ? false : DustUtils.isEmpty(accessText(DustAccess.Peek, null, hThis.getId()));

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
					accessText(DustAccess.Set, sb.toString(), h.getId());
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

		accessText(DustAccess.Set, remain, hThis.getId());
		accessText(DustAccess.Set, move, hNew.getId());
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
		accessText(DustAccess.Set, txt, hTxt.getId());
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
			accessText(DustAccess.Set, null, hThis.getId());
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
			accessText(DustAccess.Set, txt, hTxt.getId());
			hTxt = null;
		}

		return hTxt;
	}

}
