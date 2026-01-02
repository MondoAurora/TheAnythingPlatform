package me.giskard.dust;

public abstract class DustAgent implements DustConsts {
	protected void init() throws Exception {
	}

	protected abstract Object process(DustAction action) throws Exception;

	protected void release() throws Exception {
	}
}