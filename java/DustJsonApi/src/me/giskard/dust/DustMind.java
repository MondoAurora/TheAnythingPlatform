package me.giskard.dust;

import java.io.File;

public abstract class DustMind extends DustConsts.DustAgent implements DustConsts {
	protected abstract DustObject getObject(DustObject unit, DustObject type, String id, DustOptCreate optCreate);
	protected abstract DustObject getUnit(String unitId, boolean createIfMissing);
	protected abstract boolean releaseUnit(String unitId);
	protected abstract Object checkAccess(DustObject agent, DustAccess acess, DustObject object, DustObject att, Object value) throws RuntimeException;
	
	protected abstract DustObject bootLoadAppUnitJsonApi(DustObject appUnit, File f) throws Exception;
}