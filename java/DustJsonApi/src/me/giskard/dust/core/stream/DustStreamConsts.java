package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import me.giskard.dust.core.DustConsts;
import me.giskard.tokens.DustGenTokens_stream_1;

public interface DustStreamConsts extends DustConsts, DustGenTokens_stream_1 {

	public interface StreamProcessor {
		default boolean readStream(InputStream is, Map<String, Object> data) throws Exception {
			return false;
		};

		default boolean writeStream(OutputStream os, Map<String, Object> data) throws Exception {
			return false;
		};
	}
}
