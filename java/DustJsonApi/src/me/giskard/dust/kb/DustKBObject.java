package me.giskard.dust.kb;

import java.util.Map;
import java.util.TreeMap;

import me.giskard.dust.Dust;
import me.giskard.dust.DustException;

class DustKBObject implements DustKBConsts, DustKBConsts.KBObject {

	String id;
	String type;
	KBUnit unit;

	Map<String, Object> content = new TreeMap<>();

	DustKBObject(KBUnit unit, String type, String id) {
		this.unit = unit;
		this.type = type;
		this.id = id;

		if (null == type) {
			DustException.wrap(null, "Missing type for object", id);
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + DUST_SEP_ID + id;
	}

	@Override
	public KBUnit getUnit() {
		return unit;
	}
	
	@Override
	public Iterable<String> atts() {
		return content.keySet();
	}
	
	@Override
	public void load(KBObject from,  boolean deep, String... atts) {
		if ( deep ) {
			Dust.log(TOKEN_LEVEL_WARNING, "DustKBObject deep load not supported");
		}
		
		DustKBObject kbo = (DustKBObject)from;
		
		if ( 0 == atts.length) {
			for (String a : from.atts() ) {
				content.put(a, kbo.content.get(a));
			}
		} else {
			for (String a : atts ) {
				content.put(a, kbo.content.get(a));
			}
		}
	}

	@Override
	public <RetType> RetType access(KBAccess access, Object val, Object... path) {
		return DustKBUtils.access(access, val, this.content, path);
	}

}
