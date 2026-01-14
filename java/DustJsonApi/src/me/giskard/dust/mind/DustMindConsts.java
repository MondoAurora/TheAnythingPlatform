package me.giskard.dust.mind;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.DustConsts;
import me.giskard.dust.utils.DustUtilsConsts;

public interface DustMindConsts extends DustConsts, DustUtilsConsts {
	
	String TOKEN_MIND = DUST_UNIT_ID + DUST_SEP_TOKEN + "MiND";
	
	String TOKEN_KB_KNOWNUNITS = DUST_UNIT_ID + DUST_SEP_TOKEN + "knownUnits";
	
	String TOKEN_KBMETA_UNIT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Unit";
	String TOKEN_KBMETA_TYPE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Type";
	String TOKEN_KBMETA_ATTRIBUTE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Attribute";
	String TOKEN_KBMETA_TAG = DUST_UNIT_ID + DUST_SEP_TOKEN + "Tag";
	
	enum KBCollType {
		One, Set, Arr, Map;
		
		public static KBCollType getCollType(Object coll) {
			KBCollType ret = (null == coll) ? KBCollType.One
					: (coll instanceof ArrayList) ? KBCollType.Arr
							: (coll instanceof Map) ? KBCollType.Map : (coll instanceof Set) ? KBCollType.Set : KBCollType.One;

			return ret;
		}

	};
	
	enum KBOptCreate {
		Primary, Reference, None
	}

	public interface KBUnit {
//		KBStore getStore();
		String getUnitId();
		Iterable<? extends DustObject> objects();
		int size();

		DustObject getObject(String type, String id, KBOptCreate optCreate);

		default DustObject getObject(String type, String id)  {
			return getObject(type, id, KBOptCreate.Primary);
		}
	}

	public interface KBStore {
		String getMetaTypeId(String mt);

		KBUnit getUnit(String unitId, boolean createIfMissing);	
		boolean releaseUnit(String unitId);
		void reset();
	}

}
