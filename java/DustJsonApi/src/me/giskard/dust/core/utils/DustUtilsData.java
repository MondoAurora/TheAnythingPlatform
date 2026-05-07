package me.giskard.dust.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.mind.DustMindConsts;

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

	private static String NO_DATE = "1970-01-01";
	private static String NO_TIME = "00:00:00.000Z";

	public static DustHandle createEvent(DustHandle hUnit, DustHandle hTarget, Date dStart, long duration, DustHandle durationUnit) {

		DustHandle hEvent = Dust.getHandle(hUnit, TOKEN_EVENT, null, DustOptCreate.Primary);
		Dust.access(DustAccess.Set, hTarget, hEvent, TOKEN_TARGET);

		String strDate = sdfEventDate.format(dStart);
		if (!DustUtils.isEqual(NO_DATE, strDate)) {
			Dust.access(DustAccess.Set, strDate, hEvent, TOKEN_EVENT_DATE);
		}
		String strTime = sdfEventTime.format(dStart);
		if (!DustUtils.isEqual(NO_TIME, strTime)) {
			Dust.access(DustAccess.Set, strTime, hEvent, TOKEN_EVENT_TIME);
		}

		Dust.access(DustAccess.Set, duration, hEvent, TOKEN_EVENT_DURATION);
		Dust.access(DustAccess.Set, durationUnit, hEvent, TOKEN_EVENT_DURATION_UNIT);

		return hEvent;
	}

	public static Date getEventDate(DustHandle hEvent) throws Exception {
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
				d = sdf.parse(str);
			}
		}

		return d;
	}

}
