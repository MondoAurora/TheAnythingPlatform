package me.giskard.dust.core.dev;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

public class DustDevDumpAgent extends DustAgent implements DustDevConsts {

	@Override
	protected void init() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_NAME);
		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, name, "init()");
	}
	
	@Override
	protected Object begin() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_NAME);
		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, name, "begin()");
		return null;
	}
	
	@Override
	protected Object process(DustAccess access) throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_NAME);
		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, name, "process()", access);
		return null;
	}
	
	@Override
	protected Object end(boolean commit) throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_NAME);
		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, name, "end()", commit);
		return null;
	}
	
	@Override
	protected void release() throws Exception {
		DustHandle name = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_NAME);
		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, name, "release()");
	}
}
