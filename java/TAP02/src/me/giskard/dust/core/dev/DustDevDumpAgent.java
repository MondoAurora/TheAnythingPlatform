package me.giskard.dust.core.dev;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;

public class DustDevDumpAgent extends DustAgent implements DustDevConsts {

	@Override
	public void init() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "init()");
	}
	
	@Override
	public void begin() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "begin()");
	}
	
	@Override
	public void process() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "process()");
	}
	
	@Override
	public void end(boolean commit) throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "end()", commit);
	}
	
	@Override
	public void release() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_NAME);
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, name, "release()");
	}
}
