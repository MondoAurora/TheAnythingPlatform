package me.giskard.dust.mind;

import java.util.Map;
import java.util.TreeMap;

import me.giskard.dust.DustConsts;

//@SuppressWarnings({ "unchecked", "rawtypes" })
class DustMindIdea implements DustConsts.DustObject, DustMindConsts {
	final DustMindAgent mind;

	Map<String, Object> content = new TreeMap<>();

	DustMindIdea(DustMindAgent mind) {
		this.mind = mind;
	};

	DustMindIdea(DustMindAgent mind, DustMindIdea unit, DustObject type, String id) {
		this(mind);
		init(unit, type, id);
	};

	void init(DustMindIdea unit, DustObject type, String id) {
		content.put(TOKEN_UNIT, unit);
		content.put(TOKEN_ID, id);
		content.put(TOKEN_TYPE, type);
	}

	@Override
	public DustMindIdea getUnit() {
		return (DustMindIdea) content.get(TOKEN_UNIT);
	}

	@Override
	public String getId() {
		return (String) content.get(TOKEN_ID);
	}

	@Override
	public DustMindIdea getType() {
		return (DustMindIdea) content.get(TOKEN_TYPE);
	}

	@Override
	public String toString() {
		DustMindIdea t = getType();
		String str = getId();
		
		if ( null != t) {
			str = str + " [" + t.getId() + "]";
		}

		return str;
	}

	protected Object getContent() {
		return content;
	}
}
