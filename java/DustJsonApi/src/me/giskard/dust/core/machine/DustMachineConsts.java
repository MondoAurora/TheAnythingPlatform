package me.giskard.dust.core.machine;

import java.util.Map;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustMachine;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.tokens.giskard_me.DustGenTokens_stream_1;

public interface DustMachineConsts extends DustConsts, DustUtilsConsts, DustGenTokens_stream_1 {
	
	public abstract class DustMachineImpl extends DustMachine {
		abstract Map getContent(DustHandle h);
	}

	class DustMachineHandle implements DustHandle {
		final DustMachineImpl mind;

		DustMachineIdea unit;
		DustMachineHandle type;
		String id;

		DustMachineHandle(DustMachineImpl mind) {
			this.mind = mind;
		}

		public DustMachineHandle(DustMachineImpl mind, DustMachineIdea unit, DustMachineHandle type, String id) {
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
