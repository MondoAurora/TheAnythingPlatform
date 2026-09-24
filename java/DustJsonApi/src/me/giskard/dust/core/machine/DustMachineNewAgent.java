package me.giskard.dust.core.machine;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.DustMachine;
import me.giskard.dust.core.stream.DustStreamJsonApiAgent;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineNewAgent extends DustMachine implements DustGenBootConsts {

	static ThreadLocal<DustMachineNewIdea> THREADS = new ThreadLocal<DustMachineNewIdea>() {
		public void set(DustMachineNewIdea value) {
//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SET thread context", Thread.currentThread(), value);
			super.set(value);
		};
	};

	DustMachineNewHandle hAspUnit;
	DustMachineNewHandle hAspAsp;

	DustMachineNewHandle hAttCallStack;

	DustMachineNewHandle hAttCtxApp;
	DustMachineNewHandle hAttCtxDlg;
	DustMachineNewHandle hAttCtxAgt;
	DustMachineNewHandle hAttCtxMsg;

	DustMachineNewHandle hAttUnitHandles;
	DustMachineNewHandle hAttUnitObjects;
	DustMachineNewHandle hAttUnitState;

	DustMachineNewHandle hTagStateInSync;

	public DustMachineNewAgent() {
		Set<DustMachineNewHandle> threads = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLTHREADS);
		DustMachineNewIdea iBootThread = DustMachineBoot.getIdea(threads.iterator().next());
		THREADS.set(iBootThread);

		hAspUnit = DustMachineBoot.getTokenHandle(TOKEN_MIND_ASP_UNIT);
		hAspAsp = DustMachineBoot.getTokenHandle(TOKEN_MIND_ASP_ASPECT);

		hAttCallStack = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_CALL_STACK);

		hAttCtxApp = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_CTX_APP);
		hAttCtxDlg = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_CTX_DLG);
		hAttCtxAgt = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_CTX_AGT);
		hAttCtxMsg = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_CTX_MSG);

		hAttUnitHandles = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_UNIT_HANDLES);
		hAttUnitObjects = DustMachineBoot.getTokenHandle(TOKEN_DUST_ATT_UNIT_OBJECTS);
		hAttUnitState = DustMachineBoot.getTokenHandle(TOKEN_MIND_ATT_UNIT_STATE);

		hTagStateInSync = DustMachineBoot.getTokenHandle(TOKEN_MISC_TAG_STATE_IN_SYNC);
	}

	DustMachineNewIdea getCtx(Object key) {
		DustMachineNewIdea iCtx = DustUtils.simpleGet(THREADS.get().content, hAttCallStack, 0);
		return (DustMachineNewIdea) iCtx.content.get(key);
	}

	@Override
//protected DustMachineNewHandle getUnit(String unitId, boolean createIfMissing) {
	public synchronized DustMachineNewHandle getUnit(String unitId, boolean createIfMissing) {
		DustMachineNewIdea iApp = getCtx(hAttCtxApp);

		DustMachineNewHandle hUnit = getHandleInt(iApp, hAspUnit, unitId, createIfMissing);

//		DustMachineNewHandle hUnit = DustUtils.simpleGet(iApp.content, hAttUnitHandles, unitId);
//
//		if (createIfMissing && (null == hUnit)) {
//			Dust.log("boot", "Would create unit", unitId);
//		}

		return hUnit;
	}

	@Override
	protected boolean releaseUnit(DustHandle unit) {
		// TODO Auto-generated method stub
		return false;
	}

	protected boolean syncUnits() {
		boolean ret = false;

		DustMachineNewIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineNewHandle, DustMachineNewIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);

		for (boolean chg = true; chg;) {
			chg = false;
			for (Map.Entry<DustMachineNewHandle, DustMachineNewIdea> eu : unitMap.entrySet()) {
				DustMachineNewIdea iUnit = eu.getValue();

				if (hTagStateInSync != iUnit.content.get(hAttUnitState)) {
					DustMachineNewHandle hUnit = eu.getKey();

					loadUnit(hUnit, iUnit);

					chg = true;
					ret = true;

					break;
				}
			}
		}

		return ret;
	}

	public void loadUnit(DustMachineNewHandle handle, DustMachineNewIdea idea) {
		Dust.log("boot", "======== loading unit", handle, "============");

		try (FileInputStream fis = new FileInputStream("localStore/" + handle.id + DUST_EXT_JSON)) {
			DustStreamJsonApiAgent.loadUnit(handle, fis);
			idea.content.put(hAttUnitState, hTagStateInSync);
		} catch (Throwable e) {
			DustException.wrap(e, "loading unit", handle.id);
		}
	}

	@Override
//	protected DustHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
	public synchronized DustHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
		boolean createIfMissing = DustOptCreate.None != optCreate;
		DustMachineNewHandle hUnit = (DustMachineNewHandle) unit;

		String[] ii = DustUtils.splitId(id);
		if (null != ii[0]) {
			hUnit = getUnit(ii[0], createIfMissing);
		}
		DustMachineNewIdea iUnit = DustMachineBoot.getIdea(hUnit);

		DustMachineNewHandle hType = (null == type) ? null
				: (type instanceof DustMachineNewHandle) ? (DustMachineNewHandle) type : getHandleInt(null, hAspAsp, (String) type, true);

		DustMachineNewHandle hRet = getHandleInt(iUnit, hType, id, createIfMissing);

		return hRet;
	}

	DustMachineNewHandle getHandleInt(DustMachineNewIdea iUnit, DustMachineNewHandle type, String id, boolean createIfMissing) {
		if (null == iUnit) {
			String[] ii = DustUtils.splitId(id);
			if (null != ii[0]) {
				DustMachineNewHandle hUnit = getUnit(ii[0], createIfMissing);
				iUnit = DustMachineBoot.getIdea(hUnit);
			}
		}
		
		DustMachineNewHandle hRet = DustUtils.simpleGet(iUnit.content, hAttUnitHandles, id);

		if (createIfMissing && (null == hRet)) {
			if ("giskard.me/mind.1$Attribute".equals(id)) {
				Dust.log("hmmm", id);
			}
			hRet = DustMachineBoot.getHandle(iUnit, type, id);
			Dust.log("boot", "Handle created", hRet);
		}

		return hRet;
	}

	@Override
	protected DustHandle bootLoadAppUnit(DustHandle appUnit, String unitId, InputStream is, Bootloader bootLoader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {

//		Dust.log("boot", "access", access, val, root, path);
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
	public void init() throws Exception {
//	protected void init() throws Exception {
		syncUnits();
	}

	@Override
	public Object process(DustAccess access) throws Exception {
//		protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
