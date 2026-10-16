package me.giskard.dust.api;

public abstract class DustMachine extends DustAgent implements DustConsts {
	
	public interface Provider {
		DustMachine getMachine();
	}
	
	protected abstract DustHandle getHandle(String id, DustHandle type);

	protected abstract <RetType> RetType access(DustAccess access, Object val, Object root, Object... path);
	
	protected abstract void manageThread(DustThreadAction action, Object anchor);

}