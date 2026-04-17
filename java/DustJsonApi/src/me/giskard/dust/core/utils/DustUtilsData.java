package me.giskard.dust.core.utils;

import java.util.Random;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.mind.DustMindConsts;

public class DustUtilsData extends DustUtils implements DustUtilsConsts, DustMindConsts {
	private static final Random RND = new Random(System.currentTimeMillis());

	public static DustHandle getAtt(DustHandle meta, DustHandle type, String attName) {
		DustHandle att = Dust.getHandle(meta, DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE), meta.getId() + DUST_SEP_TOKEN + attName, DustOptCreate.Meta);
		Dust.access(DustAccess.Insert, type, att, TOKEN_APPEARS);
		Dust.access(DustAccess.Set, att, type, TOKEN_CHILDMAP, attName);
		return att;
	}

	public static String getNewId(DustHandle hUnit, int bytes) {
		String id;

		byte[] rb = new byte[bytes];
		do {
			RND.nextBytes(rb);

			StringBuilder sb = new StringBuilder(2 * bytes);
			for (byte b : rb) {
				sb.append(Character.forDigit((b >> 4) & 0xF, 16));
				sb.append(Character.forDigit((b & 0xF), 16));
			}

			id = sb.toString();
		} while (null != Dust.getHandle(hUnit, null, id, DustOptCreate.None));

		return id;
	}

}
