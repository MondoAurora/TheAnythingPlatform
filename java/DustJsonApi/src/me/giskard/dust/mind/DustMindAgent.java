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

	DustMindObject typeType;
	DustMindObject typeAtt;
	DustMindObject typeUnit;

	DustMindIdea unitApp;
	DustMindIdea unitMind;
	DustMindIdea unitMeta;

	DustMindObject defaultSerializer;

	DustCreator<DustMindObject> createObject = new DustCreator<DustMindObject>() {
		@Override
		public DustMindObject create(Object key, Object... hints) {
			String k = (String) key;

//			if (k.startsWith("SAPMeta")) {
//				Dust.log("hmm");
//			}
			return new DustMindObject(DustMindAgent.this, (DustMindIdea) hints[0], (DustMindObject) hints[1], k);
		}
	};

	DustCreator<DustMindIdea> createIdea = new DustCreator<DustMindIdea>() {
		@Override
		public DustMindIdea create(Object key, Object... hints) {
			DustMindObject ob = (DustMindObject) key;

			DustMindIdea ret = new DustMindIdea(ob);

			if (DustUtils.isEqual(typeUnit, ob.getType())) {
				initUnit(ret, false);
			}

			return ret;
		}

		@Override
		public void initNew(DustMindIdea item, Object key, Object... hints) {
			DustObject type = item.dmo.getType();
			if ((null != type) && DustUtils.isEqual(typeUnit, type)) {
				optLoadUnit(item.dmo.getId(), item);
			}
		};
	};

	public DustMindAgent() {
		unitMind = new DustMindIdea(new DustMindObject(this, null, null, NAME_MIND));
		initUnit(unitMind, true);
		unitMeta = new DustMindIdea(new DustMindObject(this, unitMind, null, DUST_UNIT_ID));
		initUnit(unitMeta, false);

		((Map) unitMind.content.get(TOKEN_UNIT_REFS)).put(DUST_UNIT_ID, unitMeta.dmo);
		((Map) unitMind.content.get(TOKEN_UNIT_OBJECTS)).put(unitMeta.dmo, unitMeta);

		typeType = safeGetIdea(unitMeta, null, TOKEN_KBMETA_TYPE, DustOptCreate.Meta).dmo;
		typeAtt = safeGetIdea(unitMeta, typeType, TOKEN_KBMETA_ATTRIBUTE, DustOptCreate.Meta).dmo;
		typeUnit = safeGetIdea(unitMeta, typeType, TOKEN_KBMETA_UNIT, DustOptCreate.Meta).dmo;

		typeType.init(unitMeta, typeType, TOKEN_KBMETA_TYPE);

		unitMind.dmo.init(unitMind, typeUnit, NAME_MIND);
		unitMind.loadDmo();
		unitMeta.dmo.init(unitMind, typeUnit, DUST_UNIT_ID);
		unitMeta.loadDmo();
	}

	@Override
	protected void init() {
		DustObject mind = getObject(unitApp.dmo, null, TOKEN_MIND, DustOptCreate.None);
		defaultSerializer = Dust.access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);
		optLoadUnit(DUST_UNIT_ID, unitMeta);
	}

	private void initUnit(DustMindIdea iUnit, boolean weak) {
		iUnit.content.put(TOKEN_UNIT_OBJECTS, weak ? new WeakHashMap() : new HashMap());
		iUnit.content.put(TOKEN_UNIT_REFS, new TreeMap());
	}

	@Override
	protected DustMindObject getObject(DustObject unit, DustObject type, String id, DustOptCreate optCreate) {
		DustMindObject ret = null;

		DustMindObject u = (DustMindObject) unit;
		int sep = id.indexOf(DUST_SEP_TOKEN);

		if (optCreate == DustOptCreate.Meta) {
			if (null == type) {
				type = typeType;
			}
			if (unit != unitMeta.dmo) {
				ret = getObject(unitMeta.dmo, type, id, (null == unit) ? DustOptCreate.Meta : DustOptCreate.None);
			}

			if (null == ret) {
				if (-1 != sep) {
					String uid = id.substring(0, sep);
					if ((null == u) || !DustUtils.isEqual(u.getId(), uid)) {
						DustMindObject uu = (DustMindObject) getUnit(uid, true);
						if ((null == u) || ((uu != u) && (optCreate == DustOptCreate.Meta))) {
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

			Map<String, DustMindObject> unitRefs = (Map) ui.content.get(TOKEN_UNIT_REFS);

			ret = (optCreate == DustOptCreate.None) ? unitRefs.get(id) : DustUtils.safeGet(unitRefs, createObject, id, ui, type);
		}
		return ret;
	}

	@Override
	protected Map getContent(DustObject ob) {
		DustMindObject mo = (DustMindObject) ob;
		DustMindIdea idea = safeGetIdea(mo.getUnitIdea(), mo);
		return idea.getContent();
	}

	private DustMindIdea safeGetIdea(DustMindIdea unit, DustObject type, String id, DustOptCreate optCreate) {
		DustMindIdea ret = null;

		synchronized (unit) {
			DustMindObject ob = getObject(unit.dmo, type, id, optCreate);

			if (null != ob) {
				Map mOb = (Map) unit.content.get(TOKEN_UNIT_OBJECTS);
				ret = DustUtils.safeGet(mOb, createIdea, ob);
			}
		}
		return ret;
	}

	private DustMindIdea safeGetIdea(DustMindIdea unit, DustMindObject ob) {
		DustMindIdea ret = null;
		Map mOb = (Map) unit.content.get(TOKEN_UNIT_OBJECTS);

		synchronized (mOb) {
			ret = DustUtils.safeGet(mOb, createIdea, ob);
		}
		return ret;
	}

	@Override
	protected DustMindObject getUnit(String unitId, boolean createIfMissing) {
		DustMindIdea ui = getUnitIdea(unitId, createIfMissing);
		return (null == ui) ? null : ui.dmo;
	}

	protected DustMindIdea getUnitIdea(String unitId, boolean createIfMissing) {
		DustMindIdea ret = null;
		DustMindObject ob = null;

		if (DustUtils.isEmpty(unitId)) {
			if (createIfMissing) {
				ob = new DustMindObject(this, unitMind, typeUnit, "");
			}
		} else {
			ob = getObject(unitMind.dmo, typeUnit, unitId, createIfMissing ? DustOptCreate.Primary : DustOptCreate.None);
		}

		if (null != ob) {
			ret = safeGetIdea(unitMind, ob);
		}

		return ret;
	}

	private void optLoadUnit(String unitId, DustMindIdea unit) {
		Object ser = Dust.access(DustAccess.Peek, defaultSerializer, unitMind, TOKEN_UNIT_OBJECTS, unitId, TOKEN_SERIALIZER);

		if ((null != ser) && !DustUtils.isEmpty(unitId)) {
			Map<String, Object> params = new HashMap<>();

			params.put(TOKEN_CMD, TOKEN_CMD_LOAD);
			params.put(TOKEN_KEY, unitId);
			params.put(TOKEN_DATA, unit.dmo);

			Dust.access(DustAccess.Process, params, ser);
		}
	}

	@Override
	protected boolean releaseUnit(DustObject unit) {
		return (null == unit) ? false : null != Dust.access(DustAccess.Delete, null, unitMind.content, TOKEN_UNIT_OBJECTS, unit);
	}

	@Override
	protected Object checkAccess(DustObject agent, DustAccess acess, DustObject object, DustObject att, Object value) throws RuntimeException {
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
	protected synchronized DustObject bootLoadAppUnit(DustObject appUnit, File f) throws Exception {
		if (f.isFile()) {
			if (null == appUnit) {
				String unitId = DustUtils.cutPostfix(f.getName(), ".");
				this.unitApp = getUnitIdea(unitId, true);
				appUnit = this.unitApp.dmo;
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

				DustObject info = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_CMD_INFO);
				String iid = info.getId();
				String uid = DustUtils.getPrefix(iid, DUST_SEP_TOKEN);
				DustObject unitInfo = Dust.getUnit(uid, true);
				info = getObject(unitInfo, info.getType(), iid, DustOptCreate.Primary);
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

				DustUtilsFactory<String, DustObject> atts = new DustUtilsFactory<String, DustObject>(new DustCreator<DustObject>() {
					@Override
					public DustObject create(Object key, Object... hints) {
						return getObject(null, typeAtt, (String) key, DustOptCreate.Meta);
					}
				});

				DustDevCounter<DustObject> cntGlobal = new DustDevCounter<DustObject>(false);
				DustDevCounter<DustObject> cntUnit = new DustDevCounter<DustObject>(false);
				Set<DustObject> types = new HashSet<DustObject>();
				Set<String> metaUnitsGlobal = new HashSet<>();
				int totalCount = 0;

				for (String un : unitNames) {
					if (DustUtils.isEqual(uid, un)) {
						Dust.log(TOKEN_LEVEL_TRACE, "SKIPPING unit info", un);
						continue;
					}

					Dust.log(TOKEN_LEVEL_TRACE, "Loading unit info", un);
					boolean notLoaded = !loadedUnits.contains(un);

					DustObject u = getUnit(un, notLoaded);
					cntUnit.reset();
					Set<String> metaUnits = new HashSet<>();

					int count = 0;

					for (DustObject o : DustMindUtils.getUnitMembers(u)) {
						++count;

						DustObject type = o.getType();
						types.add(type);

						metaUnits.add(type.getUnit().getId());

						cntUnit.add(type);
						cntGlobal.add(type);

						Map<String, Object> data = getContent(o);

						for (Map.Entry<String, Object> de : data.entrySet()) {
							String attName = de.getKey();
							DustObject att = atts.get(attName);

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
								} else if (val instanceof DustObject) {
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

					for (Map.Entry<DustObject, Long> cnt : cntUnit) {
						DustObject o = cnt.getKey();
						String t = types.contains(o) ? TOKEN_TYPES : TOKEN_ATTRIBUTES;
						Dust.access(DustAccess.Set, cnt.getValue(), info, TOKEN_KB_KNOWNUNITS, un, t, o.getId());
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

				for (Map.Entry<DustObject, Long> cnt : cntGlobal) {
					DustObject o = cnt.getKey();
					String id = o.getId();

					if (types.contains(o)) {
						Map<String, DustObject> cm = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, o, TOKEN_CHILDMAP);

						for (Map.Entry<String, DustObject> ce : cm.entrySet()) {
							Dust.access(DustAccess.Set, ce.getValue().getId(), info, TOKEN_TYPES, id, TOKEN_CHILDMAP, ce.getKey());
						}
					} else {
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, o, TOKEN_VALTYPE), info, TOKEN_ATTRIBUTES, id, TOKEN_VALTYPE);
						Dust.access(DustAccess.Set, Dust.access(DustAccess.Peek, null, o, TOKEN_COLLTYPE), info, TOKEN_ATTRIBUTES, id, TOKEN_COLLTYPE);

						Collection<DustObject> at = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, o, TOKEN_APPEARS);
						for (DustObject t : at) {
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
