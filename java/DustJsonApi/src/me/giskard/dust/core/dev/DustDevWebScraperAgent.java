package me.giskard.dust.core.dev;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.dust.core.net.DustNetUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.mod.utils.DustUtilsHtmlJsoup;
import me.giskard.tokens.DustGenTokens_stream_1;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustDevWebScraperAgent extends DustAgent implements DustDevConsts, DustGenTokens_stream_1 {

	@Override
	protected Object process(DustAccess access) throws Exception {
//		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		String cacheRoot = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_PATH);

		File cr = DustUtilsFile.ensureDir(cacheRoot);
		String urlStr = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_URL);

		URL url = new URL(urlStr);

		String author = url.getHost();

		int ii = urlStr.indexOf(author);
		String prefix = urlStr.substring(0, ii) + author;

		if (author.startsWith("www.")) {
			author = author.substring(author.indexOf(".") + 1);
		}
		String data = DustUtils.getPostfix(urlStr, "/");

		File f = DustNetUtils.downloadCached(cr, urlStr, null, null, 5000);

		Document doc = DustUtilsHtmlJsoup.readHtml(f, DUST_CHARSET_UTF8);

		DustHandle target = Dust.getUnit(author + "/" + data, true);

		switch (data) {
		case "media-types":
			readMediaTypes(urlStr, prefix, doc, target, data);
			break;
		}

		return null;
	}

	void readMediaTypes(String urlStr, String prefix, Document doc, DustHandle target, String type) {
		String key;
		String[] atts = { "Name", "Template", "Reference" };
		Map<String, Object> rec = new HashMap<String, Object>();

		DustHandle hType = Dust.getHandle(target, TOKEN_MIND_ASP_ASPECT, type, DustOptCreate.Primary);
		DustCreator<DustHandle> cc = new DustCreator<DustHandle>() {
			@Override
			public DustHandle create(Object key, Object... hints) {
				DustHandle h = Dust.getHandle(target, hType, null, DustOptCreate.Primary);
				return h;
			}
		};
		DustUtilsFactory<String, DustHandle> content = new DustUtilsFactory<String, DustHandle>(cc, true);

		DustCreator<DustHandle> cRef = new DustCreator<DustHandle>() {
			@Override
			public DustHandle create(Object key, Object... hints) {
				DustHandle h = Dust.getHandle(target, TOKEN_STREAM_ASP_URLREF, null, DustOptCreate.Primary);
				return h;
			}
		};
		DustUtilsFactory<String, DustHandle> refs = new DustUtilsFactory<String, DustHandle>(cRef, true);

		for (DustHandle h : DustMachineUtils.getUnitMembers(target)) {
			DustHandle ht = h.getType();

			if (hType == ht) {
				key = Dust.access(DustAccess.Peek, null, h, atts[0]);
				content.put(key, h);
			} else if (TOKEN_STREAM_ASP_URLREF.equals(ht.getId())) {
				key = Dust.access(DustAccess.Peek, null, h, TOKEN_STREAM_ATT_URL);
				refs.put(key, h);
			}
		}

		Elements all = doc.getAllElements();

		Elements el;

		ArrayList<String> cols = new ArrayList<String>();
		int colIdx = 0;
		String txt;

		DustHandle groupTag = null;

		for (Element e : all) {
			String tag = e.tagName();

			switch (tag) {
			case "table":
				String tagName = e.id();

				if (tagName.startsWith("table-")) {
					tagName = tagName.substring(tagName.indexOf("-") + 1);
					groupTag = Dust.getHandle(target, TOKEN_MIND_ASP_TAG, tagName, DustOptCreate.Primary);
				} else {
					groupTag = null;
				}
				Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, tagName);
				cols.clear();
				break;
			case "th":
				if (null != groupTag) {
					txt = e.ownText().trim();
					if (-1 == DustUtils.indexOf(txt, (Object[]) atts)) {
						cols.clear();
					} else {
						Dust.getHandle(target, TOKEN_MIND_ASP_ATTRIBUTE, txt, DustOptCreate.Primary);
						cols.add(txt);
					}
				}
				break;
			case "tbody":
				break;
			case "tr":
				colIdx = 0;
				optAddRec(content, atts[0], rec, groupTag);
				break;
			case "td":
				if (!cols.isEmpty()) {
					String cn = cols.get(colIdx);
					el = e.getElementsByTag("a");
					Object v = null;

					switch (cn) {
					case "Name":
						v = e.wholeText().trim();
						break;
					case "Template":
						Element a = el.first();
						v = loadRef(urlStr, prefix, a, refs);
						break;
					case "Reference":
						Set s = new HashSet();
						for (Element aa : el) {
							s.add(loadRef(urlStr, prefix, aa, refs));
							v = s;
						}
						break;
					}

					if (null != v) {
						rec.put(cn, v);
					}

					++colIdx;
				}
				break;

			default:
				break;
			}
		}

		optAddRec(content, atts[0], rec, groupTag);
	}

	public DustHandle loadRef(String urlStr, String prefix, Element a, DustUtilsFactory<String, DustHandle> refs) {
		String url = a.attr("href");
		String name = a.wholeText().trim();
		DustHandle hf = DustNetUtils.loadRef(urlStr, prefix, url, name, refs);
		return hf;
	}

	public DustHandle optAddRec(DustUtilsFactory<String, DustHandle> content, String keyAtt, Map<String, Object> rec, DustHandle groupTag) {
		DustHandle h = null;

		if (!rec.isEmpty() && (null != groupTag)) {
			String id = (String) rec.get(keyAtt);
			h = content.get(id);

			for (Map.Entry<String, Object> re : rec.entrySet()) {
				String key = re.getKey();
				Object val = re.getValue();
				if (null != val) {
					if (val instanceof Collection) {
						for (Object v : (Collection) val) {
							Dust.access(DustAccess.Insert, v, h, key);
						}
					} else {
						Dust.access(DustAccess.Set, val, h, key);
					}
				}
			}

			Dust.access(DustAccess.Insert, groupTag, h, TOKEN_MIND_ATT_TAGS);

			Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, rec);
			rec.clear();

			String group = groupTag.getId();
			
			group = "TAP/mediatype-" + DustUtils.getPostfix(group, DUST_SEP_TOKEN);

			DustHandle hu = Dust.getUnit(group, true);
			String ti = h.getId();
			ti = DustUtils.getPostfix(ti, DUST_SEP_TOKEN);
			
			DustHandle ht = Dust.getHandle(hu, TOKEN_MIND_ASP_TAG, ti, DustOptCreate.Primary);
			Dust.access(DustAccess.Set, id, ht, TOKEN_MISC_ATT_KEY);
			Dust.access(DustAccess.Set, h.getId(), ht, TOKEN_MISC_ATT_SOURCE);
			
			DustHandle hParent = Dust.getHandle(null, null, TOKEN_STREAM_TAG_MEDIATYPE, DustOptCreate.None);
			Dust.access(DustAccess.Set, hParent, ht, TOKEN_MISC_ATT_PARENT);

			Object v = Dust.access(DustAccess.Peek, null, h, "Template", TOKEN_STREAM_ATT_URL);
			Dust.access(DustAccess.Set, v, ht, TOKEN_STREAM_ATT_URL);

			Collection r = Dust.access(DustAccess.Peek, null, h, "Reference");
			for (Object ro : r) {
				v = Dust.access(DustAccess.Peek, null, ro, TOKEN_STREAM_ATT_URL);
				Dust.access(DustAccess.Insert, v, ht, TOKEN_STREAM_ATT_URLREFS);
			}
		}

		return h;
	}
}
