package me.giskard.dust.core.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineBoot implements DustGenBootConsts, DustUtilsConsts {

	private static DustMachineNewIdea iMACHINE;

	private static Map<String, DustMachineNewHandle> HANDLE_MAP = new TreeMap<>();

	private static final DustCreator<DustMachineNewHandle> handleCreator = new DustCreator<DustMachineNewHandle>() {
		public DustMachineNewHandle create(Object key, Object... hints) {
			DustMachineNewIdea u = DustUtils.optGet(hints, 0, null);
			DustMachineNewHandle t = DustUtils.optGet(hints, 1, null);
			DustMachineNewHandle h = new DustMachineNewHandle(iMACHINE, u, t, (String) key);
			return h;
		}
	};

	public DustMachineBoot() throws Exception {
		Set set;
		ArrayList arr;

		String launchTime = DustUtils.strTime();
		String memId = "DustMachine." + launchTime + "_" + DustUtils.getNewId(DUST_DEF_ID_BYTES);

		iMACHINE = new DustMachineNewIdea();
		DustMachineNewHandle mh = new DustMachineNewHandle(iMACHINE, null, null, memId);
		iMACHINE.mh = mh;

		Map<String, String> bootTokens = DustDevUtils.loadConstHandles(DustGenBootConsts.class.getName());

		for (String tn : bootTokens.keySet()) {
			HANDLE_MAP.put(tn, new DustMachineNewHandle(null, iMACHINE, null, tn));
		}

		DustMachineNewHandle hUnitHandles = HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_HANDLES);

		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_ID), memId);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_TYPE), HANDLE_MAP.get(TOKEN_DUST_ASP_MACHINE));
		iMACHINE.content.put(hUnitHandles, new TreeMap());
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS), new HashMap());

		DustMachineNewHandle hThread = getMachineHandle(TOKEN_DUST_ASP_THREAD);
		set = new TreeSet();
		set.add(hThread);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLTHREADS), set);

		DustMachineNewHandle hDialog = getMachineHandle(TOKEN_DUST_ASP_DIALOG);
		set = new TreeSet();
		set.add(hDialog);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLDIALOGS), set);

		DustMachineNewIdea iThread = getIdea(hThread);
		iThread.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_DIALOG), hDialog);

		DustMachineNewHandle hApp = getMachineHandle(TOKEN_DUST_ASP_APPLICATION);
		set = new TreeSet();
		set.add(hApp);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLAPPLICATIONS), set);

		DustMachineNewIdea iDialog = getIdea(hDialog);
		iDialog.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_APPLICATION), hApp);

		DustMachineNewIdea iApp = getIdea(hApp);
		Map unitHandles = new TreeMap();
		iApp.content.put(hUnitHandles, unitHandles);
		iApp.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS), new HashMap());

		DustMachineNewHandle hMsg = getMachineHandle(TOKEN_MIND_ASP_MESSAGE);
		DustMachineNewIdea iMsg = getIdea(hMsg);
		iMsg.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_CMD), HANDLE_MAP.get(TOKEN_MISC_TAG_CMD_INIT));
		arr = new ArrayList();
		arr.add(mh);
		iMsg.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_LISTENERS), arr);

		DustMachineNewHandle hCtx = getMachineHandle(TOKEN_DUST_ASP_CALL_CONTEXT);
		DustMachineNewIdea iCtx = getIdea(hCtx);

		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_APP), iApp);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_DLG), iDialog);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_AGT), iMACHINE);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_MSG), iMsg);

		arr = new ArrayList();
		arr.add(iCtx);
		iThread.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CALL_STACK), arr);

		for (Map.Entry<String, DustMachineNewHandle> ech : HANDLE_MAP.entrySet()) {
			String key = ech.getKey();

			String[] kk = DustUtils.splitId(key);

			DustMachineNewHandle hUnit = DustUtils.safeGet(unitHandles, handleCreator, kk[0], iApp, HANDLE_MAP.get(TOKEN_MIND_ASP_UNIT));
			DustMachineNewIdea iUnit = getIdea(hUnit);
			Map uh = (Map) iUnit.content.get(hUnitHandles);

			DustMachineNewHandle bh = ech.getValue();

			uh.put(key, bh);
			bh.unit = iUnit;

			String tp[] = bootTokens.getOrDefault(key, "").split("_");

			switch (DustUtils.optGet(tp, 2, "")) {
			case "ASP":
				bh.type = HANDLE_MAP.get(TOKEN_MIND_ASP_ASPECT);
				break;
			case "ATT":
				bh.type = HANDLE_MAP.get(TOKEN_MIND_ASP_ATTRIBUTE);
				break;
			case "TAG":
				bh.type = HANDLE_MAP.get(TOKEN_MIND_ASP_TAG);
				break;
			case "AGT":
				bh.type = HANDLE_MAP.get(TOKEN_MIND_ASP_AGENT);
				break;
			default:
				DustException.wrap(null, "Should not be here");
				break;
			}
		}
	}

	static DustMachineNewHandle getMachineHandle(String typeName) {
		DustMachineNewHandle hType = HANDLE_MAP.get(typeName);
		return getHandle(iMACHINE, hType, null);
	}

	static DustMachineNewHandle getHandle(DustMachineNewIdea iUnit, DustMachineNewHandle hType, String id) {
		Map handles = (Map) iUnit.content.get(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_HANDLES));

		if ( DustUtils.isEmpty(id)) {
			id = DustUtils.getNewId(handles.keySet(), DUST_DEF_ID_BYTES);
		}
		return DustUtils.safeGet(handles, handleCreator, id, iUnit, hType);
	}

	
	public static <RetType> RetType getMachineData(String token) {
		return (RetType) iMACHINE.content.get(HANDLE_MAP.get(token));
	}

	
	public static DustMachineNewHandle getTokenHandle(String token) {
		return HANDLE_MAP.get(token);
	}

	public static DustMachineNewIdea getIdea(DustMachineNewHandle handle) {
		Map ideas = (Map) handle.unit.content.get(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS));
		DustMachineNewIdea ret = (DustMachineNewIdea) ideas.get(handle);

		if (null == ret) {
			ret = new DustMachineNewIdea(handle);
			ret.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_ID), handle.id);
			ret.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_TYPE), handle.type);
			ret.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_UNIT), handle.unit);

			if (handle.type == HANDLE_MAP.get(TOKEN_MIND_ASP_UNIT)) {
				ret.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_HANDLES), new TreeMap());
				ret.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS), new HashMap());
			}

			ideas.put(handle, ret);
		}

		return ret;
	}

}
