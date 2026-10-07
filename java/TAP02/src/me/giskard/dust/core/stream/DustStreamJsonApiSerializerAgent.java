package me.giskard.dust.core.stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtilsJsonApi;

public class DustStreamJsonApiSerializerAgent extends DustAgent implements DustStreamConsts {

	@Override
	public void process() throws Exception {
		DustHandle unit = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);
		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

		if (HANDLE_MISC_TAG_CMD_LOAD.equals(cmd)) {
			boolean ok = false;
			try {
				Dust.access(DustAccess.Set, HANDLE_MISC_TAG_STATE_LOADING, unit, HANDLE_MIND_ATT_UNIT_STATE);
				InputStream is = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_INPUT);
				if (null == is) {
					Reader r = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_READER);
					ok = DustUtilsJsonApi.loadUnit(unit, r);
				} else {
					ok = DustUtilsJsonApi.loadUnit(unit, is);
				}
			} finally {
				Dust.access(DustAccess.Set, ok ? HANDLE_MISC_TAG_STATE_IN_SYNC : HANDLE_MISC_TAG_STATE_LOAD_FAILED, unit, HANDLE_MIND_ATT_UNIT_STATE);
			}
		} else if (HANDLE_MISC_TAG_CMD_SAVE.equals(cmd)) {
			OutputStream os = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_OUTPUT);
			if (null == os) {
				Writer w = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_READER);
				DustUtilsJsonApi.storeUnit(unit, w);
			} else {
				DustUtilsJsonApi.storeUnit(unit, os);
			}
		}
	}
}
