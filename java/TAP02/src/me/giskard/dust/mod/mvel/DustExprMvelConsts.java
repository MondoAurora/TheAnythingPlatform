package me.giskard.dust.mod.mvel;

import gen.handles.giskard_me.DustGenHandles_misc_1;
import gen.handles.giskard_me.DustGenHandles_text_1;
import me.giskard.dust.api.DustConsts;

public interface DustExprMvelConsts extends DustConsts, DustGenHandles_misc_1, DustGenHandles_text_1 {

	public interface MvelDataWrapper {
		Number getNum(String conceptId);
		boolean exists(Object key);
		Object get(String key);
	}

	
	public interface MvelUtilsGen {
		boolean exists(Object key);
	}
	
}
