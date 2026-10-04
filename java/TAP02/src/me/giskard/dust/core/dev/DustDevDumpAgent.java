package me.giskard.dust.core.dev;

import me.giskard.dust.api.DustAgent;
import me.giskard.dust.core.Dust;

public class DustDevDumpAgent extends DustAgent implements DustDevConsts {

	@Override
	protected void init() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "init()");
	}
	
	@Override
	protected void begin() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "begin()");
	}
	
	@Override
	protected void process() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "process()");
	}
	
	@Override
	protected void end(boolean commit) throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "end()", commit);
	}
	
	@Override
	protected void release() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "release()");
	}
}
