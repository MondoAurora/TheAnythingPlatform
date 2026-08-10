package me.giskard.dust.core.flow;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustFlowPlayerAgent extends DustAgent implements DustFlowConsts {
	
	@Override
	protected void init() throws Exception {}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_MIND_ATT_CMD);
//		StringBuilder sb = null;

		switch (cmd) {
		case "LnF":
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}

}
