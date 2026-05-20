package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import me.giskard.dust.core.DustConsts;

public interface DustStreamConsts extends DustConsts {
	String TOKEN_INPUT_STREAM = DUST_UNIT_DUST + DUST_SEP_TOKEN + "inputStream";
	String TOKEN_OUTPUT_STREAM = DUST_UNIT_DUST + DUST_SEP_TOKEN + "outputStream";
	String TOKEN_STREAM_SOURCE = DUST_UNIT_DUST + DUST_SEP_TOKEN + "streamSource";

	String TOKEN_STREAM_ROOTFOLDER = DUST_UNIT_DUST + DUST_SEP_TOKEN + "rootFolder";
	String TOKEN_STREAM_URL = DUST_UNIT_DUST + DUST_SEP_TOKEN + "url";
	String TOKEN_STREAM_ENCODING = DUST_UNIT_DUST + DUST_SEP_TOKEN + "encoding";

	String TOKEN_STREAM_WRITER = DUST_UNIT_DUST + DUST_SEP_TOKEN + "writer";

	String TOKEN_STREAM_SAPFIRSTCOL = DUST_UNIT_DUST + DUST_SEP_TOKEN + "firstCol";
	String TOKEN_STREAM_COLSEP = DUST_UNIT_DUST + DUST_SEP_TOKEN + "colSep";
	String TOKEN_STREAM_ROWSEP = DUST_UNIT_DUST + DUST_SEP_TOKEN + "rowSep";

	public interface StreamProcessor {
		default boolean readStream(InputStream is, Map<String, Object> data) throws Exception {
			return false;
		};

		default boolean writeStream(OutputStream os, Map<String, Object> data) throws Exception {
			return false;
		};
	}
}
