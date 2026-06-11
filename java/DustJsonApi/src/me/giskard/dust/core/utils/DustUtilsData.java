package me.giskard.dust.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindConsts;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustUtilsData implements DustUtilsConsts, DustMindConsts {

	public static DustHandle getAtt(DustHandle meta, DustHandle type, String attName) {
		DustHandle att = Dust.getHandle(meta, TOKEN_KBMETA_ATTRIBUTE, meta.getId() + DUST_SEP_TOKEN + attName, DustOptCreate.Meta);
		Dust.access(DustAccess.Insert, type, att, TOKEN_APPEARS);
		Dust.access(DustAccess.Set, att, type, TOKEN_CHILDMAP, attName);
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

		DustHandle hEvent = Dust.getHandle(hUnit, TOKEN_EVENT, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Set, hTarget, hEvent, TOKEN_TARGET);

		Dust.access(DustAccess.Set, duration, hEvent, TOKEN_EVENT_DURATION);
		Dust.access(DustAccess.Set, durationUnit, hEvent, TOKEN_EVENT_DURATION_UNIT);
		
		setEventDate(hEvent, dStart);

		return hEvent;
	}

	public static void setEventDate(DustHandle hEvent, Date dStart) {
		String strDate = sdfEventDate.format(dStart);
		if (!DustUtils.isEqual(NO_DATE, strDate)) {
			Dust.access(DustAccess.Set, strDate, hEvent, TOKEN_EVENT_DATE);
		}
		String strTime = sdfEventTime.format(dStart);
		if (!DustUtils.isEqual(NO_TIME, strTime)) {
			Dust.access(DustAccess.Set, strTime, hEvent, TOKEN_EVENT_TIME);
		}
	}

	public static Date getEventDate(DustHandle hEvent) {
		Date d = null;

		if (null != hEvent) {
			SimpleDateFormat sdf;
			String str;

			String strDate = Dust.access(DustAccess.Peek, "", hEvent, TOKEN_EVENT_DATE);
			String strTime = Dust.access(DustAccess.Peek, "", hEvent, TOKEN_EVENT_TIME);

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
		Map<String, Map<String, Object>> mapping = Dust.access(DustAccess.Peek, null, src, TOKEN_MAPPING);

		if (null != mapping) {
			Map mp = new HashMap();

			for (Map.Entry<String, Map<String, Object>> mfe : mapping.entrySet()) {
				String field = mfe.getKey();
				Map<String, Object> def = mfe.getValue();
				Object val = null;

				if (null == def) {
					val = Dust.access(DustAccess.Peek, null, params, field);
				} else {
					val = Dust.access(DustAccess.Peek, null, params, def.get(TOKEN_SOURCE));

					switch ((String) def.getOrDefault(TOKEN_CMD, "")) {
					case "split":
						String sep = (String) def.get(TOKEN_SEPARATOR);
						int idx = ((Number) def.get(TOKEN_INDEX)).intValue();
						String str = (String) val;
						val = DustUtils.isEmpty(str) ? def.get(TOKEN_DEFAULT) : DustUtils.optGet(str.split(sep), idx, def.get(TOKEN_DEFAULT));
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


}
