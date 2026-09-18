package me.giskard.dust.core.machine;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.WeakHashMap;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.dev.DustDevCounter;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMachineAgent extends DustMachineConsts.DustMachineImpl implements DustMachineConsts {

	class CallContext {
		private DustHandle hAgent;
		private DustHandle hMessage;
		private Map work;

		CallContext(DustHandle hAgent, DustHandle hMessage) {
			init(hAgent, hMessage);
		}

		public void init(DustHandle hAgent, DustHandle hMessage) {
			this.hAgent = hAgent;
			this.hMessage = hMessage;
		}

		CallContext(CallContext src) {
			hAgent = src.hAgent;
			hMessage = src.hMessage;
		}

		public <RetType> RetType get(DustContext ctx, boolean createMissing) {
			Object ret = null;
			switch (ctx) {
			case Agent:
				ret = hAgent;
				break;
			case Dialog:
				break;
			case Message:
				ret = hMessage;
				break;
			case Work:
				ret = work;
				if (createMissing && (null == ret)) {
					ret = work = new HashMap();
				}
				break;
			}

			return (RetType) ret;
		}

		@Override
		public String toString() {
			return DustUtils.sbAppend(null, " ", true, "CallContext", hAgent, hMessage, work).toString();
		}
	}

	static ThreadLocal<CallContext> THREAD_CONTEXTS = new ThreadLocal<CallContext>() {
//		public CallContext get() {
//			CallContext ctx = super.get();
//			Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Get thread context", Thread.currentThread(), ctx);
//			return ctx;
//		};

		public void set(CallContext value) {
//			Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SET thread context", Thread.currentThread(), value);
			super.set(value);
		};
	};

	public <RetType> RetType optGetCtx(Object in, boolean createMissing) {
		return (RetType) ((in instanceof DustContext) ? THREAD_CONTEXTS.get().get((DustContext) in, createMissing) : in);
	}

	Stack<ArrayList<DustHandle>> transactionStack = new Stack<ArrayList<DustHandle>>();

	ThreadLocal<Set<DustHandle>> loadingUnit = new ThreadLocal<Set<DustHandle>>() {
		protected Set<DustHandle> initialValue() {
			return new HashSet<DustHandle>();
		};
	};

	DustMachineHandle typeType;
	DustMachineHandle typeAtt;
	DustMachineHandle typeUnit;

	DustMachineIdea unitApp;
	DustMachineIdea machine;
	DustMachineIdea unitMeta;

	DustMachineHandle defaultSerializer;
	Set<DustHandle> changedUnits = new HashSet<>();

	DustCreator<DustMachineHandle> createHandle = new DustCreator<DustMachineHandle>() {
		@Override
		public DustMachineHandle create(Object key, Object... hints) {
			String k = (String) key;
			DustMachineIdea unit = (DustMachineIdea) hints[0];
			DustMachineHandle hUnit = unit.mh;

			if ((null != unitApp) && (unitApp.mh != hUnit) && !loadingUnit.get().contains(hUnit)) {
//			if ((null != unitApp) && !loadingUnit.get().contains(hUnit)) {
				changedUnits.add(hUnit);
			}

			return new DustMachineHandle(DustMachineAgent.this, unit, (DustMachineHandle) hints[1], k);
		}
	};

	DustCreator<DustMachineIdea> createIdea = new DustCreator<DustMachineIdea>() {
		@Override
		public DustMachineIdea create(Object key, Object... hints) {
			DustMachineHandle h = (DustMachineHandle) key;

			DustMachineIdea ret = new DustMachineIdea(h);

			if ((null != typeUnit) && DustUtils.isEqual(typeUnit, h.getType())) {
				initUnit(ret, false);
			}

			return ret;
		}

		@Override
		public void initNew(DustMachineIdea item, Object key, Object... hints) {
			DustHandle type = item.mh.getType();
			if ((null != type) && DustUtils.isEqual(typeUnit, type)) {
				optLoadUnit(item.mh.getId(), item);
			}
		};
	};

	private Map<String, DustMachineIdea> bootRefUnits;

	public DustMachineAgent() {
		machine = new DustMachineIdea(new DustMachineHandle(this, null, null, TOKEN_DUST_AGT_RUNTIME));
		initUnit(machine, true);
		unitMeta = new DustMachineIdea(new DustMachineHandle(this, machine, null, UNIT_MIND));
		initUnit(unitMeta, false);

		((Map) machine.content.get(TOKEN_DUST_ATT_UNIT_REFS)).put(UNIT_DUST, unitMeta.mh);
		((Map) machine.content.get(TOKEN_DUST_ATT_UNIT_OBJECTS)).put(unitMeta.mh, unitMeta);

		typeType = safeGetIdea(unitMeta, null, TOKEN_MIND_ASP_ASPECT, DustOptCreate.Meta).mh;
		typeAtt = safeGetIdea(unitMeta, typeType, TOKEN_MIND_ASP_ATTRIBUTE, DustOptCreate.Meta).mh;
		typeUnit = safeGetIdea(unitMeta, typeType, TOKEN_MIND_ASP_UNIT, DustOptCreate.Meta).mh;

		typeType.init(unitMeta, typeType, TOKEN_MIND_ASP_ASPECT);
		DustMachineIdea typeIdea = safeGetIdea(unitMeta, typeType);
		typeIdea.loadMh();

		machine.mh.init(machine, typeUnit, TOKEN_DUST_AGT_RUNTIME);
		machine.loadMh();
		unitMeta.mh.init(machine, typeUnit, UNIT_DUST);
		unitMeta.loadMh();

		THREAD_CONTEXTS.set(new CallContext(machine.mh, null));
	}

	@Override
	protected void init() {
		DustHandle hMachine = getHandle(unitApp.mh, null, TOKEN_DUST_AGT_RUNTIME, DustOptCreate.None);
		defaultSerializer = access(DustAccess.Peek, null, hMachine, TOKEN_MIND_ATT_SERIALIZER);
		optLoadUnit(UNIT_DUST, unitMeta);
		if (null != bootRefUnits) {
			for (Map.Entry<String, DustMachineIdea> be : bootRefUnits.entrySet()) {
				optLoadUnit(be.getKey(), be.getValue());
			}

			bootRefUnits = null;
		}

	}

	private void initUnit(DustMachineIdea iUnit, boolean weak) {
		iUnit.content.put(TOKEN_DUST_ATT_UNIT_OBJECTS, weak ? new WeakHashMap() : new HashMap());
		iUnit.content.put(TOKEN_DUST_ATT_UNIT_REFS, new TreeMap());
	}

	@Override
	protected DustMachineHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
		DustMachineHandle ret = null;

		if (DustUtils.isEmpty(id)) {
			if (optCreate == DustOptCreate.Primary) {
				id = DustUtils.getNewId(unit);
			} else {
				return null;
			}
		}

		DustMachineHandle u = (DustMachineHandle) unit;
		int sep = id.indexOf(DUST_SEP_TOKEN);

		if (type instanceof String) {
			type = getHandle(unitMeta.mh, typeType, (String) type, DustOptCreate.Meta);
		}

		if (optCreate == DustOptCreate.Meta) {
			if (null == type) {
				type = typeType;
			}
			if (unit != unitMeta.mh) {
				ret = getHandle(unitMeta.mh, type, id, (null == unit) ? DustOptCreate.Meta : DustOptCreate.None);
			}
		}

		if (null == ret) {
			if (-1 != sep) {
				String uid = id.substring(0, sep);
				if ((null == u) || !DustUtils.isEqual(u.getId(), uid)) {
					u = (DustMachineHandle) getUnit(uid, true);
				}
			}

			DustMachineIdea ui;

			if ((null != type) && DustUtils.isEqual(typeUnit, type)) {
				ui = machine;
			} else {
				if (-1 == sep) {
					id = u.getId() + DUST_SEP_TOKEN + id;
				}
				ui = safeGetIdea(machine, u);
			}

			Map<String, DustMachineHandle> unitRefs = (Map) ui.content.get(TOKEN_DUST_ATT_UNIT_REFS);

			ret = (optCreate == DustOptCreate.None) ? unitRefs.get(id) : DustUtils.safeGet(unitRefs, createHandle, id, ui, type);
		}

		return ret;
	}

	Map getContent(DustHandle h) {
		DustMachineHandle mh = (DustMachineHandle) h;
		DustMachineIdea idea = safeGetIdea(mh.getUnitIdea(), mh);
		return idea.getContent();
	}

	private DustMachineIdea safeGetIdea(DustMachineIdea unit, Object type, String id, DustOptCreate optCreate) {
		DustMachineIdea ret = null;

		synchronized (unit) {
			DustMachineHandle mh = getHandle(unit.mh, type, id, optCreate);

			if (null != mh) {
				ret = safeGetIdea(unit, mh);
			}
		}
		return ret;
	}

	private DustMachineIdea safeGetIdea(DustMachineIdea unit, DustMachineHandle mh) {
		DustMachineIdea ret = null;
		Map mOb = (Map) unit.content.get(TOKEN_DUST_ATT_UNIT_OBJECTS);

		synchronized (mOb) {
			ret = DustUtils.safeGet(mOb, createIdea, mh);
		}
		return ret;
	}

	@Override
	protected DustMachineHandle getUnit(String unitId, boolean createIfMissing) {
		DustMachineIdea ui = getUnitIdea(unitId, createIfMissing);
		return (null == ui) ? null : ui.mh;
	}

	protected DustMachineIdea getUnitIdea(String unitId, boolean createIfMissing) {
		DustMachineIdea ret = null;
		DustMachineHandle mh = null;

		if (DustUtils.isEmpty(unitId)) {
			if (createIfMissing) {
				mh = new DustMachineHandle(this, machine, typeUnit, "");
			}
		} else {
			mh = getHandle(machine.mh, typeUnit, unitId, createIfMissing ? DustOptCreate.Primary : DustOptCreate.None);
		}

		if (null != mh) {
			ret = safeGetIdea(machine, mh);
		}

		return ret;
	}

	private void optLoadUnit(String unitId, DustMachineIdea unit) {
		if (!DustUtils.isEmpty(unitId)) {
			Object ser;

			DustHandle hMachine = (null == unitApp) ? null : getHandle(unitApp.mh, null, TOKEN_DUST_AGT_RUNTIME, DustOptCreate.None);
			ser = access(DustAccess.Peek, null, hMachine, TOKEN_STREAM_ATT_UNIT_HANDLER);

//			Object ser = (null == unitApp) ? null : access(DustAccess.Peek, null, unitApp.mh, TOKEN_DUST_AGT_RUNTIME, TOKEN_STREAM_ATT_UNIT_HANDLER);

			if (null != ser) {
				if (null != access(DustAccess.Peek, null, ser, TOKEN_MIND_ATT_NEXT)) {
					DustHandle hC = getHandle(null, null, TOKEN_MISC_TAG_CMD_LOAD, DustOptCreate.None);
					access(DustAccess.Set, hC, ser, TOKEN_MIND_ATT_CMD);
					access(DustAccess.Set, unitId, ser, TOKEN_MISC_ATT_KEY);

					try {
						loadingUnit.get().add(unit.mh);
						access(DustAccess.Process, null, ser);
					} finally {
						loadingUnit.get().remove(unit.mh);
					}

//					access(DustAccess.Delete, null, ser, TOKEN_MIND_ATT_CMD);
					access(DustAccess.Delete, null, ser, TOKEN_MISC_ATT_KEY);

					return;
				}
			}

			ser = access(DustAccess.Peek, defaultSerializer, machine, TOKEN_DUST_ATT_UNIT_OBJECTS, unitId, TOKEN_MIND_ATT_SERIALIZER);

			if (null != ser) {

				access(DustAccess.Set, TOKEN_MISC_TAG_CMD_LOAD, ser, TOKEN_MIND_ATT_CMD);
				access(DustAccess.Set, unitId, ser, TOKEN_MISC_ATT_KEY);
				access(DustAccess.Set, unit.mh, ser, TOKEN_MISC_ATT_DATA);

				try {
					loadingUnit.get().add(unit.mh);
					access(DustAccess.Process, null, ser);
				} finally {
					loadingUnit.get().remove(unit.mh);
				}
			} else {
				if (null != bootRefUnits) {
					if (machine.mh != unit.mh) {
						bootRefUnits.put(unitId, unit);
					}
				}
			}
		}
	}

	@Override
	protected boolean releaseUnit(DustHandle unit) {
		if (loadingUnit.get().contains(unit)) {
			Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "Skip release unit because it changed", unit.getId());
			return false;
		}

		return (null == unit) ? false : null != access(DustAccess.Delete, null, machine.content, TOKEN_DUST_ATT_UNIT_OBJECTS, unit);
	}

	@Override
	protected <RetType> RetType notifyAgent(DustHandle hAgent, DustAction action, DustAccess access, DustHandle hMessage) {
		String agent = hAgent.getId();
		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "To agent", agent, "message", hMessage);

		long start = System.currentTimeMillis();
		Object ret = null;
		CallContext ctxSave = THREAD_CONTEXTS.get();
//		DustUtilsFactory<DustContext, Object> ctx = CTX;
//		Set<DustHandle> chg = changedUnits;

		boolean tHead = Dust.access(DustAccess.Peek, false, hMessage, TOKEN_MIND_ATT_TRANSACTION_HEAD);
		if (tHead) {
			transactionStack.push(new ArrayList<DustHandle>());
		}

		boolean tItem = Dust.access(DustAccess.Peek, false, hMessage, TOKEN_MIND_ATT_TRANSACTION_ITEM);
		if (tItem) {
			ArrayList<DustHandle> ts = transactionStack.peek();
			if (!ts.contains(hAgent)) {
				ts.add(0, hAgent);
			}
		}

		Throwable exc = null;
		CallContext ctx = new CallContext(hAgent, hMessage);
		THREAD_CONTEXTS.set(ctx);

		try {
//			CTX.put(DustContext.Agent, hAgent);
//			CTX.put(DustContext.Message, hMessage);
//			CTX.put(DustContext.Message, params);

//			boolean save = Dust.access(DustAccess.Check, TOKEN_MISC_TAG_CMD_SAVE, params, TOKEN_CMD);
//			if (!save) {
//				changedUnits = new HashSet<>();
//			}
			ret = super.callAgent(hAgent, action, access);

//			saveChanges();
		} catch (Throwable e) {
			exc = e;
			DustException.wrap(e, "Agent", agent, "failed to process message", hMessage);
		} finally {
			Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Message processed", System.currentTimeMillis() - start, "msec.");

			if (tHead) {
//				CTX.clear();
				ArrayList<DustHandle> transactionItems = transactionStack.pop();
				for (DustHandle ht : transactionItems) {
					ctx.init(ht, null);
					try {
						super.callAgent(ht, DustAction.End, (null == exc) ? DustAccess.Commit : DustAccess.Rollback);
					} catch (Throwable e) {
						DustException.wrap(e, "Transaction close for agent", agent, "failed for message", hMessage);
					}
				}
			}

//			CTX = ctxSave;
			THREAD_CONTEXTS.set(ctxSave);
//		changedUnits = chg;
		}

		return (RetType) ret;
	}

	public void saveChanges() {
		for (DustHandle hChg : changedUnits) {
//			if ( (DustUtils.isEmpty(hChg.getId()))) {
			if ((unitApp.mh == hChg) || (machine.mh == hChg) || (DustUtils.isEmpty(hChg.getId()))) {
				continue;
			}

//			Object ser = null;
//
//			DustHandle hMachine = (null == unitApp) ? null : getHandle(unitApp.mh, null, TOKEN_DUST_AGT_RUNTIME, DustOptCreate.None);
//			ser = access(DustAccess.Peek, null, hMachine, TOKEN_STREAM_ATT_UNIT_HANDLER);
//
//			if (null != ser) {
//				if (null != access(DustAccess.Peek, null, ser, TOKEN_MIND_ATT_NEXT)) {
//					DustHandle hC = getHandle(null, null, TOKEN_MISC_TAG_CMD_SAVE, DustOptCreate.None);
//					access(DustAccess.Set, hC, ser, TOKEN_MIND_ATT_CMD);
//					access(DustAccess.Set, hChg.getId(), ser, TOKEN_MISC_ATT_KEY);
//
//					access(DustAccess.Process, null, ser);
//
//					access(DustAccess.Delete, null, ser, TOKEN_MIND_ATT_CMD);
//					access(DustAccess.Delete, null, ser, TOKEN_MISC_ATT_KEY);
//
//					continue;
//				}
//			}

			Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_SAVE, defaultSerializer, TOKEN_MIND_ATT_CMD);
			Dust.access(DustAccess.Set, hChg.getId(), defaultSerializer, TOKEN_MISC_ATT_KEY);
			Dust.access(DustAccess.Set, hChg, defaultSerializer, TOKEN_MISC_ATT_DATA);

			Dust.access(DustAccess.Process, null, defaultSerializer);
		}

		changedUnits.clear();
	}

	private void registerChange(DustHandle agent, DustAccess acess, DustHandle handle, DustHandle att, Object lastKey, Object oldVal, Object newVal)
			throws RuntimeException {
		checkAccess(agent, acess, handle, att, lastKey, newVal);

//		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Register change", agent, acess, handle, att, lastKey, newVal);

		DustHandle hUnit = DustUtils.isEqual(typeUnit, handle.getType()) ? handle : handle.getUnit();

		if (loadingUnit.get().contains(hUnit)) {
			return;
		}
		if ((machine.mh == hUnit) || (unitApp.mh == hUnit)) {
//		if (machine.mh == hUnit) {
			return;
		}

		changedUnits.add(hUnit);
	}

	@Override
	public <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		Object ret = null;

		if (0 == path.length) {
			if (null == root) {
				switch (access) {
				case Get:
				case Peek:
					return (RetType) new CallContext(THREAD_CONTEXTS.get());
				case Set:
					if (val instanceof CallContext) {
						THREAD_CONTEXTS.set((CallContext) val);
					} else {
						DustException.wrap(null, "You can only set a CallContext");
					}
					break;
				default:
					DustException.wrap(null, "Should not be here");
					break;
				}
			}
		}
//		DustHandle agent = (DustHandle) CTX.peek(DustContext.Agent);
		DustHandle agent = optGetCtx(DustContext.Agent, false);

		if ((null == root) || (root instanceof DustContext)) {
			return accessCtx(access, agent, val, (DustContext) root, path);
		}

		Object curr = root;

		DustCollType collType = DustUtils.getCollType(root);

		Object prev = null;
		Object lastKey = null;
		DustHandle lastHandle = null;

		Object prevColl = null;
		DustHandle prevHandle = (curr instanceof DustHandle) ? (DustHandle) curr : null;
		DustHandle prevAtt = null;

		if (val instanceof Enum) {
			val = ((Enum) val).name();
		}

		/**
		 * Process the path
		 */

		for (Object p : path) {
			if (p instanceof Enum) {
				p = ((Enum) p).name();
//			} else if (p instanceof String) {
//				DustObject a = Dust.getObject(prevUnit, typeAtt, (String) p, DustOptCreate.Meta);
//				p = a.getId();
//			} else if (p instanceof DustObject) {
//				p = ((DustObject) p).getId();
			}

			if (curr instanceof DustHandle) {
				lastHandle = prevHandle = (DustHandle) curr;

				if (p instanceof DustHandle) {
					prevAtt = (DustHandle) p;
					p = ((DustHandle) p).getId();
				} else if (p instanceof String) {
					DustHandle a = Dust.getHandle(prevHandle.getUnit(), typeAtt, (String) p, DustOptCreate.Meta);
					prevAtt = a;
					p = a.getId();
				}

				curr = getContent(prevHandle);
			} else if (null == curr) {
				if (access.creator) {
					curr = (p instanceof Integer) ? new ArrayList() : new HashMap();

					if (null != prevColl) {
						if ((null != prevAtt) && (null != prevHandle)) {
							registerChange(agent, DustAccess.Insert, prevHandle, prevAtt, lastKey, null, curr);
						}

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

			if ((null != prevAtt) && (null != prevHandle)) {
				curr = checkAccess(agent, access, prevHandle, prevAtt, lastKey, curr);
			}
		}

		/**
		 * Admin change
		 */

		Boolean change = null;
		boolean itemDel = false;

		if (TOKEN_MIND_ATT_TAGS.equals(lastKey)) {
//			DustHandle hVal = (val instanceof DustHandle) ? (DustHandle) val : getHandle(null, TOKEN_MIND_ASP_TAG, (String) val, DustOptCreate.None);
//			val = hVal;

			if (curr instanceof Collection) {
				for (Object o : (Collection) curr) {
					DustHandle ht = (o instanceof DustHandle) ? (DustHandle) o : getHandle(null, TOKEN_MIND_ASP_TAG, (String) o, DustOptCreate.None);
					switch (access) {
					case Peek:
//						if (DustUtils.isEqual(val, access(DustAccess.Peek, "", ht, TOKEN_MISC_ATT_PARENT))) {
						if (DustUtils.isEqual(val, access(DustAccess.Peek, "", ht, TOKEN_MISC_ATT_PARENT, TOKEN_MIND_ATT_ID))) {
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

		if (Boolean.TRUE.equals(change) && (null != lastHandle)) {
			registerChange(agent, access, lastHandle, prevAtt, lastKey, curr, val);
		}

		/**
		 * Do the job
		 */

		DustAction action = null;

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
						switch (specIdx) {
						case 0:
							((DustMachineHandle) lastHandle).type = (DustMachineHandle) val;
							break;
						case 1:
							((DustMachineHandle) lastHandle).id = (String) val;
							break;
						case 2:
							((DustMachineHandle) lastHandle).unit = getUnitIdea(((DustMachineHandle) val).getId(), true);
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
		case Begin:
			action = DustAction.Begin;
			break;
		case Commit:
			action = DustAction.End;
			break;
		case Rollback:
			action = DustAction.End;
			break;
		case Process:
			action = DustAction.Process;
			break;
		}

		if (curr instanceof DustHandle) {
			curr = checkAccess(agent, access, (DustHandle) curr, null, null, curr);
		}

		if (null != action) {
			Object ll = access(DustAccess.Peek, null, curr, TOKEN_MIND_ATT_LISTENERS);
			if (ll instanceof Collection) {
				for (Object l : (Collection) ll) {
					ret = notifyAgent((DustHandle) l, action, access, (DustHandle) curr);
				}
			}
		}

		return (RetType) ret;

	}

	public <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
//		DustHandle agent = peekCtx(DustContext.Agent);
		DustHandle agent = optGetCtx(DustContext.Agent, false);
		return accessCtx(access, agent, val, root, path);
	}

	private <RetType> RetType accessCtx(DustAccess access, DustHandle agent, Object val, Object root, Object... path) {
		Object ret = NOT_FOUND;

//		Object main = optGetCtx(root);
		Object main = optGetCtx(root, access.creator);
		Object def = val;
		boolean pg = false;

		switch (access) {
		case Peek:
		case Get:
			pg = true;
			def = NOT_FOUND;
		case Check:
		case Visit:
			ret = ((null != main) && (main == root)) ? access(access, def, main, path) : NOT_FOUND;
			for (DustContext dc : DustContext.values()) {
				if (NOT_FOUND != ret) {
					break;
				}
				Object ctx = optGetCtx(dc, false);
				ret = (null == ctx) ? NOT_FOUND : access(access, def, ctx, path);
			}

			if (pg && (NOT_FOUND == ret)) {
				ret = val;
			}
			break;

		case Begin:
		case Process:
		case Commit:
		case Rollback:

		case Set:
		case Insert:
		case Delete:
		case Reset:
			// ret = access(access, val, main, path);
//			ret = ((null != main) && (main == root)) ? access(access, def, main, path) : NOT_FOUND;
			ret = (null != main) ? access(access, def, main, path) : NOT_FOUND;
			break;

		}

		if (ret instanceof DustHandle) {
			ret = checkAccess(agent, access, (DustHandle) ret, null, null, ret);
		}

		return (RetType) ret;
	}

	private Object checkAccess(DustHandle agent, DustAccess acess, DustHandle handle, DustHandle att, Object lastKey, Object value) throws RuntimeException {
		Object ret = value;
		Map m;

		if (null != value) {
			if (DustUtils.isChange(acess) && (null != att)) {
				m = getContent(att);
				if (Boolean.TRUE.equals(m.get(TOKEN_MIND_TAG_FINAL))) {
					DustException.wrap(null, "Trying to overwrite a final attribute)");
				}
			}
			m = getContent(handle);

			Collection c = (Collection) m.get(TOKEN_AAA_ATT_READABLETO);

			if (null != c) {
				if (!c.contains(agent)) {
					ret = null;
				}
			}
		}
		return ret;
	}

	@Override
	protected synchronized DustHandle bootLoadAppUnit(DustHandle appUnit, String path, InputStream is, Bootloader bootLoader) throws Exception {
		if (null == appUnit) {
			String[] ss = path.split("/");
			int sl = ss.length;
			path = ss[sl - 2] + "/" + ss[sl - 1];
//			int u = path.lastIndexOf("/");
//			String unitId = DustUtils.cutPostfix(path.substring(u + 1), ".");
			bootRefUnits = new TreeMap<>();

			String unitId = DustUtils.cutPostfix(path, ".");

			this.unitApp = getUnitIdea(unitId, true);
			appUnit = this.unitApp.mh;

		}
		loadingUnit.get().add(appUnit);
		bootLoader.loadStreamBoot(appUnit, is);

		return appUnit;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		switch (access) {
		case Process:
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_MIND_ATT_CMD);
			switch (cmd) {
			default:
				Dust.log(TOKEN_MISC_TAG_LEVEL_WARNING, "MindAgent not handling command", cmd);
				break;
			case TOKEN_MISC_TAG_CMD_SAVE:
				saveChanges();
				break;
			case TOKEN_MIND_CMD_GETHANDLE:
				String hId = Dust.access(DustAccess.Peek, null, DustContext.Message, TOKEN_MISC_ATT_GLOBALID);

				String[] parts = hId.split("\\$");
				DustHandle hh = getUnit(parts[0], true);
				hh = getHandle(hh, null, parts[1], DustOptCreate.None);
				Dust.access(DustAccess.Set, hh, DustContext.Message, TOKEN_MISC_ATT_TARGET);
				// getHandle(null, null, hId, DustOptCreate.None);
				break;
			case TOKEN_MISC_TAG_CMD_DELETE:
				Collection<DustHandle> toDel = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, DustContext.Message, TOKEN_MISC_ATT_MEMBERS);
				Dust.access(DustAccess.Delete, null, DustContext.Message, TOKEN_MISC_ATT_TARGET);

				for (DustHandle dh : toDel) {
					Object old;
					DustHandle hu = dh.getUnit();
					DustMachineIdea ui = getUnitIdea(hu.getId(), false);
					changedUnits.add(hu);

					old = ((Map) ui.content.get(TOKEN_DUST_ATT_UNIT_REFS)).remove(dh.getId());
					old = ((Map) ui.content.get(TOKEN_DUST_ATT_UNIT_OBJECTS)).remove(dh);

					Dust.access(DustAccess.Insert, ((DustMachineIdea) old).content.toString(), DustContext.Message, TOKEN_MISC_ATT_TARGET, KEY_ADD);
				}

				break;
			case TOKEN_MIND_CMD_LISTUNITS:
				Map<String, ? extends DustHandle> um = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, machine.content, TOKEN_DUST_ATT_UNIT_REFS);
				Dust.access(DustAccess.Delete, null, DustContext.Message, TOKEN_MISC_ATT_TARGET);

				for (DustHandle dh : um.values()) {
					Dust.access(DustAccess.Insert, dh, DustContext.Message, TOKEN_MISC_ATT_TARGET);
				}
//				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "listUnits", o);
				break;
			case TOKEN_MISC_TAG_CMD_INFO:

				DustHandle info = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_MISC_TAG_CMD_INFO);
				String iid = info.getId();
				String uid = DustUtils.getPrefix(iid, DUST_SEP_TOKEN);
				DustHandle unitInfo = Dust.getUnit(uid, true);
				info = getHandle(unitInfo, info.getType(), iid, DustOptCreate.Primary);
//				String lastChanged = Dust.access(DustAccess.Peek, null, info, TOKEN_LASTCHANGED);

//				if (!DustUtils.isEmpty(lastChanged)) {
//					return info;
//				}

				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Before MiND info", DustDevUtils.memInfo());

				Collection<String> loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, machine, TOKEN_DUST_ATT_UNIT_OBJECTS, KEY_MAP_KEYS);
				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Loaded units", loadedUnits);

				Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_INFO, defaultSerializer, TOKEN_MIND_ATT_CMD);

				Dust.access(DustAccess.Process, null, defaultSerializer);

				Collection<String> fileNames = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, defaultSerializer, TOKEN_MISC_ATT_MEMBERS);

				DustUtilsFactory<String, DustHandle> atts = new DustUtilsFactory<String, DustHandle>(new DustCreator<DustHandle>() {
					@Override
					public DustHandle create(Object key, Object... hints) {
						return getHandle(null, typeAtt, (String) key, DustOptCreate.Meta);
					}
				});

				DustDevCounter<DustHandle> cntGlobal = new DustDevCounter<DustHandle>(false);
				DustDevCounter<DustHandle> cntUnit = new DustDevCounter<DustHandle>(false);
				Set<DustHandle> types = new HashSet<DustHandle>();
				Set<String> metaUnitsGlobal = new HashSet<>();
				int totalCount = 0;

				for (String fn : fileNames) {
					if (!fn.endsWith(DUST_EXT_JSON)) {
						continue;
					}
					String un = DustUtils.cutPostfix(fn, ".");
					un = DustUtils.getPostfix(un, "/");

					if (DustUtils.isEqual(uid, un)) {
						Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SKIPPING unit info", un);
						continue;
					}

					if (DustUtils.isEqual(UNIT_DUST, un)) {
						Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "SKIPPING unit info", un);
						continue;
					}

					Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Loading unit info", un);
					boolean notLoaded = !loadedUnits.contains(un);

					DustHandle u = getUnit(un, notLoaded);
					cntUnit.reset();
					Set<String> metaUnits = new HashSet<>();

					int count = 0;

					for (DustHandle h : DustMachineUtils.getUnitMembers(u)) {
						++count;

						DustHandle type = h.getType();
						types.add(type);

						metaUnits.add(type.getUnit().getId());

						cntUnit.add(type);
						cntGlobal.add(type);

						Map<String, Object> data = getContent(h);

						for (Map.Entry<String, Object> de : data.entrySet()) {
							String attName = de.getKey();
							DustHandle att = atts.get(attName);

							metaUnits.add(att.getUnit().getId());
							cntUnit.add(att);
							cntGlobal.add(att);

							Object val = de.getValue();

							String valType = null;
							String collType = null;

							if (val instanceof Set) {
								collType = TOKEN_MIND_TAG_COLLTYPE_SET;
							} else if (val instanceof Collection) {
								collType = TOKEN_MIND_TAG_COLLTYPE_ARR;
							} else if (val instanceof Map) {
								collType = TOKEN_MIND_TAG_COLLTYPE_MAP;
							}

							if (null != collType) {
								val = DustUtils.getSample(val);
							}

							if (null != val) {
								if (val instanceof String) {
									String sVal = ((String) val).toLowerCase().trim();

									if (-1 != DustUtils.indexOf(sVal, DustUtils.DUST_BOOL)) {
										valType = TOKEN_MIND_TAG_VALTYPE_BOOL;
									} else {
										valType = TOKEN_MIND_TAG_VALTYPE_STRING;
									}
								} else if (val instanceof DustHandle) {
									valType = TOKEN_MIND_TAG_VALTYPE_HANDLE;
								} else if (val instanceof Long) {
									valType = TOKEN_MIND_TAG_VALTYPE_INTEGER;
								} else if (val instanceof Double) {
									valType = TOKEN_MIND_TAG_VALTYPE_REAL;
								} else {
									valType = TOKEN_MIND_TAG_VALTYPE_RAW;
								}
							}

							Dust.access(DustAccess.Set, valType, att, TOKEN_MIND_TAG_VALTYPE);
							if (null != collType) {
								Dust.access(DustAccess.Set, collType, att, TOKEN_MIND_TAG_COLLTYPE);
							}
						}
					}

					totalCount += count;
					metaUnitsGlobal.addAll(metaUnits);

					Dust.access(DustAccess.Set, count, info, TOKEN_MIND_ATT_KNOWNUNITS, un, TOKEN_MISC_ATT_COUNT);

					for (String mu : metaUnits) {
						Dust.access(DustAccess.Set, mu, info, TOKEN_MIND_ATT_KNOWNUNITS, un, TOKEN_MISC_ATT_META, KEY_ADD);
					}

					for (Map.Entry<DustHandle, Long> cnt : cntUnit) {
						DustHandle h = cnt.getKey();
						String t = types.contains(h) ? TOKEN_MISC_ATT_TYPES : TOKEN_MISC_ATT_ATTRIBUTES;
						Dust.access(DustAccess.Set, cnt.getValue(), info, TOKEN_MIND_ATT_KNOWNUNITS, un, t, h.getId());
					}

					if (notLoaded && !metaUnitsGlobal.contains(un)) {
						Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Dropping unit", un);
						releaseUnit(u);
					}
				}

				Dust.access(DustAccess.Set, totalCount, info, TOKEN_MISC_ATT_COUNT);

				Map<String, Object> params = new HashMap<>();
				params.put(TOKEN_MIND_ATT_CMD, TOKEN_MISC_TAG_CMD_SAVE);

				for (String mu : metaUnitsGlobal) {
					if (DustUtils.isEqual(UNIT_DUST, mu)) {
						continue;
					}
					Dust.access(DustAccess.Set, mu, info, TOKEN_MISC_ATT_META, KEY_ADD);
				}

				for (Map.Entry<DustHandle, Long> cnt : cntGlobal) {
					DustHandle h = cnt.getKey();
					String id = h.getId();

					if (types.contains(h)) {
						Map<String, DustHandle> cm = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, h, TOKEN_MISC_ATT_CHILDMAP);

						for (Map.Entry<String, DustHandle> ce : cm.entrySet()) {
							Dust.access(DustAccess.Set, ce.getValue().getId(), info, TOKEN_MISC_ATT_TYPES, id, TOKEN_MISC_ATT_CHILDMAP, ce.getKey());
						}
					} else {
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, h, TOKEN_MIND_TAG_VALTYPE), info, TOKEN_MISC_ATT_ATTRIBUTES, id,
								TOKEN_MIND_TAG_VALTYPE);
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, h, TOKEN_MIND_TAG_COLLTYPE), info, TOKEN_MISC_ATT_ATTRIBUTES, id,
								TOKEN_MIND_TAG_COLLTYPE);

						Collection<DustHandle> at = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_MISC_ATT_APPEARS);
						for (DustHandle t : at) {
							Dust.access(DustAccess.Set, t.getId(), info, TOKEN_MISC_ATT_ATTRIBUTES, id, TOKEN_MISC_ATT_APPEARS, KEY_ADD);
						}
					}
				}

				Dust.access(DustAccess.Set, DustUtils.strTime(), info, TOKEN_MISC_ATT_LASTCHANGED);

				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "After MiND info", DustDevUtils.memInfo());
				loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, machine, TOKEN_DUST_ATT_UNIT_OBJECTS, KEY_MAP_KEYS);
				Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Loaded units", loadedUnits);

				break;
			}
			break;
		default:
			break;
		}

		return null;
	}
}
