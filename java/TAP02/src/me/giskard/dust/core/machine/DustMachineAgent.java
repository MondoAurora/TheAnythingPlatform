package me.giskard.dust.core.machine;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.api.DustMachine;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.core.utils.DustUtilsJsonApi;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustMachineAgent extends DustMachine implements DustGenBootConsts, DustUtilsConsts, DustMachineConsts {

	static WeakHashMap<Object, Map> THREAD_ANCHORS = new WeakHashMap<Object, Map>();

	static ThreadLocal<DustMachineIdea> THREADS = new ThreadLocal<DustMachineIdea>() {
		public void set(DustMachineIdea value) {
//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SET thread context", Thread.currentThread(), value);
			super.set(value);
		};
	};

	static DustMachineHandle hAspUnit;
	static DustMachineHandle hAspAsp;

	static DustMachineHandle hAttCallStack;

	static DustMachineHandle hAttCtxApp;
	static DustMachineHandle hAttCtxDlg;
	static DustMachineHandle hAttCtxAgt;
	static DustMachineHandle hAttCtxMsg;

	static DustMachineHandle hAttUnitHandles;
	static DustMachineHandle hAttUnitObjects;
	static DustMachineHandle hAttUnitState;

	static DustMachineHandle hTagStateInSync;

	static boolean running = false;

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

	static DustMachineIdea getCtx(Object key) {
		DustMachineIdea it = THREADS.get();
		DustMachineIdea iCtx = DustUtils.simpleGet(it.content, hAttCallStack, 0);
		return (DustMachineIdea) iCtx.content.get(key);
	}

	@Override
	protected synchronized void manageThread(DustThreadAction action, Object anchor) {
		DustMachineIdea iThread = THREADS.get();
		Map anchored;

		switch (action) {
		case SetAnchor: {
			anchored = new TreeMap();
			Map src = ((DustMachineIdea) DustUtils.simpleGet(iThread.content, hAttCallStack, 0)).content;

			anchored.put(HANDLE_DUST_ATT_CTX_APP, ((DustMachineIdea) src.get(HANDLE_DUST_ATT_CTX_APP)).mh);
			anchored.put(HANDLE_DUST_ATT_CTX_DLG, ((DustMachineIdea) src.get(HANDLE_DUST_ATT_CTX_DLG)).mh);
			anchored.put(HANDLE_DUST_ATT_CTX_AGT, ((DustMachineIdea) src.get(HANDLE_DUST_ATT_CTX_AGT)).mh);

			THREAD_ANCHORS.put(anchor, anchored);
		}
			break;
		case Connect: {
			anchored = THREAD_ANCHORS.get(anchor);
			if (null == iThread) {
				DustMachineHandle hThread = DustMachineBoot.getMachineHandle(TOKEN_DUST_ASP_THREAD);
				iThread = DustMachineBoot.getIdea(hThread);

				Set<DustMachineHandle> threads = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLTHREADS);
				threads.add(hThread);

				THREADS.set(iThread);
			}

			DustMachineHandle hDlg = DustMachineBoot.getMachineHandle(TOKEN_DUST_ASP_DIALOG);
			Set<DustMachineHandle> dialogs = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLDIALOGS);
			dialogs.add(hDlg);

			DustMachineIdea iDlg = DustMachineBoot.getIdea(hDlg);

			DustMachineHandle hCtx = DustMachineBoot.getMachineHandle(TOKEN_DUST_ASP_CALL_CONTEXT);
			DustMachineIdea iCtx = DustMachineBoot.getIdea(hCtx);

			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_APP,
					DustMachineBoot.getIdea((DustMachineHandle) anchored.get(HANDLE_DUST_ATT_CTX_APP)));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_AGT,
					DustMachineBoot.getIdea((DustMachineHandle) anchored.get(HANDLE_DUST_ATT_CTX_AGT)));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_DLG, iDlg);

			ArrayList cs = DustUtils.safeGet(iThread.content, ARRAY_CREATOR, HANDLE_DUST_ATT_CALL_STACK);
			cs.clear();
			cs.add(iCtx);
			iThread.content.put((DustMachineHandle) HANDLE_DUST_ATT_DIALOG, hDlg);
		}
			break;
		case ReleaseSuccess:
			commitLocalObjects();
			// NO break
		case ReleaseFailure: {
			DustMachineHandle hDlg = access(DustAccess.Delete, null, iThread, HANDLE_DUST_ATT_DIALOG);
			access(DustAccess.Delete, hDlg, null);
			access(DustAccess.Delete, iThread.mh, null);

			Set<DustMachineHandle> threads = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLTHREADS);
			threads.remove(iThread.mh);
			Set<DustMachineHandle> dialogs = DustMachineBoot.getMachineData(TOKEN_DUST_ATT_ALLDIALOGS);
			dialogs.remove(hDlg);

			THREADS.remove();
		}
			break;
		}
	}

	static DustMachineIdea resolveHandle(DustHandle h) {
		return running ? resolveHandle(h, false) : DustMachineBoot.getIdea((DustMachineHandle) h);
	}

	static DustMachineIdea resolveHandle(DustHandle h, boolean change) {
		DustMachineHandle mh = (DustMachineHandle) h;

		if (!running) {
			return DustMachineBoot.getIdea(mh);
		}

		Map<DustMachineHandle, DustMachineIdea> localIdeas = change
				? DustUtils.safeGet(getCtx(HANDLE_DUST_ATT_CTX_DLG).content, SORTEDMAP_CREATOR, hAttUnitObjects)
				: DustUtils.simpleGet(getCtx(HANDLE_DUST_ATT_CTX_DLG).content, hAttUnitObjects);
		DustMachineIdea idea = (null == localIdeas) ? null : localIdeas.get(mh);

		if (null == idea) {
			idea = DustMachineBoot.getIdea(mh);

			if (change) {
				DustMachineIdea copy = new DustMachineIdea(mh);
				if (null != idea) {
					DustMachineUtils.loadData(copy, idea);
				}
				localIdeas.put(mh, copy);

				Dust.log(null, "*****  Local copy created for", mh);

				idea = copy;
			}
		}

		return idea;
	}

	private Object checkAccess(DustMachineIdea iAgt, DustAccess acess, DustMachineIdea iTarget, DustMachineHandle hAtt,
			Object lastKey, Object valPrev, Object valNew) {
		Object ret = valNew;

		if ((null != iTarget) && (null != hAtt)) {
			boolean pass = true;

//			if (pass && DustUtils.isChange(acess)) {
//				iTarget.mh.unit.content.put(hAttUnitState, hTagStateInSync);
//			}
		}

		return ret;
	}

	synchronized DustMachineHandle getUnit(String unitId, boolean createIfMissing) {
		DustMachineIdea iApp = getCtx(hAttCtxApp);

		DustMachineHandle hUnit = getHandleInt(iApp, hAspUnit, unitId, createIfMissing);

		return hUnit;
	}

	Collection<DustHandle> getUnits() {
		DustMachineIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineHandle, DustMachineIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);

		return new TreeSet<DustHandle>(unitMap.keySet());
	}

	boolean inSync = false;

	protected boolean syncUnits(boolean load) throws Exception {
		boolean ret = false;

		DustMachineIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineHandle, DustMachineIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);
		DustHandle hMsg = Dust.access(DustAccess.Peek, null, iApp, HANDLE_DUST_ATT_NODE, HANDLE_STREAM_ATT_UNIT_HANDLER);

		if (load) {
			if (!inSync) {
				try {
					inSync = true;
					for (boolean chg = true; chg;) {
						chg = false;
						for (Map.Entry<DustMachineHandle, DustMachineIdea> eu : unitMap.entrySet()) {
							DustMachineIdea iUnit = eu.getValue();
							DustMachineHandle hUnit = eu.getKey();

							if (hTagStateInSync != iUnit.content.get(hAttUnitState)) {
								try {
									Dust.log(null, "Loading unit", iUnit);

									Dust.access(DustAccess.Set, hUnit, hMsg, HANDLE_MISC_ATT_TARGET);

									Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_LOAD, hMsg, HANDLE_MIND_ATT_CMD);
									Dust.access(DustAccess.Commit, null, hMsg);

									iUnit.content.put(hAttUnitState, hTagStateInSync);
								} finally {
									Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MIND_ATT_CMD);
									Dust.access(DustAccess.Delete, null, hMsg, HANDLE_MISC_ATT_TARGET);
								}

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
				} finally {
					inSync = false;
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

		try (FileOutputStream fos = new FileOutputStream("localStore/" + handle.id + DUST_EXT_JSON)) {
			DustUtilsJsonApi.storeUnit(handle, fos);
		} catch (Throwable e) {
			DustException.wrap(e, "saving unit", handle.id);
		}
	}

	@Override
	public synchronized DustHandle getHandle(String id, DustHandle type) {
		DustMachineHandle hUnit;
		boolean createIfMissing = (null != type);
		boolean onlyUnit = false;

		String[] ii = DustUtils.splitId(id);
		if (null != ii[0]) {
			hUnit = getUnit(ii[0], createIfMissing);
		} else {
			hUnit = getUnit(ii[1], createIfMissing);
			onlyUnit = true;
		}

		DustMachineIdea iUnit = resolveHandle(hUnit);

		if (running && (hTagStateInSync != iUnit.content.get(hAttUnitState))) {
			try {
				syncUnits(true);
			} catch (Exception e) {
				DustException.wrap(e, "syncUnits failed after loading", hUnit);
			}
		}

		if (onlyUnit) {
			return hUnit;
		}

		if (DustHandle.PLACEHOLDER == type) {
			type = null;
		}

//		if (null == type) {
//			Dust.log(null, "getHandle", "null type", id);
//		}

		DustMachineHandle hType = (null == type) ? null
				: (type instanceof DustMachineHandle) ? (DustMachineHandle) type
						: getHandleInt(null, hAspAsp, type.getId(), true);

		DustMachineHandle hRet = getHandleInt(iUnit, hType, id, createIfMissing);

		return hRet;
	}

	DustMachineHandle getHandleInt(DustMachineIdea iUnit, DustMachineHandle type, String id, boolean createIfMissing) {
		if (null == iUnit) {
			String[] ii = DustUtils.splitId(id);
			if (null != ii[0]) {
				DustMachineHandle hUnit = getUnit(ii[0], createIfMissing);
				iUnit = resolveHandle(hUnit);
			}
		}

		DustMachineHandle hRet = (null == id) ? null : DustUtils.simpleGet(iUnit.content, hAttUnitHandles, id);

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
				switch (access) {
				case Delete: {
					DustMachineHandle hh = (DustMachineHandle) val;
					if (null != hh) {
						Map m = hh.unit.content;
						((Map) m.get(HANDLE_DUST_ATT_UNIT_HANDLES)).remove(hh.id);
						((Map) m.get(HANDLE_DUST_ATT_UNIT_OBJECTS)).remove(hh);
					}
				}
					break;
				case Commit:
					commitLocalObjects();
					return null;
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
				iCurr = resolveHandle(prevHandle);
				curr = iCurr.getContent();
				collType = DustCollType.Map;
			} else if (null == curr) {
				if (create) {

					curr = (p instanceof Integer) ? new ArrayList() : new HashMap();

					if (null != prevColl) {
//						Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Should ensure using a local deep copy of the edited idea", iCurr.mh);
						/* curr = */ checkAccess(iAgt, DustAccess.Insert, iCurr, prevAtt, lastKey, null, curr);

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

			/* curr = */ checkAccess(iAgt, create ? DustAccess.Get : DustAccess.Peek, iCurr, prevAtt, lastKey, null, curr);
		}

		/**
		 * Spec handle tags
		 */

		if (HANDLE_MIND_ATT_TAGS.equals(prevAtt)) {
			if (curr instanceof Collection) {
				for (DustHandle ht : (Collection<DustHandle>) curr) {
					switch (access) {
					case Peek:
						if (DustUtils.isEqual(val, access(DustAccess.Peek, null, ht, HANDLE_MISC_ATT_PARENT))) {
//						if (DustUtils.isEqual(val, access(DustAccess.Peek, "", ht, HANDLE_MISC_ATT_PARENT, TOKEN_MIND_ATT_ID))) {
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
			if (running && (null != prevHandle)) {
				DustMachineIdea iNew = resolveHandle(prevHandle, true);
				if (iCurr != iNew) {
					iCurr = iNew;
					prevColl = iCurr.content;
				}
			} else {

			}
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

						int specIdx = DustUtils.indexOf(lastKey, HANDLE_MIND_ATT_TYPE, HANDLE_MIND_ATT_ID, HANDLE_MIND_ATT_UNIT);

						switch (specIdx) {
						case 0:
							((DustMachineHandle) lastHandle).type = (DustMachineHandle) val;
							break;
						case 1:
							((DustMachineHandle) lastHandle).id = (String) val;
							break;
						case 2:
							((DustMachineHandle) lastHandle).unit = resolveHandle((DustMachineHandle) val);
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
			Object ll = access(DustAccess.Peek, Collections.EMPTY_LIST, curr, HANDLE_MIND_ATT_LISTENERS);
			for (DustHandle l : (Collection<DustHandle>) ll) {
				ret = notifyAgent((DustHandle) l, access, (DustHandle) curr);
			}
			break;
		}

		return (RetType) ret;
	}

	synchronized void commitLocalObjects() {
		Map<DustMachineHandle, DustMachineIdea> localObjects = DustUtils.simpleGet(getCtx(HANDLE_DUST_ATT_CTX_DLG).content,
				hAttUnitObjects);

		if (null != localObjects) {
			for (DustMachineIdea li : localObjects.values()) {
				DustMachineIdea idea = DustMachineBoot.getIdea(li.mh);

				// should later manage commit IDs and check current to detect collision.

				idea.content.clear();
				idea.content.putAll(li.content);
			}
		}
	}

	protected <RetType> RetType notifyAgent(DustHandle hAgent, DustAccess access, DustHandle hMessage) {
		DustAgent agt = access(DustAccess.Get, null, hAgent, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		DustMachineIdea iThread;

		iThread = THREADS.get();

		try {
//			DustMachineIdea iApp = getCtx(hAttCtxApp);
//		DustHandle hCtx = getHandle(iApp.mh, HANDLE_DUST_ASP_CALL_CONTEXT, null, true);

			DustMachineHandle hCtx = DustMachineBoot.getMachineHandle(TOKEN_DUST_ASP_CALL_CONTEXT);

			DustMachineIdea iCtx = resolveHandle(hCtx);

			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_APP, getCtx(HANDLE_DUST_ATT_CTX_APP));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_DLG, getCtx(HANDLE_DUST_ATT_CTX_DLG));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_AGT, resolveHandle((DustMachineHandle) hAgent));
			iCtx.content.put((DustMachineHandle) HANDLE_DUST_ATT_CTX_MSG, resolveHandle((DustMachineHandle) hMessage));

			access(DustAccess.Insert, iCtx, iThread, HANDLE_DUST_ATT_CALL_STACK, 0);

			if (null == agt) {
				DustHandle hNarrative = access(DustAccess.Get, null, hAgent, HANDLE_MIND_ATT_NARRATIVE);
				String cn = access(DustAccess.Get, null, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_BINARY_RESOLVER,
						hNarrative);

				Class ca = Class.forName(cn);
				agt = Dust.createInstance(ca);
				access(DustAccess.Set, agt, hAgent, HANDLE_DUST_ATT_WRAPPEDOBJECT);

				agt.init();

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
	}

	public void begin() throws Exception {
		syncUnits(true);

//	syncUnits(false);

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

		Dust.log(null, "Resolver map loaded", rm);

		DustMachineIdea iApp = getCtx(hAttCtxApp);

		Map<DustMachineHandle, DustMachineIdea> unitMap = DustUtils.simpleGet(iApp.content, hAttUnitObjects);

		for (DustMachineIdea iUnit : unitMap.values()) {
			access(DustAccess.Insert, HANDLE_MISC_TAG_MANDATORY, iUnit.content, HANDLE_MIND_ATT_TAGS);
		}

		access(DustAccess.Commit, null, null);

		running = true;
	}

	@Override
	public void process() throws Exception {
		// TODO Auto-generated method stub
	}
}
