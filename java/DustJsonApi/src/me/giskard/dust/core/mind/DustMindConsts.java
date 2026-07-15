package me.giskard.dust.core.mind;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsConsts;

public interface DustMindConsts extends DustConsts, DustUtilsConsts {

	String NAME_MIND = "MiND";
	
//	String TOKEN_MIND = UNIT_DUST + DUST_SEP_TOKEN + NAME_MIND;


	class DustMindHandle implements DustHandle {
		final DustMindAgent mind;

		DustMindIdea unit;
		DustMindHandle type;
		String id;

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
