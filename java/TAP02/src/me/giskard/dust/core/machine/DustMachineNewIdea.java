package me.giskard.dust.core.machine;

import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.utils.DustUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMachineNewIdea implements DustMachineConsts {
	DustMachineNewHandle mh;

	final Map<DustMachineNewHandle, Object> content = new HashMap<>();

// CAN'T USE TreeMap because the boot process changes the content of the handle!!!
//	final Map<DustMachineNewHandle, Object> content = new TreeMap<>();

	DustMachineNewIdea() {
	}

	DustMachineNewIdea(DustMachineNewHandle mh) {
		this.mh = mh;
	};

//	void loadMh() {
//		content.put(TOKEN_MIND_ATT_UNIT, mh.getUnit());
//		content.put(TOKEN_MIND_ATT_TYPE, mh.getType());
//		content.put(TOKEN_MIND_ATT_ID, mh.getId());
//	}

	@Override
	public String toString() {
		return DustUtils.toString(mh);
	}

	protected Map<DustMachineNewHandle, Object> getContent() {
		return content;
	}
}
