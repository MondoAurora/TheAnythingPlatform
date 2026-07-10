package me.giskard.dust.core.mind;

import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;

@SuppressWarnings("rawtypes")
public interface DustMindNarrative extends DustMindConsts {
	
	abstract class ChainAgent extends DustAgent {
		DustHandle hNext;

		@Override
		protected void init() throws Exception {
			hNext = Dust.access(DustAccess.Peek, null, null, TOKEN_NEXT);
		};

	}
	
	public class ForAll extends ChainAgent {

		@Override
		protected Object process(DustAccess access) throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			Map params = new HashMap();

			Dust.access(DustAccess.Set, cmd, params, TOKEN_CMD);
			Dust.access(DustAccess.Process, params, hNext);
			return null;
		}
		
	}
	
	public class Filter extends ChainAgent {

		@Override
		protected Object process(DustAccess access) throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			Map params = new HashMap();

			Dust.access(DustAccess.Set, cmd, params, TOKEN_CMD);
			Dust.access(DustAccess.Process, params, hNext);
			return null;
		}
		
	}
}
