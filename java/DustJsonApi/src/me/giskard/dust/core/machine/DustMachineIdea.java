package me.giskard.dust.core.machine;

import java.util.Map;
import java.util.TreeMap;

//@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMachineIdea implements DustMachineConsts {
	DustMachineHandle mh;

	Map<String, Object> content = new TreeMap<>();

	DustMachineIdea() {
	}

	DustMachineIdea(DustMachineHandle mh) {
		setMh(mh);
	};

	void setMh(DustMachineHandle mh) {
		this.mh = mh;
		loadMh();
	};

	void loadMh() {
		content.put(TOKEN_MIND_ATT_UNIT, mh.getUnit());
		content.put(TOKEN_MIND_ATT_TYPE, mh.getType());
		content.put(TOKEN_MIND_ATT_ID, mh.getId());
	}

	@Override
	public String toString() {
		return mh.toString();
	}

	protected Map<String, Object> getContent() {
		return content;
	}
}
