package me.giskard.dust.core.machine;

import me.giskard.dust.core.DustConstsBoot.DustHandle;

public class DustMachineNewHandle implements DustHandle {
	DustMachineNewIdea machine;

	DustMachineNewIdea unit;
	DustMachineNewHandle type;
	String id;

	DustMachineNewHandle(DustMachineNewIdea machine) {
		this.machine = machine;
	}

	public DustMachineNewHandle(DustMachineNewIdea machine, DustMachineNewIdea unit, DustMachineNewHandle type, String id) {
		this(machine);
		init(unit, type, id);
	}

	void init(DustMachineNewIdea unit, DustMachineNewHandle type, String id) {
		this.unit = unit;
		this.type = type;
		this.id = id;
	}

	DustMachineNewIdea getUnitIdea() {
		return unit;
	}

	@Override
	public DustMachineNewHandle getUnit() {
		return (null == unit) ? null : unit.mh;
	}

	@Override
	public DustMachineNewHandle getType() {
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