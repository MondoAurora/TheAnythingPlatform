package me.giskard.dust.core;

import me.giskard.tokens.giskard_me.DustGenTokens_aaa_1;
import me.giskard.tokens.giskard_me.DustGenTokens_dust_1;
import me.giskard.tokens.giskard_me.DustGenTokens_mind_1;
import me.giskard.tokens.giskard_me.DustGenTokens_misc_1;

public interface DustConsts extends DustConstsBoot, DustGenTokens_dust_1, DustGenTokens_misc_1, DustGenTokens_mind_1, DustGenTokens_aaa_1 {

	public abstract class DustAgent implements DustConsts {
		protected final Object process(DustAction action, DustAccess access) throws Exception {
			Object ret = null;

			switch (action) {
			case Init:
				init();
				Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "Initialising on thread", Thread.currentThread());
				break;
			case Begin:
				ret = begin();
				break;
			case Process:
				ret = process(access);
				break;
			case End:
				ret = end(access == DustAccess.Commit);
				break;
			case Release:
				release();
				break;
			}

			return ret;
		}

		protected void init() throws Exception {
		}

		protected Object begin() throws Exception {
			return NOT_IMPLEMENTED;
		}

		protected abstract Object process(DustAccess access) throws Exception;

		protected Object end(boolean commit) throws Exception {
			return NOT_IMPLEMENTED;
		}

		protected void release() throws Exception {
		}
	}

}
