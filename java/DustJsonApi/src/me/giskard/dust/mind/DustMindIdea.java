package me.giskard.dust.mind;

import java.util.Map;
import java.util.TreeMap;

//@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindIdea implements DustMindConsts {
	DustMindHandle mh;

	Map<String, Object> content = new TreeMap<>();

	DustMindIdea() {
	}

	DustMindIdea(DustMindHandle mh) {
		setMh(mh);
	};

	void setMh(DustMindHandle mh) {
		this.mh = mh;
		loadMh();
	};

	void loadMh() {
		content.put(TOKEN_UNIT, mh.getUnit());
		content.put(TOKEN_TYPE, mh.getType());
		content.put(TOKEN_ID, mh.getId());
	}

	@Override
	public String toString() {
		return mh.toString();
	}

	protected Map<String, Object> getContent() {
		return content;
	}
}
