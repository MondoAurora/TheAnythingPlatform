package me.giskard.dust.mind;

import java.util.Map;
import java.util.TreeMap;

//@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindIdea implements DustMindConsts {
	DustMindObject dmo;

	Map<String, Object> content = new TreeMap<>();

	DustMindIdea() {
	}

	DustMindIdea(DustMindObject dmo) {
		setDmo(dmo);
	};

	void setDmo(DustMindObject dmo) {
		this.dmo = dmo;
		loadDmo();
	};

	void loadDmo() {
		content.put(TOKEN_UNIT, dmo.getUnit());
		content.put(TOKEN_TYPE, dmo.getType());
		content.put(TOKEN_ID, dmo.getId());
	}

	@Override
	public String toString() {
		return dmo.toString();
	}

	protected Map<String, Object> getContent() {
		return content;
	}
}
