package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import me.giskard.dust.core.DustConsts;

public interface DustStreamConsts extends DustConsts {
	String TOKEN_INPUT_STREAM = UNIT_DUST + DUST_SEP_TOKEN + "inputStream";
	String TOKEN_OUTPUT_STREAM = UNIT_DUST + DUST_SEP_TOKEN + "outputStream";
	String TOKEN_STREAM_SOURCE = UNIT_DUST + DUST_SEP_TOKEN + "streamSource";

	String TOKEN_STREAM_ROOTFOLDER = UNIT_DUST + DUST_SEP_TOKEN + "rootFolder";
	String TOKEN_STREAM_URL = UNIT_DUST + DUST_SEP_TOKEN + "url";
	String TOKEN_STREAM_MIMETYPE = UNIT_DUST + DUST_SEP_TOKEN + "mimeType";
	String TOKEN_STREAM_ENCODING = UNIT_DUST + DUST_SEP_TOKEN + "encoding";

	String TOKEN_STREAM_WRITER = UNIT_DUST + DUST_SEP_TOKEN + "writer";

	String TOKEN_STREAM_SAPFIRSTCOL = UNIT_DUST + DUST_SEP_TOKEN + "firstCol";
	String TOKEN_STREAM_COLSEP = UNIT_DUST + DUST_SEP_TOKEN + "colSep";
	String TOKEN_STREAM_ROWSEP = UNIT_DUST + DUST_SEP_TOKEN + "rowSep";

	public interface StreamProcessor {
		default boolean readStream(InputStream is, Map<String, Object> data) throws Exception {
			return false;
		};

		default boolean writeStream(OutputStream os, Map<String, Object> data) throws Exception {
			return false;
		};
	}
}
