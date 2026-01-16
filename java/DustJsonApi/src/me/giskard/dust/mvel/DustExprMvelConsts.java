package me.giskard.dust.mvel;

import me.giskard.dust.DustConsts;

public interface DustExprMvelConsts extends DustConsts {

	public interface MvelDataWrapper {
		Number getNum(String conceptId);
		boolean exists(Object key);
		Object get(String key);
	}

	
	public interface MvelUtilsGen {
		boolean exists(Object key);
	}
	
}
