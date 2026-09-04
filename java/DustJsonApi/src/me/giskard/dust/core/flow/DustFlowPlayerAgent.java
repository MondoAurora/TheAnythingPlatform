package me.giskard.dust.core.flow;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustFlowPlayerAgent extends DustAgent implements DustFlowConsts {

	@Override
	protected void init() throws Exception {
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		Object msg = Dust.access(DustAccess.Peek, null, DustContext.Message);
//		StringBuilder sb = null;

//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "FlowPlayer received command", cmd);

		Iterable<DustHandle> members = Dust.access(DustAccess.Visit, null, null, TOKEN_MISC_ATT_MEMBERS);

		if (null != members) {
			Object cmd = Dust.access(DustAccess.Peek, null, msg, TOKEN_MIND_ATT_CMD);
			for (DustHandle h : members) {
				Dust.access(DustAccess.Set, cmd, h, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, null, h);
			}
		}
		return null;

	}

}
