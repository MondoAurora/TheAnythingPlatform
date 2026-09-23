package me.giskard.dust.core.machine;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.core.DustMachine;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineNewAgent extends DustMachine implements DustMachineConsts {

	static ThreadLocal<DustMachineNewIdea> THREADS = new ThreadLocal<DustMachineNewIdea>() {
		public void set(DustMachineNewIdea value) {
//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SET thread context", Thread.currentThread(), value);
			super.set(value);
		};
	};

	public DustMachineNewAgent() {
		Set<DustMachineNewHandle> threads = DustUtils.simpleGet(DustMachineBoot.iMACHINE.content, DustMachineBoot.HANDLE_MAP.get(TOKEN_DUST_ATT_ALLTHREADS));
		DustMachineNewIdea iBootThread = DustMachineBoot.getIdea(threads.iterator().next());
		THREADS.set(iBootThread);
	}
	
	Map<DustMachineNewHandle, Object> getCtx() {
		return null;
	}

	@Override
	protected DustHandle getUnit(String unitId, boolean createIfMissing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean releaseUnit(DustHandle unit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected DustHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DustHandle bootLoadAppUnit(DustHandle appUnit, String unitId, InputStream is, Bootloader bootLoader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <RetType> RetType optGetCtx(Object in, boolean createMissing) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <RetType> RetType notifyAgent(DustHandle hAgent, DustAction action, DustAccess access, DustHandle hMessage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
