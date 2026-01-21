package me.giskard.dust.mind;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.giskard.dust.Dust;
import me.giskard.dust.DustException;
import me.giskard.dust.DustMind;
import me.giskard.dust.dev.DustDevCounter;
import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

//@SuppressWarnings("unchecked")
//@SuppressWarnings("rawtypes")
@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindAgent extends DustMind implements DustMindConsts {

	DustMindIdea typeAtt;
	DustMindIdea typeType;
	DustMindIdea typeUnit;

	DustMindIdea appUnit;
	DustMindIdea mindUnit;
	DustMindIdea metaUnit;

	boolean booted = false;
	DustMindIdea defaultSerializer;

//	DustUtilsFactory<String, DustMindIdea> meta = new DustUtilsFactory<String, DustMindIdea>(new DustCreator<DustMindIdea>() {
//		@Override
//		public DustMindIdea create(Object key, Object... hints) {
//			return new DustMindIdea(DustMindAgent.this, null, null, (String) key);
//		}
//	}, true);

	public DustMindAgent() {
		mindUnit = new DustMindIdea(this, null, null, TOKEN_MIND);
		metaUnit = getUnit(DUST_UNIT_ID, true);

		typeType = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, null, TOKEN_KBMETA_TYPE);
		typeAtt = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, typeType, TOKEN_KBMETA_ATTRIBUTE);
		typeUnit = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, typeType, TOKEN_KBMETA_UNIT);

		typeType.content.put(TOKEN_TYPE, typeType);
		mindUnit.content.put(TOKEN_TYPE, typeUnit);
		metaUnit.content.put(TOKEN_TYPE, typeUnit);

		DustMindIdea attType = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, typeAtt, TOKEN_UNIT);
		DustMindIdea attId = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, typeAtt, TOKEN_TYPE);
		DustMindIdea attUnit = safeGetIdea(metaUnit, TOKEN_MEMBERS, true, typeAtt, TOKEN_ID);

		attType.content.put(TOKEN_FINAL, true);
		attId.content.put(TOKEN_FINAL, true);
		attUnit.content.put(TOKEN_FINAL, true);
	}

	@Override
	protected void init() {
		DustObject mind = getObject(appUnit, null, TOKEN_MIND, DustOptCreate.None);
		defaultSerializer = Dust.access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);
		optLoadUnit(DUST_UNIT_ID, metaUnit);
	}

	@Override
	protected Object checkAccess(DustObject agent, DustAccess acess, DustObject object, DustObject att, Object value) throws RuntimeException {
		Object ret = value;

		if (null != value) {
			if (DustUtils.isChange(acess) && (null != att) && (Boolean) ((DustMindIdea) att).content.getOrDefault(TOKEN_FINAL, Boolean.FALSE)) {
				DustException.wrap(null, "Trying to overwrite a final attribute)");
			}

			Collection c = (Collection) ((DustMindIdea) object).content.get(TOKEN_READABLETO);

			if (null != c) {
				if (!c.contains(agent)) {
					ret = null;
				}
			}
		}
		return ret;
	}

	@Override
	protected DustObject getObject(DustObject unit, DustObject type, String id, DustOptCreate optCreate) {
		Object ret = null;

		if (optCreate == DustOptCreate.Meta) {
			if (null == type) {
				type = typeType;
			}
			if (unit != metaUnit) {
				ret = getObject(metaUnit, type, id, (null == unit) ? optCreate : DustOptCreate.None);
				if (null != ret) {
					return (DustObject) ret;
				}
			}
//		} else {
//			if (null == unit) {
//				DustException.wrap(null, "Missing unit in getObject", type, id);
//			}
		}

		DustMindIdea u = (DustMindIdea) unit;

		int sep = id.indexOf(DUST_SEP_TOKEN);
		if (-1 != sep) {
			String uid = id.substring(0, sep);
			if ((null == u) || !DustUtils.isEqual(u.getId(), uid)) {
				DustMindIdea uu = (DustMindIdea) getUnit(uid, true);
				if ((null == u) || ((uu != u) && (optCreate == DustOptCreate.Meta))) {
					// TODO Should do an auto-copy in the target unit?
//				DustException.wrap(null, "unit mismatch, requested", u.getId(), "for id", id);
					u = uu;
				}
			}
		} else {
			id = u.getId() + DUST_SEP_TOKEN + id;
		}

		ret = safeGetIdea(u, TOKEN_MEMBERS, (optCreate != DustOptCreate.None), type, id);

		return (DustObject) ret;
	}

	private DustMindIdea safeGetIdea(DustMindIdea u, String key, boolean createMissing, DustObject type, String id) {
		DustMindIdea ret;
		synchronized (u) {
			Map m = (Map) u.content.get(key);
			if (null == m) {
				if (!createMissing) {
					return null;
				}
				m = new TreeMap();
				u.content.put(key, m);
			}
			ret = (DustMindIdea) m.get(id);

			if (null == ret) {
				if (!createMissing) {
					return null;
				}
				ret = new DustMindIdea(this, u, type, id);
				m.put(id, ret);

				if ((null != type) && (type == typeUnit)) {
					optLoadUnit(id, ret);
				}
			}
		}
		return ret;
	}

	@Override
	protected DustMindIdea getUnit(String unitId, boolean createIfMissing) {
		DustMindIdea ret = null;

		if (DustUtils.isEmpty(unitId)) {
			if (createIfMissing) {
				ret = new DustMindIdea(this, null, typeUnit, "");
			}
		} else {
			ret = safeGetIdea(mindUnit, TOKEN_KB_KNOWNUNITS, createIfMissing, typeUnit, unitId);
		}

		return ret;
	}

	private void optLoadUnit(String unitId, DustMindIdea unit) {
		Object ser = Dust.access(DustAccess.Peek, defaultSerializer, null, TOKEN_KB_KNOWNUNITS, unitId, TOKEN_SERIALIZER);

		if (null != ser) {
			Map<String, Object> params = new HashMap<>();

			params.put(TOKEN_CMD, TOKEN_CMD_LOAD);
			params.put(TOKEN_KEY, unitId);
			params.put(TOKEN_DATA, unit);

			Dust.access(DustAccess.Process, params, ser);
		}
	}

	@Override
	protected boolean releaseUnit(String unitId) {
		return null != Dust.access(DustAccess.Delete, null, mindUnit, TOKEN_KB_KNOWNUNITS, unitId);
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		switch (access) {
		case Process:
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			switch (cmd) {
			case TOKEN_CMD_INFO:

				DustObject info = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_CMD_INFO);
				String lastChanged = Dust.access(DustAccess.Peek, null, info, TOKEN_LASTCHANGED);

				if (!DustUtils.isEmpty(lastChanged)) {
					return info;
				}

				Dust.log(TOKEN_LEVEL_TRACE, "Before MiND info", DustDevUtils.memInfo());

				Collection<String> loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, mindUnit, TOKEN_KB_KNOWNUNITS, KEY_MAP_KEYS);
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

						Map<String, Object> data = ((DustMindIdea) o).content;

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
								Dust.access(DustAccess.Set, collType, att, TOKEN_VALTYPE);
							}
						}
					}
					
					totalCount += count;
					metaUnitsGlobal.addAll(metaUnits);

					Dust.access(DustAccess.Set, count, info, TOKEN_KB_KNOWNUNITS, un, TOKEN_COUNT);
					
					for ( String mu : metaUnits ) {
						Dust.access(DustAccess.Set, mu, info, TOKEN_KB_KNOWNUNITS, un, TOKEN_META, KEY_ADD);						
					}
					
					for ( Map.Entry<DustObject, Long> cnt : cntUnit ) {
						DustObject o = cnt.getKey();
						String t = types.contains(o) ? TOKEN_TYPES : TOKEN_ATTRIBUTES;
						Dust.access(DustAccess.Set, cnt.getValue(), info, TOKEN_KB_KNOWNUNITS, un, t, o.getId());
					}

					if (notLoaded && !metaUnitsGlobal.contains(un)) {
						Dust.log(TOKEN_LEVEL_TRACE, "Dropping unit", un);
						releaseUnit(un);
					}
				}

				Dust.access(DustAccess.Set, totalCount, info, TOKEN_COUNT);
				
				Map<String, Object> params = new HashMap<>();
				params.put(TOKEN_CMD, TOKEN_CMD_SAVE);
				
				for ( String mu : metaUnitsGlobal ) {
					if ( DustUtils.isEqual(DUST_UNIT_ID, mu)) {
						continue;
					}
					Dust.access(DustAccess.Set, mu, info, TOKEN_META, KEY_ADD);
					params.put(TOKEN_KEY, mu);
					Dust.access(DustAccess.Process, params, ser);
				}
				
				for ( Map.Entry<DustObject, Long> cnt : cntGlobal ) {
					DustObject o = cnt.getKey();
					String t = types.contains(o) ? TOKEN_TYPES : TOKEN_ATTRIBUTES;
					Dust.access(DustAccess.Set, cnt.getValue(), info, t, o.getId());
				}
				
				Dust.access(DustAccess.Set, DustUtils.strTime(), info, TOKEN_LASTCHANGED);

				Dust.log(TOKEN_LEVEL_TRACE, "After MiND info", DustDevUtils.memInfo());
				loadedUnits = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, mindUnit, TOKEN_KB_KNOWNUNITS, KEY_MAP_KEYS);
				Dust.log(TOKEN_LEVEL_TRACE, "Loaded units", loadedUnits);

				break;
			}
			break;
		default:
			break;
		}

		return null;
	}

	@Override
	protected synchronized DustObject bootLoadAppUnitJsonApi(DustObject appUnit, File f) throws Exception {
		if (f.isFile()) {
			if (null == appUnit) {
				String unitId = DustUtils.cutPostfix(f.getName(), ".");
				appUnit = getUnit(unitId, true);
				this.appUnit = (DustMindIdea) appUnit;
			}
			DustMindSerializerJsonApi.loadFile(appUnit, f);
		}

		return appUnit;
	}

}
