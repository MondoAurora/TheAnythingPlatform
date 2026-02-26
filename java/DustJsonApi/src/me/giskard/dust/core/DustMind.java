package me.giskard.dust.core;

import java.io.File;

public abstract class DustMind extends DustConsts.DustAgent implements DustConsts {
	
	public interface Bootloader {
		void loadFile(DustHandle unit, File f) throws Exception;
	}
	
	protected abstract DustHandle bootLoadAppUnit(DustHandle appUnit, File f, Bootloader bootLoader) throws Exception;

	protected abstract DustHandle getHandle(DustHandle unit, DustHandle type, String id, DustOptCreate optCreate);
	protected abstract DustHandle getUnit(String unitId, boolean createIfMissing);
	protected abstract boolean releaseUnit(DustHandle unit);

	protected abstract <RetType> RetType access(DustAccess access, Object val, Object root, Object... path);
	@Deprecated
	protected abstract <RetType> RetType optGetCtx(Object in);
	@Deprecated
	protected abstract <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path);

	protected abstract <RetType> RetType notifyAgent(DustHandle hAgent, DustAction action, DustAccess access, DustHandle service, Object params);
	protected <RetType> RetType callAgent(DustHandle hAgent, DustAction action, DustAccess access) throws Exception {
		return Dust.callAgent(hAgent, action, access);
	}
}