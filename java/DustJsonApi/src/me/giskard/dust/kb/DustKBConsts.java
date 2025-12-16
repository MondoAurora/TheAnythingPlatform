package me.giskard.dust.kb;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.DustConsts;
import me.giskard.dust.utils.DustUtilsConsts;

public interface DustKBConsts extends DustConsts, DustUtilsConsts {
	
	String TOKEN_KB_KNOWLEDGEBASE = "KnowledgeBase";
	String TOKEN_KB_KNOWNUNITS = "knownUnits";
	
	String TOKEN_KBMETA_TYPE = "Type";
	String TOKEN_KBMETA_ATTRIBUTE = "Attribute";
	String TOKEN_KBMETA_TAG = "Tag";
	
	enum KBCollType {
		One, Set, Arr, Map;
		
		public static KBCollType getCollType(Object coll) {
			KBCollType ret = (null == coll) ? KBCollType.One
					: (coll instanceof ArrayList) ? KBCollType.Arr
							: (coll instanceof Map) ? KBCollType.Map : (coll instanceof Set) ? KBCollType.Set : KBCollType.One;

			return ret;
		}

	};


	enum KBAccess {
		Check(false), Peek(false), Get(false), Set(true), Insert(true), Delete(false), Reset(false), Visit(false);
		
		public final boolean creator;

		private KBAccess(boolean creator) {
			this.creator = creator;
		}
	};

	public interface KBObject {
		KBUnit getUnit();
		String getType();
		String getId();
		
		void load(KBObject from, boolean deep, String... atts);

	}
	
	enum KBOptCreate {
		Primary, Reference, None
	}

	public interface KBUnit {
		KBStore getStore();
		String getUnitId();
		Iterable<? extends KBObject> objects();
		int size();

		KBObject getObject(String type, String id, KBOptCreate optCreate);

		default KBObject getObject(String type, String id)  {
			return getObject(type, id, KBOptCreate.Primary);
		}
	}

	public interface KBStore {
		String getMetaTypeId(String mt);

		KBUnit getUnit(String unitId, boolean createIfMissing);	
		Iterable<? extends KBUnit> knownUnits();
		boolean releaseUnit(String unitId);
		void reset();
	}

}
