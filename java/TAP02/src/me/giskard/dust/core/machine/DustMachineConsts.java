package me.giskard.dust.core.machine;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustConsts;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.handles.giskard_me.DustGenHandles_dust_1;
import me.giskard.handles.giskard_me.DustGenHandles_mind_1;
import me.giskard.handles.giskard_me.DustGenHandles_stream_1;

public interface DustMachineConsts extends DustConsts, DustUtilsConsts, DustGenHandles_mind_1, DustGenHandles_stream_1, DustGenHandles_dust_1 {
	DustHandle[] CTXS = new DustHandle[] { HANDLE_DUST_ATT_CTX_MSG, HANDLE_DUST_ATT_CTX_AGT, HANDLE_DUST_ATT_CTX_DLG,
			HANDLE_DUST_ATT_CTX_APP, };

	abstract class ChainAgent extends DustAgent {
		DustHandle hNext;

		@Override
		public void init() throws Exception {
			hNext = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);
		};

	}
}
