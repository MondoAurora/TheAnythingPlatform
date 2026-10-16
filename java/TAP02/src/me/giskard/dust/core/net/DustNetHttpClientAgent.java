package me.giskard.dust.core.net;

import java.io.OutputStream;
import java.util.Collection;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;

public class DustNetHttpClientAgent extends DustAgent implements DustNetConsts {

	public DustNetHttpClientAgent() {
	}

	@Override
	public void process() throws Exception {

		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			String urlRoot = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_URL);
			String key = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_KEY);
			int timeout = Dust.access(DustAccess.Peek, 0, null, HANDLE_MISC_ATT_TIMEOUT);
			Collection<String> headers = Dust.access(DustAccess.Peek, null, null, HANDLE_NET_ATT_SRVCALL_HEADERS);

			OutputStream out = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_OUTPUT);

			String url = urlRoot + "/" + key;
			
			DustNetUtils.download(url, out, headers, timeout);
		} 
	}
}
