package me.giskard.dust.core.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.api.DustException;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineBoot implements DustGenBootConsts, DustUtilsConsts {

	private static DustMachineIdea iMACHINE;
	private static String memId;
	
	static {
		String launchTime = DustUtils.strTime();
		memId = "DustMachine." + launchTime + "_" + DustUtils.getNewId(DUST_DEF_ID_BYTES);

		iMACHINE = new DustMachineIdea();
		DustMachineHandle mh = new DustMachineHandle(iMACHINE, null, null, memId);
		iMACHINE.mh = mh;
	}

	private static final DustCreator<DustMachineHandle> handleCreator = new DustCreator<DustMachineHandle>() {
		public DustMachineHandle create(Object key, Object... hints) {
			DustMachineIdea u = DustUtils.optGet(hints, 0, null);
			DustMachineHandle t = DustUtils.optGet(hints, 1, null);
			DustMachineHandle h = new DustMachineHandle(iMACHINE, u, t, (String) key);
			return h;
		}
	};

	private static DustUtilsFactory<String, DustMachineHandle> HANDLE_MAP = new DustUtilsFactory<String, DustMachineHandle>(handleCreator, true);

	public DustMachineBoot() throws Exception {
		Set set;
		ArrayList arr;

		Map<String, String> bootTokens = DustDevUtils.loadConstHandles(DustGenBootConsts.class.getName());

		for (String tn : bootTokens.keySet()) {
			HANDLE_MAP.get(tn);
		}

		DustMachineHandle hUnitHandles = HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_HANDLES);

		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_ID), memId);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_TYPE), HANDLE_MAP.get(TOKEN_DUST_ASP_MACHINE));
		iMACHINE.content.put(hUnitHandles, new TreeMap());
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS), new HashMap());

		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_PLATFORM), HANDLE_MAP.get(TOKEN_DUST_IDEA_JAVA17));

		DustMachineHandle hThread = getMachineHandle(TOKEN_DUST_ASP_THREAD);
		set = new TreeSet();
		set.add(hThread);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLTHREADS), set);

		DustMachineHandle hDialog = getMachineHandle(TOKEN_DUST_ASP_DIALOG);
		set = new TreeSet();
		set.add(hDialog);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLDIALOGS), set);

		DustMachineIdea iThread = getIdea(hThread);
		iThread.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_DIALOG), hDialog);

		DustMachineHandle hApp = getMachineHandle(TOKEN_DUST_ASP_APPLICATION);
		set = new TreeSet();
		set.add(hApp);
		iMACHINE.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_ALLAPPLICATIONS), set);

		DustMachineIdea iDialog = getIdea(hDialog);
		iDialog.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_APPLICATION), hApp);

		DustMachineIdea iApp = getIdea(hApp);
		Map unitHandles = new TreeMap();
		iApp.content.put(hUnitHandles, unitHandles);
		iApp.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS), new HashMap());

		DustMachineHandle hMsg = getMachineHandle(TOKEN_MIND_ASP_MESSAGE);
		DustMachineIdea iMsg = getIdea(hMsg);
		iMsg.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_CMD), HANDLE_MAP.get(TOKEN_MISC_TAG_CMD_INIT));
		arr = new ArrayList();
		arr.add(iMACHINE.mh);
		iMsg.content.put(HANDLE_MAP.get(TOKEN_MIND_ATT_LISTENERS), arr);

		DustMachineHandle hCtx = getMachineHandle(TOKEN_DUST_ASP_CALL_CONTEXT);
		DustMachineIdea iCtx = getIdea(hCtx);

		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_APP), iApp);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_DLG), iDialog);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_AGT), iMACHINE);
		iCtx.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CTX_MSG), iMsg);

		arr = new ArrayList();
		arr.add(iCtx);
		iThread.content.put(HANDLE_MAP.get(TOKEN_DUST_ATT_CALL_STACK), arr);

		for (String key : HANDLE_MAP.keys()) {
			String[] kk = DustUtils.splitId(key);

			DustMachineHandle hUnit = DustUtils.safeGet(unitHandles, handleCreator, kk[0], iApp, HANDLE_MAP.get(TOKEN_MIND_ASP_UNIT));
			DustMachineIdea iUnit = getIdea(hUnit);
			Map uh = (Map) iUnit.content.get(hUnitHandles);

			DustMachineHandle bh = HANDLE_MAP.get(key);

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
			case "IDEA":
				// I don't know the type for the token constant
				break;
			default:
				DustException.wrap(null, "Should not be here");
				break;
			}
		}
	}

	static DustMachineHandle getMachineHandle(String typeName) {
		DustMachineHandle hType = HANDLE_MAP.get(typeName);
		return getHandle(iMACHINE, hType, null);
	}

	static DustMachineHandle getHandle(DustMachineIdea iUnit, DustMachineHandle hType, String id) {
		Map handles = (Map) iUnit.content.get(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_HANDLES));

		if ( DustUtils.isEmpty(id)) {
			id = DustUtils.getNewId(handles.keySet(), DUST_DEF_ID_BYTES);
		}
		return DustUtils.safeGet(handles, handleCreator, id, iUnit, hType);
	}

	
	public static <RetType> RetType getMachineData(String token) {
		return (RetType) iMACHINE.content.get(HANDLE_MAP.get(token));
	}

	
	public static DustMachineHandle getTokenHandle(String token) {
		return HANDLE_MAP.get(token);
	}

	public static DustMachineIdea getIdea(DustMachineHandle handle) {
		Map ideas = (Map) handle.unit.content.get(HANDLE_MAP.get(TOKEN_DUST_ATT_UNIT_OBJECTS));
		DustMachineIdea ret = (DustMachineIdea) ideas.get(handle);

		if (null == ret) {
			ret = new DustMachineIdea(handle);
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
