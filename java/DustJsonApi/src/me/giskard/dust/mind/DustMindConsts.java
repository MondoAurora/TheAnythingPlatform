package me.giskard.dust.mind;

import me.giskard.dust.DustConsts;
import me.giskard.dust.utils.DustUtilsConsts;

public interface DustMindConsts extends DustConsts, DustUtilsConsts {

	String NAME_MIND = "MiND";

	String TOKEN_MIND = DUST_UNIT_ID + DUST_SEP_TOKEN + NAME_MIND;

	String TOKEN_TYPES = DUST_UNIT_ID + DUST_SEP_TOKEN + "types";
	String TOKEN_ATTRIBUTES = DUST_UNIT_ID + DUST_SEP_TOKEN + "attributes";

	String TOKEN_KB_KNOWNUNITS = DUST_UNIT_ID + DUST_SEP_TOKEN + "knownUnits";

	String TOKEN_KBMETA_UNIT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Unit";
	String TOKEN_KBMETA_TYPE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Type";
	String TOKEN_KBMETA_ATTRIBUTE = DUST_UNIT_ID + DUST_SEP_TOKEN + "Attribute";
	String TOKEN_KBMETA_TAG = DUST_UNIT_ID + DUST_SEP_TOKEN + "Tag";

	String TOKEN_UNIT_OBJECTS = DUST_UNIT_ID + DUST_SEP_TOKEN + "UnitObjects";
	String TOKEN_UNIT_REFS = DUST_UNIT_ID + DUST_SEP_TOKEN + "UnitRefs";

	class DustMindObject implements DustObject {
		final DustMindAgent mind;

		private DustMindIdea unit;
		private DustMindObject type;
		private String id;
		
		DustMindObject(DustMindAgent mind) {
			this.mind = mind;
		}

		public DustMindObject(DustMindAgent mind, DustMindIdea unit, DustMindObject type, String id) {
			this(mind);
			init(unit, type, id);
		}

		void init(DustMindIdea unit, DustMindObject type, String id) {
			this.unit = unit;
			this.type = type;
			this.id = id;
		}
		
		DustMindIdea getUnitIdea() {
			return unit;
		}

		@Override
		public DustObject getUnit() {
			return (null == unit) ? null : unit.dmo;
		}

		@Override
		public DustObject getType() {
			return type;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			DustObject t = getType();
			String str = getId();

			if (null != t) {
				str = str + " [" + t.getId() + "]";
			}

			return str;
		}

	}
}
