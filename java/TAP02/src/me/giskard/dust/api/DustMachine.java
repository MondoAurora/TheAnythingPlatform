package me.giskard.dust.api;

public abstract class DustMachine extends DustAgent implements DustConsts {
	
	protected abstract DustHandle getHandle(DustHandle unit, Object type, String id, boolean createIfMissing);
	protected abstract DustHandle getHandle(String idd, boolean createIfMissing);

	protected abstract <RetType> RetType access(DustAccess access, Object val, Object root, Object... path);

}