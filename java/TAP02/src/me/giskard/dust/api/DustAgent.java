package me.giskard.dust.api;

public abstract class DustAgent implements DustConsts {
	protected void init() throws Exception {
	}

	protected void begin() throws Exception {
	}

	protected abstract void process() throws Exception;

	protected void end(boolean commit) throws Exception {
	}

	protected void release() throws Exception {
	}

}
