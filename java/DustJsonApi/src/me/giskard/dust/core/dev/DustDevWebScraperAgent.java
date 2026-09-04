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

		for (Element e : all) {
			String tag = e.tagName();

			switch (tag) {
			case "table":
				Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, e.id());
				cols.clear();
				break;
			case "th":
				txt = e.ownText().trim();
				if (-1 == DustUtils.indexOf(txt, (Object[]) atts)) {
					cols.clear();
				} else {
					Dust.getHandle(target, TOKEN_MIND_ASP_ATTRIBUTE, txt, DustOptCreate.Primary);
					cols.add(txt);
				}
				break;
			case "tbody":
				break;
			case "tr":
				colIdx = 0;
				optAddRec(content, atts[0], rec);
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
	}

	public DustHandle loadRef(String urlStr, String prefix, Element a, DustUtilsFactory<String, DustHandle> refs) {
		String txt;
		String src = a.attr("href");
		if (!src.startsWith("http")) {
			String p = src.startsWith("#") ? urlStr : prefix;
			src = p + src;
		}
		txt = a.wholeText().trim();

		DustHandle hf = refs.get(src);
		Dust.access(DustAccess.Set, src, hf, TOKEN_STREAM_ATT_URL);
		Dust.access(DustAccess.Set, txt, hf, TOKEN_MISC_ATT_NAME);
		return hf;
	}

	public void optAddRec(DustUtilsFactory<String, DustHandle> content, String keyAtt, Map<String, Object> rec) {
		if (!rec.isEmpty()) {
			String id = (String) rec.get(keyAtt);
			DustHandle h = content.get(id);

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
			Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, rec);
			rec.clear();
		}
	}
}
