package me.giskard.dust;

import java.io.File;

public abstract class DustMind extends DustConsts.DustAgent implements DustConsts {
	protected abstract DustHandle bootLoadAppUnit(DustHandle appUnit, File f) throws Exception;
	protected abstract DustHandle getUnit(String unitId, boolean createIfMissing);
	protected abstract boolean releaseUnit(DustHandle unit);

	protected abstract DustHandle getHandle(DustHandle unit, DustHandle type, String id, DustOptCreate optCreate);
	protected abstract Object getContent(DustHandle ob);
	protected abstract Object checkAccess(DustHandle agent, DustAccess acess, DustHandle object, DustHandle att, Object value) throws RuntimeException;

}