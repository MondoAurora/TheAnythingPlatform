package me.giskard.dust.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.dust.DustConsts;
import me.giskard.dust.DustException;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface DustUtilsConsts extends DustConsts {
	
	String TOKEN_PREPROCESS = DUST_UNIT_ID + DUST_SEP_TOKEN + "preProcess";
	String TOKEN_UPPERCASE = DUST_UNIT_ID + DUST_SEP_TOKEN + "upperCase";
	
	interface DustCreator<Type> {
		Type create(Object key, Object... hints);
		default void initNew(Type item, Object key, Object... hints) {}
	}
	
	class DustCreatorSimple<Type> implements DustCreator<Type> {
		
		Class cc;
		
		public DustCreatorSimple(Class cc) {
			this.cc = cc;
		}

		@Override
		public Type create(Object key, Object... hints) {
			try {
				return (Type) cc.getDeclaredConstructor().newInstance();
			} catch (Throwable e) {
				return DustException.wrap(e, key, hints);
			}
		}
	}
	
	DustCreator<Map> MAP_CREATOR = new DustCreatorSimple<>(HashMap.class);
	DustCreator<Set> SET_CREATOR = new DustCreatorSimple<>(HashSet.class);
	DustCreator<Map> SORTEDMAP_CREATOR = new DustCreatorSimple<>(TreeMap.class);
	DustCreator<Set> SORTEDSET_CREATOR = new DustCreatorSimple<>(TreeSet.class);
	DustCreator<ArrayList> ARRAY_CREATOR = new DustCreatorSimple<>(ArrayList.class);

}
