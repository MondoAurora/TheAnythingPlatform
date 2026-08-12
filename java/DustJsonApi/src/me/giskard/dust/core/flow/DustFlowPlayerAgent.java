package me.giskard.dust.core.flow;

import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustFlowPlayerAgent extends DustAgent implements DustFlowConsts {

	@Override
	protected void init() throws Exception {
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		Object cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_MIND_ATT_CMD);
//		StringBuilder sb = null;

		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "FlowPlayer received command", cmd);

		Iterable<DustHandle> members = Dust.access(DustAccess.Visit, null, null, TOKEN_MISC_ATT_MEMBERS);

		if (null != members) {
			Map params = new HashMap();

			for (DustHandle h : members) {
				Dust.access(DustAccess.Set, cmd, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, params, h);

			}
		}
		return null;

	}

}
