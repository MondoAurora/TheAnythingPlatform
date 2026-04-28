package me.giskard.dust.core.utils;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.mind.DustMindConsts;

public class DustUtilsData implements DustUtilsConsts, DustMindConsts {

	public static DustHandle getAtt(DustHandle meta, DustHandle type, String attName) {
		DustHandle att = Dust.getHandle(meta, DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE), meta.getId() + DUST_SEP_TOKEN + attName, DustOptCreate.Meta);
		Dust.access(DustAccess.Insert, type, att, TOKEN_APPEARS);
		Dust.access(DustAccess.Set, att, type, TOKEN_CHILDMAP, attName);
		return att;
	}

}
