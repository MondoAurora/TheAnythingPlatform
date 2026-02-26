package me.giskard.dust.core.mind;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsConsts;

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

	class DustMindHandle implements DustHandle {
		final DustMindAgent mind;

		private DustMindIdea unit;
		private DustMindHandle type;
		private String id;
		
		DustMindHandle(DustMindAgent mind) {
			this.mind = mind;
		}

		public DustMindHandle(DustMindAgent mind, DustMindIdea unit, DustMindHandle type, String id) {
			this(mind);
			init(unit, type, id);
		}

		void init(DustMindIdea unit, DustMindHandle type, String id) {
			this.unit = unit;
			this.type = type;
			this.id = id;
		}
		
		DustMindIdea getUnitIdea() {
			return unit;
		}

		@Override
		public DustMindHandle getUnit() {
			return (null == unit) ? null : unit.mh;
		}

		@Override
		public DustMindHandle getType() {
			return type;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			DustHandle t = getType();
			String str = getId();

			if (null != t) {
				str = str + " [" + t.getId() + "]";
			}

			return str;
		}

	}
}
