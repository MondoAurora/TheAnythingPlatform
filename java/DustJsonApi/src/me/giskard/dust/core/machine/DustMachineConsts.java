package me.giskard.dust.core.machine;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.tokens.giskard_me.DustGenTokens_stream_1;

public interface DustMachineConsts extends DustConsts, DustUtilsConsts, DustGenTokens_stream_1 {

	class DustMachineHandle implements DustHandle {
		DustMachineAgent mind;

		DustMachineIdea unit;
		DustMachineHandle type;
		String id;

		DustMachineHandle(DustMachineAgent mind) {
			this.mind = mind;
		}

		public DustMachineHandle(DustMachineAgent mind, DustMachineIdea unit, DustMachineHandle type, String id) {
			this(mind);
			init(unit, type, id);
		}

		void init(DustMachineIdea unit, DustMachineHandle type, String id) {
			this.unit = unit;
			this.type = type;
			this.id = id;
		}

		DustMachineIdea getUnitIdea() {
			return unit;
		}

		@Override
		public DustMachineHandle getUnit() {
			return (null == unit) ? null : unit.mh;
		}

		@Override
		public DustMachineHandle getType() {
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
