package me.giskard.dust.api;

public abstract class DustAgent implements DustConsts {
	public void init() throws Exception {
	}

	public void begin() throws Exception {
	}

	public abstract void process() throws Exception;

	public void end(boolean commit) throws Exception {
	}

	public void release() throws Exception {
	}

}
