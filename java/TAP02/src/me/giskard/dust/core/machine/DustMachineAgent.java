package me.giskard.dust.core.machine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.api.DustMachine;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.core.utils.DustUtilsJsonApi;
import me.giskard.handles.giskard_me.DustGenHandles_dust_1;
import me.giskard.handles.giskard_me.DustGenHandles_mind_1;
import me.giskard.handles.giskard_me.DustGenHandles_misc_1;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineAgent extends DustMachine
		implements DustGenBootConsts, DustUtilsConsts, DustGenHandles_mind_1, DustGenHandles_dust_1, DustGenHandles_misc_1 {

	static ThreadLocal<DustMachineIdea> THREADS = new ThreadLocal<DustMachineIdea>() {
		public void set(DustMachineIdea value) {
//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SET thread context", Thread.currentThread(), value);
			super.set(value);
		};
	};

	DustMachineHandle hAspUnit;
	DustMachineHandle hAspAsp;

	DustMachineHandle hAttCallStack;

	DustMachineHandle hAttCtxApp;
	DustMachineHandle hAttCtxDlg;
	DustMachineHandle hAttCtxAgt;
	DustMachineHandle hAttCtxMsg;

	DustMachineHandle hAttUnitHandles;
	DustMachineHandle hAttUnitObjects;
	DustMachineHandle hAttUnitState;

	DustMachineHandle hTagStateInSync;

	public DustMachineAgent() {
		Set<DustMachineHandle> threads = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLTHREADS);
		DustMachineIdea iBootThread = DustMachineBoot.getIdea(threads.iterator().next());
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

	DustMachineIdea getCtx(Object key) {
		DustMachineIdea iCtx = DustUtils.simpleGet(THREADS.get().content, hAttCallStack, 0);
		return (DustMachineIdea) iCtx.content.get(key);
	}

	public synchronized DustMachineHandle getUnit(String unitId, boolean createIfMissing) {
		DustMachineIdea iApp = getCtx(hAttCtxApp);

		DustMachineHandle hUnit = getHandleInt(iApp, hAspUnit, unitId, createIfMissing);

		return hUnit;
	}

	public Collection<DustHandle> getUnits() {
		DustMachineIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineHandle, DustMachineIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);

		return new TreeSet<DustHandle>(unitMap.keySet());
	}

	protected boolean syncUnits(boolean load) throws Exception {
		boolean ret = false;

		DustMachineIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineHandle, DustMachineIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);

		if (load) {
			for (boolean chg = true; chg;) {
				chg = false;
				for (Map.Entry<DustMachineHandle, DustMachineIdea> eu : unitMap.entrySet()) {
					DustMachineIdea iUnit = eu.getValue();
					DustMachineHandle hUnit = eu.getKey();

					if (hTagStateInSync != iUnit.content.get(hAttUnitState)) {

						loadUnit(hUnit, iUnit);

						chg = true;
						ret = true;

						break;
					}
				}
			}

//			if (!ret) 
			{
				Set<DustMachineHandle> uhs = new TreeSet<DustMachineHandle>(unitMap.keySet());
				for (DustMachineHandle hUnit : uhs) {
					DustMachineIdea iUnit = unitMap.get(hUnit);

					Map<DustMachineHandle, DustMachineIdea> ideaMap = DustUtils.simpleGet(iUnit.content, hAttUnitObjects);
					Map<String, DustMachineHandle> handleMap = DustUtils.simpleGet(iUnit.content, hAttUnitHandles);
					Dust.log(null, "boot", "unchanged", hUnit, handleMap.size(), ideaMap.size());

					for (Map.Entry<String, DustMachineHandle> ehm : handleMap.entrySet()) {
						DustMachineHandle h = ehm.getValue();

						if (null == h.type) {
							Dust.log(null, "no type for", ehm.getKey());
						}
						if (!ideaMap.containsKey(h)) {
							Dust.log(null, "no idea for", ehm.getKey());
						}
					}
				}
			}
		} else {
			for (Map.Entry<DustMachineHandle, DustMachineIdea> eu : unitMap.entrySet()) {
				DustMachineIdea iUnit = eu.getValue();
				DustMachineHandle hUnit = eu.getKey();

//				if (hTagStateInSync != iUnit.content.get(hAttUnitState)) 
				{

					saveUnit(hUnit, iUnit);

					ret = true;

//					break;
				}
			}

		}

		return ret;
	}

	public void saveUnit(DustMachineHandle handle, DustMachineIdea idea) throws Exception {

		if (handle.id.contains("sandbox")) {
			Dust.log(null, "test", "SKIP", handle);
			return;
		}

		Dust.log(null, "test", "======== saving unit", handle, "============");

//		File f = new File("tmp/ls1/" + handle.id + DUST_EXT_JSON);
//		DustUtilsFile.ensureDir(f.getParent());
//
//		try (FileOutputStream fos = new FileOutputStream(f)) {

		try (FileOutputStream fos = new FileOutputStream("localStore/" + handle.id + DUST_EXT_JSON)) {
			DustUtilsJsonApi.storeUnit(handle, fos);
//			idea.content.put(hAttUnitState, hTagStateInSync);
		} catch (Throwable e) {
			DustException.wrap(e, "saving unit", handle.id);
		}
	}

	public void loadUnit(DustMachineHandle handle, DustMachineIdea idea) {
		Dust.log(null, "boot", "======== loading unit", handle, "============");

		try (FileInputStream fis = new FileInputStream("localStore/" + handle.id + DUST_EXT_JSON)) {
			DustUtilsJsonApi.loadUnit(handle, fis);
			idea.content.put(hAttUnitState, hTagStateInSync);
		} catch (Throwable e) {
			DustException.wrap(e, "loading unit", handle.id);
		}
	}

	@Override
	protected DustHandle getHandle(String idd, boolean createIfMissing) {
		return getHandle(null, null, idd, createIfMissing);
	}

	@Override
	public synchronized DustHandle getHandle(DustHandle unit, Object type, String id, boolean createIfMissing) {
//	public synchronized DustHandle getHandle(DustHandle unit, Object type, String id, boolean createIfMissing) {
		DustMachineHandle hUnit = (DustMachineHandle) unit;

		String[] ii = DustUtils.splitId(id);
		if (null != ii[0]) {
			hUnit = getUnit(ii[0], createIfMissing);
		}
		DustMachineIdea iUnit = DustMachineBoot.getIdea(hUnit);

		if (null == type) {
			Dust.log(null, "getHandle", "null type", id);
		}

		DustMachineHandle hType = (null == type) ? null
				: (type instanceof DustMachineHandle) ? (DustMachineHandle) type
						: getHandleInt(null, hAspAsp, (String) type, true);

		DustMachineHandle hRet = getHandleInt(iUnit, hType, id, createIfMissing);

		return hRet;
	}

	DustMachineHandle getHandleInt(DustMachineIdea iUnit, DustMachineHandle type, String id, boolean createIfMissing) {
		if (null == iUnit) {
			String[] ii = DustUtils.splitId(id);
			if (null != ii[0]) {
				DustMachineHandle hUnit = getUnit(ii[0], createIfMissing);
				iUnit = DustMachineBoot.getIdea(hUnit);
			}
		}

		DustMachineHandle hRet = DustUtils.simpleGet(iUnit.content, hAttUnitHandles, id);

		if (null == hRet) {
			if (createIfMissing) {
				hRet = DustMachineBoot.getHandle(iUnit, type, id);
				Dust.log(null, "boot", "Handle created", hRet);
			}
		} else {
			if ((null == hRet.type) && (null != type)) {
				hRet.type = type;
			}
		}

		return hRet;
	}

	Map getContent(DustMachineHandle h) {
		DustMachineIdea idea = DustMachineBoot.getIdea(h);
		return idea.getContent();
	}

	private Object checkAccess(DustMachineIdea iAgt, DustAccess acess, DustMachineIdea iTarget, DustMachineHandle hAtt,
			Object lastKey, Object valPrev, Object valNew) {
		Object ret = valNew;

		if ((null != iTarget) && (null != hAtt)) {
			boolean pass = true;

			if (pass && DustUtils.isChange(acess)) {
				iTarget.mh.unit.content.put(hAttUnitState, hTagStateInSync);
			}
		}

		return ret;
	}

	DustHandle[] CTXS;

	@Override
	protected <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		Object ret = null;
		int pidx = 0;
		boolean create = DustUtils.isCreate(access);

		DustCollType collType = DustUtils.getCollType(root);

//		Dust.log(null, "boot", "access", access, val, root, path);

		DustMachineIdea iAgt = getCtx(HANDLE_DUST_ATT_CTX_AGT);

		Object curr = root;
		DustMachineIdea iCurr = null;

		Object prev = null;
		Object lastKey = null;
		DustMachineHandle lastHandle = null;

		Object prevColl = null;
		DustMachineHandle prevHandle = (curr instanceof DustHandle) ? (DustMachineHandle) curr : null;
		DustMachineHandle prevAtt = null;

		/**
		 * Process the path
		 */

		if (null == curr) {
			if (path.length > 0) {
				Object p = path[0];
				curr = (-1 == DustUtils.indexOf(p, (Object[]) CTXS)) ? null : getCtx(p);
				if (null == curr) {
					for (DustHandle ch : CTXS) {
						DustMachineIdea ci = getCtx(ch);
						if (ci.content.containsKey(p)) {
							curr = ci;
							break;
						}
					}

					if (null == curr) {
						if (create) {
							curr = getCtx(HANDLE_DUST_ATT_CTX_MSG);
						}
					}
				} else {
					++pidx;
				}
			} else {
				switch ( access ) {
				case Delete:
					DustMachineHandle hh = (DustMachineHandle)val;
					Map m = hh.unit.content;
					((Map)m.get(HANDLE_DUST_ATT_UNIT_HANDLES)).remove(hh.id);
					((Map)m.get(HANDLE_DUST_ATT_UNIT_OBJECTS)).remove(hh);
					
					break;
				}
			}
		}

		for (; pidx < path.length; ++pidx) {
			Object p = path[pidx];

			if (p instanceof DustMachineHandle) {
				prevAtt = (DustMachineHandle) p;
			} else if (p instanceof Enum) {
				p = ((Enum) p).name();
			}

			if (curr instanceof DustMachineIdea) {
				iCurr = (DustMachineIdea) curr;
				curr = iCurr.getContent();
				collType = DustCollType.Map;
			} else if (curr instanceof DustMachineHandle) {
				lastHandle = prevHandle = (DustMachineHandle) curr;
				iCurr = DustMachineBoot.getIdea(prevHandle);
				curr = iCurr.getContent();
				collType = DustCollType.Map;
			} else if (null == curr) {
				if (create) {
					curr = (p instanceof Integer) ? new ArrayList() : new HashMap();

					if (null != prevColl) {
						curr = checkAccess(iAgt, DustAccess.Insert, iCurr, prevAtt, lastKey, null, curr);

						switch (collType) {
						case Arr:
							DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, false);
							break;
						case Map:
							((Map) prevColl).put(lastKey, curr);
							break;
						case One:
							break;
						case Set:
							((Set) prevColl).add(curr);
							break;
						}
					}
				} else {
					if (p instanceof Integer) {
						switch ((Integer) p) {
						case KEY_SIZE:
							curr = 0;
							break;
						case KEY_INDEXOF:
							curr = -1;
							break;
						}
					}
					break;
				}
				prevHandle = null;
			}

			prev = curr;
			collType = DustUtils.getCollType(prev);
			prevColl = (null == collType) ? null : prev;

			lastKey = p;

			if (curr instanceof ArrayList) {
				ArrayList al = (ArrayList) curr;
				Integer idx = (Integer) p;

				switch (idx) {
				case KEY_SIZE:
					curr = al.size();
					break;
				case KEY_ADD:
					curr = null;
					break;
				case KEY_INDEXOF:
					curr = al.indexOf(val);
					break;
				case KEY_MEMBEROF:
					lastKey = al.indexOf(val);
					break;
				default:
					curr = (idx < al.size()) ? al.get(idx) : null;
					break;
				}
			} else if (curr instanceof Map) {
				curr = DustUtils.isEqual(KEY_SIZE, p) ? ((Map) curr).size()
						: DustUtils.isEqual(KEY_MAP_KEYS, p) ? new ArrayList(((Map) curr).keySet()) : ((Map) curr).get(p);
			} else {
				curr = null;
			}

			curr = checkAccess(iAgt, create ? DustAccess.Get : DustAccess.Peek, iCurr, prevAtt, lastKey, null, curr);
		}

		/**
		 * Spec handle tags
		 */

		if (HANDLE_MIND_ATT_TAGS.equals(prevAtt)) {
			if (curr instanceof Collection) {
				for (DustHandle ht : (Collection<DustHandle>) curr) {
//					DustHandle ht = (o instanceof DustHandle) ? (DustHandle) o : getHandle(null, TOKEN_MIND_ASP_TAG, (String) o, DustOptCreate.None);
					switch (access) {
					case Peek:
//						if (DustUtils.isEqual(val, access(DustAccess.Peek, "", ht, TOKEN_MISC_ATT_PARENT))) {
						if (DustUtils.isEqual(val, access(DustAccess.Peek, "", ht, HANDLE_MISC_ATT_PARENT, TOKEN_MIND_ATT_ID))) {
							return (RetType) ht;
						}
						break;
					}
				}
			} else {
				switch (access) {
				case Peek:
					return null;
				}
			}
		}

		/**
		 * Admin change
		 */

		Boolean change = null;
		boolean itemDel = false;

		switch (access) {
		case Delete:
			if (curr != null) {
				if (null != val) {
					if (curr instanceof Collection) {
						Collection pc = (Collection) curr;
						change = pc.contains(val);
						itemDel = true;
					} else if (curr instanceof Map) {
						change = ((Map) curr).containsValue(val);
						itemDel = true;
					}
				}

				if (!itemDel) {
					switch (collType) {
					case Arr:
						int lk = (int) lastKey;
						change = (0 <= lk) && (lk < ((ArrayList) prevColl).size());
						break;
					case Map:
						change = ((Map) prevColl).containsKey(lastKey);
						break;
					case One:
						change = true;
						break;
					case Set:
						change = ((Set) prevColl).contains(curr);
						break;
					}
				}
			}

			break;
		case Insert:
			switch (collType) {
			case Arr:
				change = true;
				break;
			case Map:
				change = (curr instanceof Set) ? !((Set) curr).contains(val) : !DustUtils.isEqual(curr, val);
				break;
			case One:
				break;
			case Set:
				change = !((Set) prevColl).contains(val);
				break;
			}

			break;
		case Reset:
			if (curr instanceof Map) {
				change = !((Map) curr).isEmpty();
			} else if (curr instanceof Collection) {
				change = !((Collection) curr).isEmpty();
			}

			break;
		case Set:
			if ((null != lastKey) && (null != prevColl)) {
				switch (collType) {
				case Arr:
					change = !DustUtils.isEqual(curr, val);
					break;
				case Map:
					change = !DustUtils.isEqual(curr, val);
					break;
				case One:
					break;
				case Set:
					change = !((Set) prevColl).contains(val);
					break;
				}
			}
			break;
		default:
			break;
		}

		if (Boolean.TRUE.equals(change)) {
			checkAccess(iAgt, access, iCurr, prevAtt, lastKey, curr, val);
		}

		/**
		 * Do the job
		 */

		switch (access) {
		case Check:

			boolean match = DustUtils.isEqual(val, curr);
			if (!match && (curr instanceof Collection)) {
				match = ((Collection) curr).contains(val);
			}
			ret = match;

			break;
		case Delete:
			if (curr != null) {
				if (itemDel) {
					if (curr instanceof Collection) {
						((Collection) curr).remove(val);
					} else if (curr instanceof Map) {
						((Map) curr).values().remove(val);
					}
				} else {
					switch (collType) {
					case Arr:
						((ArrayList) prevColl).remove((int) lastKey);
						break;
					case Map:
						((Map) prevColl).remove(lastKey);
						break;
					case One:
						break;
					case Set:
						((Set) prevColl).remove(val);
						break;
					}
				}
			}
			ret = curr;

			break;
		case Get:
			ret = (null == curr) ? val : curr;
			break;
		case Insert:
			if (!DustUtils.isEqual(curr, val) && (null != prevColl)) {
				switch (collType) {
				case Arr:
					DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, false);
					break;
				case Map:
					if (curr instanceof Set) {
						ret = ((Set) curr).add(val);
					} else {
						Set s = new HashSet<>();
						((Map) prevColl).put(lastKey, s);
						ret = s.add(val);
					}
					break;
				case One:
					break;
				case Set:
					ret = ((Set) prevColl).add(val);
					break;
				}
			}
			break;
		case Peek:
			if (collType == DustCollType.Set) {
				Iterator is = ((Set) prevColl).iterator();
				if (is.hasNext()) {
					curr = is.next();
				}
			}
			ret = (null == curr) ? val : curr;
			break;
		case Reset:
			if (curr instanceof Map) {
				((Map) curr).clear();
			} else if (curr instanceof Collection) {
				((Collection) curr).clear();
			}
			break;
		case Set:
			ret = curr;
			if ((null != lastKey) && (null != prevColl)) {
				switch (collType) {
				case Arr:
					DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, true);
					break;
				case Map:
					if (!DustUtils.isEqual(curr, val)) {
						((Map) prevColl).put(lastKey, val);

						int specIdx = DustUtils.indexOf(lastKey, TOKEN_MIND_ATT_TYPE, TOKEN_MIND_ATT_ID, TOKEN_MIND_ATT_UNIT);
						if (-1 == specIdx) {
							specIdx = DustUtils.indexOf(lastKey, HANDLE_MIND_ATT_TYPE, HANDLE_MIND_ATT_ID, HANDLE_MIND_ATT_UNIT);
						}
						switch (specIdx) {
						case 0:
							((DustMachineHandle) lastHandle).type = (DustMachineHandle) val;
							break;
						case 1:
							((DustMachineHandle) lastHandle).id = (String) val;
							break;
						case 2:
							((DustMachineHandle) lastHandle).unit = DustMachineBoot.getIdea((DustMachineHandle) val);
							break;
						}
					}
					break;
				case One:
					break;
				case Set:
					((Set) prevColl).add(val);
					break;
				}
			}

			break;
		case Visit:
			if (curr == null) {
				ret = (null == val) ? NOT_FOUND : val;
			} else {
				switch (DustUtils.getCollType(curr)) {
				case Arr:
				case Set:
					ret = curr;
					break;
				case Map:
					ret = ((Map) curr).entrySet();
					break;
				case One:
					ret = null;
					break;
				}
			}
			break;
		case Commit:
			Object ll = access(DustAccess.Peek, null, curr, HANDLE_MIND_ATT_LISTENERS);
			for (DustHandle l : (Collection<DustHandle>) ll) {
				ret = notifyAgent((DustHandle) l, access, (DustHandle) curr);
			}
			break;
		}

		return (RetType) ret;
	}

	protected <RetType> RetType notifyAgent(DustHandle hAgent, DustAccess access, DustHandle hMessage) {
		DustAgent agt = access(DustAccess.Get, null, hAgent, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		DustMachineIdea iThread = THREADS.get();

		try {
//			DustMachineIdea iApp = getCtx(hAttCtxApp);
//		DustHandle hCtx = getHandle(iApp.mh, HANDLE_DUST_ASP_CALL_CONTEXT, null, true);

			DustMachineHandle hCtx = DustMachineBoot.getMachineHandle(TOKEN_DUST_ASP_CALL_CONTEXT);

			DustMachineIdea iCtx = DustMachineBoot.getIdea(hCtx);

			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_APP, getCtx(HANDLE_DUST_ATT_CTX_APP));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_DLG, getCtx(HANDLE_DUST_ATT_CTX_DLG));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_AGT,
					DustMachineBoot.getIdea((DustMachineHandle) hAgent));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_MSG,
					DustMachineBoot.getIdea((DustMachineHandle) hMessage));

			access(DustAccess.Insert, iCtx, iThread, HANDLE_DUST_ATT_CALL_STACK, 0);

			if (null == agt) {
				DustHandle hNarrative = access(DustAccess.Get, null, hAgent, HANDLE_MIND_ATT_NARRATIVE);
				String cn = access(DustAccess.Get, null, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER,
						hNarrative);

				Class ca = Class.forName(cn);
				agt = Dust.createInstance(ca);

				agt.init();

				access(DustAccess.Set, agt, hAgent, HANDLE_DUST_ATT_WRAPPEDOBJECT);
			}

			agt.process();
		} catch (Throwable e) {
			DustException.wrap(e);
		} finally {
			DustMachineIdea iCtx = access(DustAccess.Delete, null, iThread, HANDLE_DUST_ATT_CALL_STACK, 0);
			access(DustAccess.Delete, iCtx.mh, null);
		}

		return null;
	}

	@Override
	public void init() throws Exception {
		CTXS = new DustHandle[] { HANDLE_DUST_ATT_CTX_MSG, HANDLE_DUST_ATT_CTX_AGT, HANDLE_DUST_ATT_CTX_DLG,
				HANDLE_DUST_ATT_CTX_APP, };

//	protected void init() throws Exception {
		syncUnits(true);

//		syncUnits(false);
	}

	public void begin() throws Exception {

		DustHandle hPlatform = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_PLATFORM);

		Collection<DustHandle> modules = access(DustAccess.Get, Collections.EMPTY_LIST, null, HANDLE_DUST_ATT_CTX_APP,
				HANDLE_DUST_ATT_NODE, HANDLE_MISC_ATT_PARENT, HANDLE_DUST_ATT_MODULES);

		for (DustHandle mod : modules) {
			DustHandle mu = mod.getUnit();
			for (DustHandle h : DustMachineUtils.getUnitMembers(mu)) {
				if (HANDLE_DUST_ASP_IMPLEMENTATION.equals(h.getType())) {
					boolean match = access(DustAccess.Check, hPlatform, h, HANDLE_DUST_ATT_PLATFORM);
					if (match) {
						DustHandle target = access(DustAccess.Get, null, h, HANDLE_MISC_ATT_TARGET);
						String cName = access(DustAccess.Get, null, h, HANDLE_MISC_ATT_KEY);
						Dust.log(null, "Implementation found", target, cName);

						access(DustAccess.Set, cName, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER, target);
					}
				}
			}
		}

		Object rm = access(DustAccess.Get, Collections.EMPTY_MAP, null, HANDLE_DUST_ATT_CTX_APP,
				HANDLE_DUST_ATT_BINARY_RESOLVER);

		Dust.log(null, "Resolver map", rm);

	}

	@Override
	public void process() throws Exception {
		// TODO Auto-generated method stub
	}
}
