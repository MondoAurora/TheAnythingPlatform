package me.giskard.dust.core.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings("unchecked")
public class DustNetUtils implements DustNetConsts {

	public static String getContentType(File f) {
		String ct = null;

		String ext = DustUtils.getPostfix(f.getName(), ".").toLowerCase();

		switch (ext) {
		case "csv":
			ct = MEDIATYPE_UTF8_CSV;
			break;
		case "xml":
			ct = MEDIATYPE_UTF8_XML;
			break;
		case "html":
		case "xhtml":
			ct = MEDIATYPE_UTF8_HTML;
			break;

		case "jpg":
			ct = "image/jpeg";
			break;
		case "ico":
			ct = "image/x-icon";
			break;

		case "png":
		case "jpeg":
		case "gif":
		case "bmp":
			ct = "image/" + ext;
			break;
		}

		return ct;
	}

	public static HttpURLConnection getConn(URL url, int timeout, Collection<String> headers) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		for (String h : headers) {
			int s = h.indexOf(":");
			String key = h.substring(0, s).trim();
			String val = h.substring(s + 1).trim();
			conn.setRequestProperty(key, val);
		}

		if (-1 != timeout) {
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
		}
		return conn;
	}

//	@SuppressWarnings("deprecation")
	public static boolean download(String urlStr, OutputStream target, Collection<String> headers, int timeout) throws Exception {
		boolean success = false;

		URL url = new URL(urlStr);
		HttpURLConnection conn = getConn(url, timeout, (null == headers) ? Collections.EMPTY_LIST : headers);

        String protocol = url.getProtocol().toLowerCase();
		if ("http".equals(protocol) || "https".equals(protocol)) {
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			int resCode = conn.getResponseCode();
			if (resCode == HttpURLConnection.HTTP_SEE_OTHER || resCode == HttpURLConnection.HTTP_MOVED_PERM || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				String redirect = conn.getHeaderField("Location");
				if (redirect.startsWith("/")) {
					redirect = url.getProtocol() + "://" + url.getHost() + redirect;
				}
				conn.disconnect();

				url = new URL(redirect);
				conn = getConn(url, timeout, headers);
			}
		} else {
            conn.connect();
        }

		InputStream is = conn.getInputStream();

		if ("gzip".equals(conn.getContentEncoding())) {
			try (GZIPInputStream i = new GZIPInputStream(is)) {
				success = DustStreamUtils.copyStream(i, target);
			}
		} else {
			try (BufferedInputStream in = new BufferedInputStream(is)) {
                success = DustStreamUtils.copyStream(in, target);
			}
		}

		return success;
	}

	public static void sslHack() throws Exception {
		
    SSLContext ctx = SSLContext.getDefault();
    for (String s : ctx.getSupportedSSLParameters().getCipherSuites()) {
      if (s.contains("AES_256_CBC_SHA"))
        System.out.println(s);
    }
		
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
    };

    // 2. Install the all-trusting trust manager
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    // 3. Create all-trusting host name verifier
    HostnameVerifier allHostsValid = (hostname, session) -> true;

    // 4. Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    
		System.setProperty("jdk.tls.client.enableSessionTicketExtension", "false");

		
	}

}
