package me.giskard.dust.mod.android.utils;

import me.giskard.dust.mod.android.DustAndroidConsts;

public class DustAndroidUtils implements DustAndroidConsts {
    public static String toResName(DustHandle h) {
        StringBuilder sb = new StringBuilder();

        boolean lc = false;
        for (char chr : h.getId().toCharArray()) {
            if (Character.isLetter(chr)) {
                if (Character.isLowerCase(chr)) {
                    lc = true;
                } else {
                    if (lc) {
                        sb.append("_");
                        lc = false;
                    }
                    chr = Character.toLowerCase(chr);
                }
            } else {
                lc = false;
                if (!Character.isDigit(chr)) {
                    chr = '_';
                }
            }

            sb.append(chr);
        }

        return sb.toString();
    }
}
