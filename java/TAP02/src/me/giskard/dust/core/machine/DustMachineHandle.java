package me.giskard.dust.core.machine;

import me.giskard.dust.api.DustHandle;

public class DustMachineHandle implements DustHandle {
	DustMachineIdea machine;

	DustMachineIdea unit;
	DustMachineHandle type;
	String id;

	DustMachineHandle(DustMachineIdea machine) {
		this.machine = machine;
	}

	public DustMachineHandle(DustMachineIdea machine, DustMachineIdea unit, DustMachineHandle type, String id) {
		this(machine);
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