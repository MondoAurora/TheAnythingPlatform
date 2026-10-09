package me.giskard.dust.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.machine.DustMachineConsts;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustUtilsData implements DustUtilsConsts, DustMachineConsts {

	public static DustHandle getAtt(DustHandle meta, DustHandle type, String attName) {
		DustHandle att = Dust.getHandle(meta, HANDLE_MIND_ASP_ATTRIBUTE, meta.getId() + DUST_SEP_TOKEN + attName, true);
		Dust.access(DustAccess.Insert, type, att, HANDLE_MISC_ATT_APPEARS);
		Dust.access(DustAccess.Set, att, type, HANDLE_MISC_ATT_CHILDMAP, attName);
		return att;
	}

	private static SimpleDateFormat sdfEventDate = new SimpleDateFormat(DUST_FMT_DATE);
	private static SimpleDateFormat sdfEventTime = new SimpleDateFormat(DUST_FMT_TIME);
	private static SimpleDateFormat sdfEventDateTime = new SimpleDateFormat(DUST_FMT_DATE + "'T'" + DUST_FMT_TIME);

	public final static String NO_DATE = "1970-01-01";
	public final static String NO_TIME = "00:00:00.000Z";

	public static Date getEventZeroDate() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DUST_FMT_DATE);
			return sdf.parse(DustUtilsData.NO_DATE);
		} catch (ParseException e) {
			return DustException.wrap(e);
		}
	};

	public static DustHandle createEvent(DustHandle hUnit, DustHandle hTarget, Date dStart, long duration, DustHandle durationUnit) {

		DustHandle hEvent = Dust.getHandle(hUnit, HANDLE_MISC_ASP_EVENT, null, true);
		Dust.access(DustAccess.Set, hTarget, hEvent, HANDLE_MISC_ATT_TARGET);

		Dust.access(DustAccess.Set, duration, hEvent, HANDLE_MISC_ATT_EVENT_DURATION);
		Dust.access(DustAccess.Set, durationUnit, hEvent, HANDLE_MISC_ATT_EVENT_DURATION_UNIT);
		
		setEventDate(hEvent, dStart);

		return hEvent;
	}

	public static void setEventDate(DustHandle hEvent, Date dStart) {
		String strDate = sdfEventDate.format(dStart);
		if (!DustUtils.isEqual(NO_DATE, strDate)) {
			Dust.access(DustAccess.Set, strDate, hEvent, HANDLE_MISC_ATT_EVENT_DATE);
		}
		String strTime = sdfEventTime.format(dStart);
		if (!DustUtils.isEqual(NO_TIME, strTime)) {
			Dust.access(DustAccess.Set, strTime, hEvent, HANDLE_MISC_ATT_EVENT_TIME);
		}
	}

	public static Date getEventDate(DustHandle hEvent) {
		Date d = null;

		if (null != hEvent) {
			SimpleDateFormat sdf;
			String str;

			String strDate = Dust.access(DustAccess.Peek, "", hEvent, HANDLE_MISC_ATT_EVENT_DATE);
			String strTime = Dust.access(DustAccess.Peek, "", hEvent, HANDLE_MISC_ATT_EVENT_TIME);

			if (DustUtils.isEmpty(strDate)) {
				str = strTime;
				sdf = sdfEventTime;
			} else if (DustUtils.isEmpty(strTime)) {
				str = strDate;
				sdf = sdfEventDate;
			} else {
				str = strDate + "T" + strTime;
				sdf = sdfEventDateTime;
			}

			if (!DustUtils.isEmpty(str)) {
				try {
					d = sdf.parse(str);
				} catch (ParseException e) {
					DustException.wrap(e);
				}
			}
		}

		return d;
	}
	
	public static Map optLoadMapping(Object src, Map params) {
		Map<String, Map<String, Object>> mapping = Dust.access(DustAccess.Peek, null, src, HANDLE_MISC_ATT_MAPPING);

		if (null != mapping) {
			Map mp = new HashMap();

			for (Map.Entry<String, Map<String, Object>> mfe : mapping.entrySet()) {
				String field = mfe.getKey();
				Map<String, Object> def = mfe.getValue();
				Object val = null;

				if (null == def) {
					val = Dust.access(DustAccess.Peek, null, params, field);
				} else {
					val = Dust.access(DustAccess.Peek, null, params, def.get(HANDLE_MISC_ATT_SOURCE));

					switch ((String) def.getOrDefault(HANDLE_MIND_ATT_CMD, "")) {
					case "split":
						String sep = (String) def.get(HANDLE_MISC_ATT_SEPARATOR);
						int idx = ((Number) def.get(HANDLE_MISC_ATT_INDEX)).intValue();
						String str = (String) val;
						val = DustUtils.isEmpty(str) ? def.get(HANDLE_MISC_ATT_DEFAULT) : DustUtils.optGet(str.split(sep), idx, def.get(HANDLE_MISC_ATT_DEFAULT));
						break;
					}
				}

				if (null != val) {
					mp.put(field, val);
				}
			}

			params = mp;
		}
		
		return params;
	}

	public static Iterable<DustHandle> getAttHandles(DustHandle ob) {
		Iterable<DustHandle> ret = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, ob, KEY_MAP_KEYS);
		return ret;
	}

}
