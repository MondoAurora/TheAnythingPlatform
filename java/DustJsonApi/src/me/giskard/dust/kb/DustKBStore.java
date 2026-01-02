package me.giskard.dust.kb;

import java.util.Map;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

//@SuppressWarnings("unchecked")
public class DustKBStore extends DustAgent implements DustKBConsts, DustKBConsts.KBStore {

	static DustKBUnit appUnit;

	private Object defaultSerializer;
	private DustKBUnit metaUnit;

	private DustUtilsFactory<String, DustKBUnit> identifiedUnits = new DustUtilsFactory<String, DustKBUnit>(new DustCreator<DustKBUnit>() {
		@Override
		public DustKBUnit create(Object key, Object... hints) {
			DustKBUnit ret = new DustKBUnit(DustKBStore.this, (String) key);

			return ret;
		}

		@Override
		public void initNew(DustKBUnit item, Object key, Object... hints) {
			Object ser = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KB_KNOWNUNITS, TOKEN_SERIALIZER, key);

			if (null == ser) {
				ser = defaultSerializer;
			}

			if (null != ser) {
				Map<String, Object> params = DustUtils.simpleGet(ser, TOKEN_PARAMS);

				params.put(TOKEN_CMD, TOKEN_CMD_LOAD);
				params.put(TOKEN_KEY, key);
				params.put(TOKEN_UNIT, item);
				
				Dust.sendMessage(ser);
			}

		}

	}, true);

	public DustKBStore() {
		identifiedUnits.put(appUnit.getUnitId(), appUnit);
		appUnit.setStore(this);
	}

	@Override
	public String getMetaTypeId(String mt) {
		return metaUnit.getUnitId() + DUST_SEP_TOKEN + mt;
	}

	@Override
	public DustKBUnit getUnit(String unitId, boolean createIfMissing) {
		return DustUtils.isEmpty(unitId) ? new DustKBUnit(this, unitId) : createIfMissing ? identifiedUnits.get(unitId) : identifiedUnits.peek(unitId);
	}

	@Override
	public Iterable<? extends KBUnit> knownUnits() {
		return identifiedUnits.values();
	}

	@Override
	public boolean releaseUnit(String unitId) {
		DustKBUnit u = getUnit(unitId, false);

		if (null != u) {
			identifiedUnits.drop(u);
			Dust.log(TOKEN_LEVEL_INFO, "Unit released", unitId);

			return true;
		}

		return false;
	}

	@Override
	public void reset() {
		identifiedUnits.clear();
		identifiedUnits.put(appUnit.getUnitId(), appUnit);
	}

	@Override
	protected Object process(DustAction action) throws Exception {
		return null;
	}

	protected void init() {
		defaultSerializer = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_SERIALIZER);

		String mm = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_META);
		metaUnit = getUnit(mm, true);
	}

	@Override
	public String toString() {
		StringBuilder sb = null;

		new StringBuilder("Knowledge base [");

		for (KBUnit u : knownUnits()) {
			sb = DustUtils.sbAppend(sb, ", ", false, u.getUnitId() + " (" + u.size() + ")");
		}

		return "Knowledge base [" + DustUtils.toString(sb) + "]";
	}
}
