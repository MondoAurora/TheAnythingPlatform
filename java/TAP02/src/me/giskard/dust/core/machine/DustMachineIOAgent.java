package me.giskard.dust.core.machine;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings("rawtypes")
public class DustMachineIOAgent extends DustAgent implements DustMachineConsts  {

	@Override
	public void process() throws Exception {
		DustHandle hCmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
		DustHandle hNext = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);

		DustHandle hTarget = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);
		String name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_KEY);

		DustHandle hSource = Dust.access(DustAccess.Peek, null, null, HANDLE_STREAM_ATT_SOURCE);

		if (null != hTarget) {
			if ( DustUtils.isEqual(HANDLE_MIND_ASP_UNIT, hTarget)) {
				hTarget = Dust.getHandle(name, true);
			}
			
			Dust.access(DustAccess.Set, hCmd, hSource, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Set, hTarget, hSource, HANDLE_MISC_ATT_TARGET);
			Dust.access(DustAccess.Set, name, hSource, HANDLE_MISC_ATT_KEY);
			Dust.access(DustAccess.Set, hNext, hSource, HANDLE_MIND_ATT_NEXT);

			Dust.access(DustAccess.Set, hCmd, hNext, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Set, hTarget, hNext, HANDLE_MISC_ATT_TARGET);

			Dust.access(DustAccess.Commit, null, hSource);

			Dust.access(DustAccess.Delete, null, hSource, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Delete, null, hSource, HANDLE_MISC_ATT_TARGET);
			Dust.access(DustAccess.Delete, null, hSource, HANDLE_MISC_ATT_KEY);
			Dust.access(DustAccess.Delete, null, hSource, HANDLE_MIND_ATT_NEXT);

			Dust.access(DustAccess.Delete, null, hNext, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Delete, null, hNext, HANDLE_MISC_ATT_TARGET);
		}
	}
}
