package me.giskard.dust.mind;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

	DustMindHandle typeType;
	DustMindHandle typeAtt;
	DustMindHandle typeUnit;

	DustMindIdea unitApp;
	DustMindIdea unitMind;
	DustMindIdea unitMeta;

	DustMindHandle defaultSerializer;

	DustCreator<DustMindHandle> createHandle = new DustCreator<DustMindHandle>() {
		@Override
		public DustMindHandle create(Object key, Object... hints) {
			String k = (String) key;

//			if (k.startsWith("SAPMeta")) {
//				Dust.log("hmm");
//			}
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
		defaultSerializer = Dust.access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);
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

	@Override
	protected Map getContent(DustHandle h) {
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
		Object ser = Dust.access(DustAccess.Peek, defaultSerializer, unitMind, TOKEN_UNIT_OBJECTS, unitId, TOKEN_SERIALIZER);

		if ((null != ser) && !DustUtils.isEmpty(unitId)) {
			Map<String, Object> params = new HashMap<>();

			params.put(TOKEN_CMD, TOKEN_CMD_LOAD);
			params.put(TOKEN_KEY, unitId);
			params.put(TOKEN_DATA, unit.mh);

			Dust.access(DustAccess.Process, params, ser);
		}
	}

	@Override
	protected boolean releaseUnit(DustHandle unit) {
		return (null == unit) ? false : null != Dust.access(DustAccess.Delete, null, unitMind.content, TOKEN_UNIT_OBJECTS, unit);
	}

	@Override
	protected Object checkAccess(DustHandle agent, DustAccess acess, DustHandle object, DustHandle att, Object value) throws RuntimeException {
		Object ret = value;
		Map m;

		if (null != value) {
			if (DustUtils.isChange(acess) && (null != att)) {
				m = getContent(att);
				if ((Boolean) m.getOrDefault(TOKEN_FINAL, Boolean.FALSE)) {
					DustException.wrap(null, "Trying to overwrite a final attribute)");
				}
			}
			m = getContent(object);

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

				Object ser = Dust.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);
				Map<String, Object> p = new HashMap<>();
				p.put(TOKEN_CMD, TOKEN_CMD_INFO);

				Dust.access(DustAccess.Process, p, ser);

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
					params.put(TOKEN_KEY, mu);
					Dust.access(DustAccess.Process, params, ser);
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

				params.put(TOKEN_KEY, uid);
				Dust.access(DustAccess.Process, params, ser);

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
