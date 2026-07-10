package me.giskard.dust.core.dev;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

public class DustDevGenSourceTokenAgent extends DustAgent {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
		
		Dust.log(TOKEN_LEVEL_TRACE, "Source generator received command", cmd);
		return null;
	}

}
