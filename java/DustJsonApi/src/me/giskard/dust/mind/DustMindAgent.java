package me.giskard.dust.mind;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import me.giskard.dust.Dust;
import me.giskard.dust.DustException;
import me.giskard.dust.DustMind;
import me.giskard.dust.utils.DustUtils;

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
		DustObject mind = getObject(appUnit, null, "MiND", DustOptCreate.None);
		defaultSerializer = Dust.access(DustAccess.Peek, mindUnit, mind, TOKEN_SERIALIZER);
		optLoadUnit(DUST_UNIT_ID, metaUnit);
	}

	@Override
	protected Object checkAccess(DustObject agent, DustAccess acess, DustObject object, DustObject att, Object value) throws RuntimeException {
		Object ret = value;

		if (null != value) {
			if (DustUtils.isChange(acess) && (Boolean) ((DustMindIdea) att).content.getOrDefault(TOKEN_FINAL, Boolean.FALSE)) {
				DustException.wrap(null, "Trying to overwrite a final attribute)");
			}
			
			Collection c = (Collection) ((DustMindIdea) object).content.get(TOKEN_READABLETO);
			
			if ( null != c) {
				if ( !c.contains(agent) ) {
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
		} else {
			if (null == unit) {
				DustException.wrap(null, "Missing unit in getObject", type, id);
			}
		}

		DustMindIdea u = (DustMindIdea) unit;

		int sep = id.indexOf(DUST_SEP_TOKEN);
		if (-1 != sep) {
			String uid = id.substring(0, sep);
			if (!DustUtils.isEqual(u.getId(), uid)) {
				DustMindIdea uu = (DustMindIdea) getUnit(uid, true);
				if ((null != u) && (uu != u) && (optCreate == DustOptCreate.Meta)) {
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
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
