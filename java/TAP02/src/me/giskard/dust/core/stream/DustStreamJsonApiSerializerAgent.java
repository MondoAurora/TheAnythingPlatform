package me.giskard.dust.core.stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;

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
			try {
				Dust.access(DustAccess.Set, HANDLE_MISC_TAG_STATE_LOADING, unit, HANDLE_MIND_ATT_UNIT_STATE);
				FileInputStream fis = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_INPUT);
				DustUtilsJsonApi.loadUnit(unit, fis);
				Dust.access(DustAccess.Set, HANDLE_MISC_TAG_STATE_IN_SYNC, unit, HANDLE_MIND_ATT_UNIT_STATE);
			} catch (Throwable e) {
				Dust.access(DustAccess.Set, HANDLE_MISC_TAG_STATE_LOAD_FAILED, unit, HANDLE_MIND_ATT_UNIT_STATE);
			}
		} else if (HANDLE_MISC_TAG_CMD_SAVE.equals(cmd)) {
			FileOutputStream fos = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_OUTPUT);
			DustUtilsJsonApi.storeUnit(unit, fos);
		}
	}
}
