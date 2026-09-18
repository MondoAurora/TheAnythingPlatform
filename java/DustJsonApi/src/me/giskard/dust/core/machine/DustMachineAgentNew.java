package me.giskard.dust.core.machine;

import java.io.InputStream;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMachineAgentNew extends DustMachineConsts.DustMachineImpl implements DustMachineConsts {

	public DustMachineAgentNew() {
	}

	@Override
	protected DustHandle bootLoadAppUnit(DustHandle appUnit, String unitId, InputStream is, Bootloader bootLoader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DustHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
		// TODO Auto-generated method stub
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

	@Override
	Map getContent(DustHandle h) {
		// TODO Auto-generated method stub
		return null;
	}
}
