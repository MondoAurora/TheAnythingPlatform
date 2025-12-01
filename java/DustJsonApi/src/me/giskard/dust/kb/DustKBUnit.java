package me.giskard.dust.kb;

import java.util.HashSet;
import java.util.Set;

import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

class DustKBUnit implements DustKBConsts, DustKBConsts.KBUnit {

	private KBStore store;
	private String unitId;

	private DustUtilsFactory<String, KBObject> content = new DustUtilsFactory<String, KBObject>(new DustCreator<KBObject>() {
		@Override
		public KBObject create(Object key, Object... hints) {
			KBObject ret = new DustKBObject(DustKBUnit.this, (String) hints[0], (String) hints[1]);

			if (KBOptCreate.Reference == DustUtils.optGet(hints, 2, KBOptCreate.Primary)) {
				included.add(ret);
			}

			return ret;
		}
	}, false);

	private Set<KBObject> included = new HashSet<>();

	DustKBUnit(KBStore store, String unitId) {
		this.store = store;
		this.unitId = unitId;
	}

	@Override
	public KBStore getStore() {
		return store;
	}

	@Override
	public String getUnitId() {
		return unitId;
	}

	@Override
	public KBObject getObject(String type, String id, KBOptCreate optCreate) {
		String key = type + DUST_SEP_ID + id;

		if (optCreate == KBOptCreate.None) {
			return content.peek(key);
		} else {
			KBObject ret = content.get(key, type, id, optCreate);

			if (optCreate == KBOptCreate.Primary) {
				included.remove(ret);
			}

			return ret;
		}
	}

	void setStore(KBStore s) {
		store = s;
	}

	@Override
	public Iterable<? extends KBObject> objects() {
		return content.values();
	}
	
	@Override
	public int size() {
		return content.size();
	}
}
