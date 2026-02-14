package me.giskard.dust.mind;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import me.giskard.dust.Dust;
import me.giskard.dust.DustException;
import me.giskard.dust.DustMind;
import me.giskard.dust.dev.DustDevCounter;
import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.stream.DustStreamJsonApiSerializerAgent;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindAgent extends DustMind implements DustMindConsts {

	DustUtilsFactory<DustContext, Object> CTX = new DustUtilsFactory(MAP_CREATOR);

	ThreadLocal<Set<DustHandle>> loadingUnit = new ThreadLocal<Set<DustHandle>>() {
		protected Set<DustHandle> initialValue() {
			return new HashSet<DustHandle>();
		};
	};

	public <RetType> RetType optGetCtx(Object in) {
		return (RetType) ((in instanceof DustContext) ? CTX.get((DustContext) in) : in);
	}

	public <RetType> RetType peekCtx(DustContext dc) {
		return (RetType) CTX.peek(dc);
	}

	DustMindHandle typeType;
	DustMindHandle typeAtt;
	DustMindHandle typeUnit;

	DustMindIdea unitApp;
	DustMindIdea unitMind;
	DustMindIdea unitMeta;

	DustMindHandle defaultSerializer;
	Set<DustHandle> changedUnits = new HashSet<>();

	DustCreator<DustMindHandle> createHandle = new DustCreator<DustMindHandle>() {
		@Override
		public DustMindHandle create(Object key, Object... hints) {
			String k = (String) key;
			return new DustMindHandle(DustMindAgent.this, (DustMindIdea) hints[0], (DustMindHandle) hints[1], k);
		}
	};

	DustCreator<DustMindIdea> createIdea = new DustCreator<DustMindIdea>() {
		@Override
		public DustMindIdea create(Object key, Object... hints) {
			DustMindHandle h = (DustMindHandle) key;

			DustMindIdea ret = new DustMindIdea(h);

			if (DustUtils.isEqual(typeUnit, h.getType())) {
				initUnit(ret, false);
			}

			return ret;
		}

		@Override
		public void initNew(DustMindIdea item, Object key, Object... hints) {
			DustHandle type = item.mh.getType();
			if ((null != type) && DustUtils.isEqual(typeUnit, type)) {
				optLoadUnit(item.mh.getId(), item);
			}
		};
	};

	public DustMindAgent() {
		unitMind = new DustMindIdea(new DustMindHandle(this, null, null, NAME_MIND));
		initUnit(unitMind, true);
		unitMeta = new DustMindIdea(new DustMindHandle(this, unitMind, null, DUST_UNIT_ID));
		initUnit(unitMeta, false);

		((Map) unitMind.content.get(TOKEN_UNIT_REFS)).put(DUST_UNIT_ID, unitMeta.mh);
		((Map) unitMind.content.get(TOKEN_UNIT_OBJECTS)).put(unitMeta.mh, unitMeta);

		typeType = safeGetIdea(unitMeta, null, TOKEN_KBMETA_TYPE, DustOptCreate.Meta).mh;
		typeAtt = safeGetIdea(unitMeta, typeType, TOKEN_KBMETA_ATTRIBUTE, DustOptCreate.Meta).mh;
		typeUnit = safeGetIdea(unitMeta, typeType, TOKEN_KBMETA_UNIT, DustOptCreate.Meta).mh;

		typeType.init(unitMeta, typeType, TOKEN_KBMETA_TYPE);

		unitMind.mh.init(unitMind, typeUnit, NAME_MIND);
		unitMind.loadMh();
		unitMeta.mh.init(unitMind, typeUnit, DUST_UNIT_ID);
		unitMeta.loadMh();
	}

	@Override
	protected void init() {
		DustHandle mind = getHandle(unitApp.mh, null, TOKEN_MIND, DustOptCreate.None);
		defaultSerializer = access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);
		optLoadUnit(DUST_UNIT_ID, unitMeta);
	}

	private void initUnit(DustMindIdea iUnit, boolean weak) {
		iUnit.content.put(TOKEN_UNIT_OBJECTS, weak ? new WeakHashMap() : new HashMap());
		iUnit.content.put(TOKEN_UNIT_REFS, new TreeMap());
	}

	@Override
	protected DustMindHandle getHandle(DustHandle unit, DustHandle type, String id, DustOptCreate optCreate) {
		DustMindHandle ret = null;

		DustMindHandle u = (DustMindHandle) unit;
		int sep = id.indexOf(DUST_SEP_TOKEN);

		if (optCreate == DustOptCreate.Meta) {
			if (null == type) {
				type = typeType;
			}
			if (unit != unitMeta.mh) {
				ret = getHandle(unitMeta.mh, type, id, (null == unit) ? DustOptCreate.Meta : DustOptCreate.None);
			}

			if (null == ret) {
				if (-1 != sep) {
					String uid = id.substring(0, sep);
					if ((null == u) || !DustUtils.isEqual(u.getId(), uid)) {
						DustMindHandle uu = (DustMindHandle) getUnit(uid, true);
						if ((null == u) || (uu != u)) {
							u = uu;
						}
					}
				}
			}
		}

		if (null == ret) {
			DustMindIdea ui;

			if (DustUtils.isEqual(typeUnit, type)) {
				ui = unitMind;
			} else {
				if (-1 == sep) {
					id = u.getId() + DUST_SEP_TOKEN + id;
				}
				ui = safeGetIdea(unitMind, u);
			}

			Map<String, DustMindHandle> unitRefs = (Map) ui.content.get(TOKEN_UNIT_REFS);

			ret = (optCreate == DustOptCreate.None) ? unitRefs.get(id) : DustUtils.safeGet(unitRefs, createHandle, id, ui, type);
		}
		return ret;
	}

	Map getContent(DustHandle h) {
		DustMindHandle mh = (DustMindHandle) h;
		DustMindIdea idea = safeGetIdea(mh.getUnitIdea(), mh);
		return idea.getContent();
	}

	private DustMindIdea safeGetIdea(DustMindIdea unit, DustHandle type, String id, DustOptCreate optCreate) {
		DustMindIdea ret = null;

		synchronized (unit) {
			DustMindHandle mh = getHandle(unit.mh, type, id, optCreate);

			if (null != mh) {
				ret = safeGetIdea(unit, mh);
			}
		}
		return ret;
	}

	private DustMindIdea safeGetIdea(DustMindIdea unit, DustMindHandle mh) {
		DustMindIdea ret = null;
		Map mOb = (Map) unit.content.get(TOKEN_UNIT_OBJECTS);

		synchronized (mOb) {
			ret = DustUtils.safeGet(mOb, createIdea, mh);
		}
		return ret;
	}

	@Override
	protected DustMindHandle getUnit(String unitId, boolean createIfMissing) {
		DustMindIdea ui = getUnitIdea(unitId, createIfMissing);
		return (null == ui) ? null : ui.mh;
	}

	protected DustMindIdea getUnitIdea(String unitId, boolean createIfMissing) {
		DustMindIdea ret = null;
		DustMindHandle mh = null;

		if (DustUtils.isEmpty(unitId)) {
			if (createIfMissing) {
				mh = new DustMindHandle(this, unitMind, typeUnit, "");
			}
		} else {
			mh = getHandle(unitMind.mh, typeUnit, unitId, createIfMissing ? DustOptCreate.Primary : DustOptCreate.None);
		}

		if (null != mh) {
			ret = safeGetIdea(unitMind, mh);
		}

		return ret;
	}

	private void optLoadUnit(String unitId, DustMindIdea unit) {
		Object ser = access(DustAccess.Peek, defaultSerializer, unitMind, TOKEN_UNIT_OBJECTS, unitId, TOKEN_SERIALIZER);

		if ((null != ser) && !DustUtils.isEmpty(unitId)) {
			Map<String, Object> params = new HashMap<>();

			params.put(TOKEN_CMD, TOKEN_CMD_LOAD);
			params.put(TOKEN_KEY, unitId);
			params.put(TOKEN_DATA, unit.mh);

			try {
				loadingUnit.get().add(unit.mh);
				access(DustAccess.Process, params, ser);
			} finally {
				loadingUnit.get().remove(unit.mh);
			}
		}
	}

	@Override
	protected boolean releaseUnit(DustHandle unit) {
		if (loadingUnit.get().contains(unit)) {
			Dust.log(TOKEN_LEVEL_INFO, "Skip release unit because it changed", unit.getId());
			return false;
		}

		return (null == unit) ? false : null != access(DustAccess.Delete, null, unitMind.content, TOKEN_UNIT_OBJECTS, unit);
	}

	@Override
	protected <RetType> RetType notifyAgent(DustHandle hAgent, DustAction action, DustAccess access, DustHandle service, Object params) {
		String agent = hAgent.getId();
		Dust.log(TOKEN_LEVEL_TRACE, "Message to agent", agent, "service", service, "params", params);

		long start = System.currentTimeMillis();
		Object ret = null;
		DustUtilsFactory<DustContext, Object> ctx = CTX;
		Set<DustHandle> chg = changedUnits;

		try {
			CTX = new DustUtilsFactory(MAP_CREATOR);
			CTX.put(DustContext.Agent, hAgent);
			CTX.put(DustContext.Service, service);
			CTX.put(DustContext.Input, params);

			changedUnits = new HashSet<>();

			ret = super.callAgent(hAgent, action, access);

			Map<String, Object> sp = null;

			for (DustHandle hChg : changedUnits) {
				if (unitApp.mh == hChg) {
					continue;
				}

//				Dust.log(TOKEN_LEVEL_INFO, "Would save changed unit", hChg.getId());

				if (null == sp) {
					sp = new HashMap<String, Object>();
					sp.put(TOKEN_CMD, TOKEN_CMD_SAVE);
				}

				sp.put(TOKEN_KEY, hChg.getId());
				sp.put(TOKEN_DATA, hChg);
				Dust.access(DustAccess.Process, sp, defaultSerializer);
			}
		} catch (Throwable e) {
			DustException.wrap(e, "sendMessage failed", agent, "service", service, "params", params);
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Message processed", System.currentTimeMillis() - start, "msec.");
			CTX = ctx;
			changedUnits = chg;
		}

		return (RetType) ret;
	}

	private void registerChange(DustHandle agent, DustAccess acess, DustHandle handle, DustHandle att, Object lastKey, Object oldVal, Object newVal) throws RuntimeException {
		checkAccess(agent, acess, handle, att, lastKey, newVal);

		DustHandle hUnit = handle.getUnit();

		if ((unitApp.mh == hUnit) || loadingUnit.get().contains(hUnit)) {
			return;
		}

		changedUnits.add(hUnit);
	}

	@Override
	public <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		DustHandle agent = (DustHandle) CTX.peek(DustContext.Agent);

		if ((null == root) || (root instanceof DustContext)) {
			return accessCtx(access, agent, val, (DustContext) root, path);
		}

		Object curr = root;

		DustCollType collType = DustUtils.getCollType(root);

		Object ret = null;

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

				if ((KEY_SIZE == idx)) {
					curr = al.size();
				} else if ((KEY_ADD == idx) || (idx >= al.size())) {
					curr = null;
				} else {
					curr = al.get(idx);
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

		switch (access) {
		case Delete:
			if (curr != null) {
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

			break;
		case Insert:
			switch (collType) {
			case Arr:
				change = true;
				break;
			case Map:
				change = (curr instanceof Set) ? !((Set)curr).contains(val) : !DustUtils.isEqual(curr, val);
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
			ret = DustUtils.isEqual(val, curr);
			break;
		case Delete:
			if (curr != null) {
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
			Object ll = access(DustAccess.Peek, null, curr, TOKEN_LISTENERS);
			if (ll instanceof Collection) {
				for (Object l : (Collection) ll) {
					ret = notifyAgent((DustHandle) l, action, access, (DustHandle) curr, val);
				}
			}
		}

		return (RetType) ret;

	}

	public <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		DustHandle agent = peekCtx(DustContext.Agent);
		return accessCtx(access, agent, val, root, path);
	}

	private <RetType> RetType accessCtx(DustAccess access, DustHandle agent, Object val, Object root, Object... path) {
		Object ret = NOT_FOUND;

		Object main = optGetCtx(root);
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
				Object ctx = peekCtx(dc);
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
				if ((Boolean) m.getOrDefault(TOKEN_FINAL, Boolean.FALSE)) {
					DustException.wrap(null, "Trying to overwrite a final attribute)");
				}
			}
			m = getContent(handle);

			Collection c = (Collection) m.get(TOKEN_READABLETO);

			if (null != c) {
				if (!c.contains(agent)) {
					ret = null;
				}
			}
		}
		return ret;
	}

	@Override
	protected synchronized DustHandle bootLoadAppUnit(DustHandle appUnit, File f) throws Exception {
		if (f.isFile()) {
			if (null == appUnit) {
				String unitId = DustUtils.cutPostfix(f.getName(), ".");
				this.unitApp = getUnitIdea(unitId, true);
				appUnit = this.unitApp.mh;
			}
			DustStreamJsonApiSerializerAgent.loadFile(appUnit, f);
		}

		return appUnit;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		switch (access) {
		case Process:
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			switch (cmd) {
			case TOKEN_CMD_INFO:

				DustHandle info = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_CMD_INFO);
				String iid = info.getId();
				String uid = DustUtils.getPrefix(iid, DUST_SEP_TOKEN);
				DustHandle unitInfo = Dust.getUnit(uid, true);
				info = getHandle(unitInfo, info.getType(), iid, DustOptCreate.Primary);
//				String lastChanged = Dust.access(DustAccess.Peek, null, info, TOKEN_LASTCHANGED);

//				if (!DustUtils.isEmpty(lastChanged)) {
//					return info;
//				}

				Dust.log(TOKEN_LEVEL_TRACE, "Before MiND info", DustDevUtils.memInfo());

				Collection<String> loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, unitMind, TOKEN_UNIT_OBJECTS, KEY_MAP_KEYS);
				Dust.log(TOKEN_LEVEL_TRACE, "Loaded units", loadedUnits);

				Map<String, Object> p = new HashMap<>();
				p.put(TOKEN_CMD, TOKEN_CMD_INFO);

				Dust.access(DustAccess.Process, p, defaultSerializer);

				Collection<String> unitNames = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, p, TOKEN_MEMBERS);

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

				for (String un : unitNames) {
					if (DustUtils.isEqual(uid, un)) {
						Dust.log(TOKEN_LEVEL_TRACE, "SKIPPING unit info", un);
						continue;
					}

					if (DustUtils.isEqual(DUST_UNIT_ID, un)) {
						Dust.log(TOKEN_LEVEL_TRACE, "SKIPPING unit info", un);
						continue;
					}

					Dust.log(TOKEN_LEVEL_TRACE, "Loading unit info", un);
					boolean notLoaded = !loadedUnits.contains(un);

					DustHandle u = getUnit(un, notLoaded);
					cntUnit.reset();
					Set<String> metaUnits = new HashSet<>();

					int count = 0;

					for (DustHandle h : DustMindUtils.getUnitMembers(u)) {
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
								collType = TOKEN_COLLTYPE_SET;
							} else if (val instanceof Collection) {
								collType = TOKEN_COLLTYPE_ARRAY;
							} else if (val instanceof Map) {
								collType = TOKEN_COLLTYPE_MAP;
							}

							if (null != collType) {
								val = DustUtils.getSample(val);
							}

							if (null != val) {
								if (val instanceof String) {
									String sVal = ((String) val).toLowerCase().trim();

									if (-1 != DustUtils.indexOf(sVal, DustUtils.DUST_BOOL)) {
										valType = TOKEN_VALTYPE_BOOL;
									} else {
										valType = TOKEN_VALTYPE_STRING;
									}
								} else if (val instanceof DustHandle) {
									valType = TOKEN_VALTYPE_REFERENCE;
								} else if (val instanceof Long) {
									valType = TOKEN_VALTYPE_LONG;
								} else if (val instanceof Double) {
									valType = TOKEN_VALTYPE_REAL;
								} else {
									valType = TOKEN_VALTYPE_RAW;
								}
							}

							Dust.access(DustAccess.Set, valType, att, TOKEN_VALTYPE);
							if (null != collType) {
								Dust.access(DustAccess.Set, collType, att, TOKEN_COLLTYPE);
							}
						}
					}

					totalCount += count;
					metaUnitsGlobal.addAll(metaUnits);

					Dust.access(DustAccess.Set, count, info, TOKEN_KB_KNOWNUNITS, un, TOKEN_COUNT);

					for (String mu : metaUnits) {
						Dust.access(DustAccess.Set, mu, info, TOKEN_KB_KNOWNUNITS, un, TOKEN_META, KEY_ADD);
					}

					for (Map.Entry<DustHandle, Long> cnt : cntUnit) {
						DustHandle h = cnt.getKey();
						String t = types.contains(h) ? TOKEN_TYPES : TOKEN_ATTRIBUTES;
						Dust.access(DustAccess.Set, cnt.getValue(), info, TOKEN_KB_KNOWNUNITS, un, t, h.getId());
					}

					if (notLoaded && !metaUnitsGlobal.contains(un)) {
						Dust.log(TOKEN_LEVEL_TRACE, "Dropping unit", un);
						releaseUnit(u);
					}
				}

				Dust.access(DustAccess.Set, totalCount, info, TOKEN_COUNT);

				Map<String, Object> params = new HashMap<>();
				params.put(TOKEN_CMD, TOKEN_CMD_SAVE);

				for (String mu : metaUnitsGlobal) {
					if (DustUtils.isEqual(DUST_UNIT_ID, mu)) {
						continue;
					}
					Dust.access(DustAccess.Set, mu, info, TOKEN_META, KEY_ADD);
				}

				for (Map.Entry<DustHandle, Long> cnt : cntGlobal) {
					DustHandle h = cnt.getKey();
					String id = h.getId();

					if (types.contains(h)) {
						Map<String, DustHandle> cm = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, h, TOKEN_CHILDMAP);

						for (Map.Entry<String, DustHandle> ce : cm.entrySet()) {
							Dust.access(DustAccess.Set, ce.getValue().getId(), info, TOKEN_TYPES, id, TOKEN_CHILDMAP, ce.getKey());
						}
					} else {
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, h, TOKEN_VALTYPE), info, TOKEN_ATTRIBUTES, id, TOKEN_VALTYPE);
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, h, TOKEN_COLLTYPE), info, TOKEN_ATTRIBUTES, id, TOKEN_COLLTYPE);

						Collection<DustHandle> at = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_APPEARS);
						for (DustHandle t : at) {
							Dust.access(DustAccess.Set, t.getId(), info, TOKEN_ATTRIBUTES, id, TOKEN_APPEARS, KEY_ADD);
						}
					}
				}

				Dust.access(DustAccess.Set, DustUtils.strTime(), info, TOKEN_LASTCHANGED);

				Dust.log(TOKEN_LEVEL_TRACE, "After MiND info", DustDevUtils.memInfo());
				loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, unitMind, TOKEN_UNIT_OBJECTS, KEY_MAP_KEYS);
				Dust.log(TOKEN_LEVEL_TRACE, "Loaded units", loadedUnits);

				break;
			}
			break;
		default:
			break;
		}

		return null;
	}
}
