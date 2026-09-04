package me.giskard.dust.mod.utils;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.giskard.dust.core.utils.DustUtilsConsts;

public class DustUtilsHtmlJsoup implements DustUtilsConsts {

	public static Document readHtml(String fileName, String charsetName) throws Exception {
		return readHtml(new File(fileName), charsetName);
	}

	public static Document readHtml(File f, String charsetName) throws Exception {
		Document ret = null;

		if (f.isFile()) {
			ret = Jsoup.parse(f, charsetName);
		}

		return ret;
	}

}
