package me.giskard.dust.net;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.utils.IOUtils;

import me.giskard.dust.utils.DustUtils;

public class DustNetUtils implements DustNetConsts {

	public static String getContentType(File f) {
		String ct = null;

		String ext = DustUtils.getPostfix(f.getName(), ".").toLowerCase();

		switch ( ext ) {
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

		if ( -1 != timeout ) {
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(timeout);
		}
		return conn;
	}

//	@SuppressWarnings("deprecation")
	public static boolean download(String urlStr, OutputStream target, Collection<String> headers, int timeout) throws Exception {
		boolean success = false;
		
		URL url = new URL(urlStr);
		HttpURLConnection conn = getConn(url, timeout, headers);

		if ( "http".equals(url.getProtocol()) ) {
			conn.setInstanceFollowRedirects(false);
			conn.connect();

			int resCode = conn.getResponseCode();
			if ( resCode == HttpURLConnection.HTTP_SEE_OTHER || resCode == HttpURLConnection.HTTP_MOVED_PERM || resCode == HttpURLConnection.HTTP_MOVED_TEMP ) {
				String redirect = conn.getHeaderField("Location");
				if ( redirect.startsWith("/") ) {
					redirect = url.getProtocol() + "://" + url.getHost() + redirect;
				}
				conn.disconnect();
				
				url = new URL(redirect);
				conn = getConn(url, timeout, headers);
			}
		}

		InputStream is = conn.getInputStream();

		if ( "gzip".equals(conn.getContentEncoding()) ) {
			try (GZIPInputStream i = new GZIPInputStream(is)) {
				success = 0 < IOUtils.copy(i, target);
			}
		} else {
			try (BufferedInputStream in = new BufferedInputStream(is)) {
				byte dataBuffer[] = new byte[1024];
				int bytesRead;
				while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					target.write(dataBuffer, 0, bytesRead);
					success = true;
				}
			}
		}
		
		return success;
	}
}
