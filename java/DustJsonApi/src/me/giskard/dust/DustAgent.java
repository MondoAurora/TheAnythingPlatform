package me.giskard.dust;

import me.giskard.dust.DustConsts.DustAccess;

public abstract class DustAgent implements DustConsts {
	protected void init() throws Exception {
	}

	protected abstract Object process(DustAccess access) throws Exception;

	protected void release() throws Exception {
	}
}