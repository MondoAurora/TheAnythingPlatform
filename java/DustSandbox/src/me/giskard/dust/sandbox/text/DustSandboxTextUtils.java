package me.giskard.dust.sandbox.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.net.DustNetUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.mod.utils.DustUtilsJson;

public class DustSandboxTextUtils implements DustSandboxTextConsts {
	public static String getId(Element e) {
		SimpleAttributeSet sas = (SimpleAttributeSet) e.getAttributes().getAttribute(HTML.Tag.SPAN);
		String id = (null == sas) ? null : (String) sas.getAttribute(HTML.Attribute.ID);

		return (null == id) ? (String) e.getAttributes().getAttribute(HTML.Attribute.ID) : id;
	}
	
	public static void processTranslated(DustSandboxTextAgent txtAgent, String ttxt, DustHandle tLan, Collection<DustHandle> hSel) throws IOException, BadLocationException {
		Reader stringReader = new StringReader(ttxt);
		HTMLEditorKit htmlKit = new HTMLEditorKit();
		HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
		htmlKit.read(stringReader, htmlDoc, 0);

		int dl = htmlDoc.getLength();
		String newTxt = htmlDoc.getText(0, dl);

		for (DustHandle s : hSel) {
			String k = s.getId();
			Element te = htmlDoc.getElement(k);
			if ( null != te ) {
				int so = te.getStartOffset();
				int eo = te.getEndOffset();
				if ( dl < eo ) {
					eo = dl;
				}
				String tt = newTxt.substring(so, eo);
				txtAgent.accessTextExt(DustAccess.Set, tLan, tt, s);
			}
		}
	}



	public static String translateLibreLocal(DustHandle hFrom, DustHandle hTo, String format, String content) {
		return translateLibre("http://localhost:5000/translate", "", hFrom, hTo, format, content);
	}

	public static String translateLibre(String address, String apiKey, DustHandle hFrom, DustHandle hTo, String format, String content) {
		try {
			File dir = new File("work");
			DustUtilsFile.ensureDir(dir);
			try ( FileWriter fw = new FileWriter(new File(dir, "translating." + format))) {
				fw.append(content);
				fw.flush();
			}
			Map<String, Object> msg = new TreeMap<>();

			msg.put("q", content);
			msg.put("source", DustUtils.getPostfix(hFrom.getId(), DUST_SEP_TOKEN));
			msg.put("target", DustUtils.getPostfix(hTo.getId(), DUST_SEP_TOKEN));
			msg.put("format", format);
			msg.put("alternatives", 1);
			msg.put("api_key", apiKey);

			String json = DustUtilsJson.toJson(msg);

			Dust.log(TOKEN_LEVEL_TRACE, "translating... \n", json);

			URL url = new URL(address);
			Set<String> hdrs = new HashSet<String>();
			hdrs.add("Content-Type: application/json");
			HttpURLConnection http = DustNetUtils.getConn(url, 0, hdrs);
			
			http.setRequestMethod("POST"); // PUT is another valid option
			http.setDoOutput(true);

			OutputStream os = http.getOutputStream();
			os.write(json.getBytes());
			os.flush();
			os.close();

			InputStream is = http.getInputStream();

			StringBuilder sb = new StringBuilder();
			try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
				int c = 0;
				while ((c = reader.read()) != -1) {
					sb.append((char) c);
				}
			}

			Map<String, Object> jsonResp = DustUtilsJson.parseJson(sb.toString());

			return (String) jsonResp.get("translatedText");

		} catch (Throwable ex) {
			return DustException.wrap(ex);
		}
	}

}
