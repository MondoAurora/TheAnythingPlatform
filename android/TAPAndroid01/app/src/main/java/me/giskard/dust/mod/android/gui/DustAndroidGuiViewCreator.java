package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;

import me.giskard.dust.mod.android.R;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.mod.android.utils.DustAndroidUtils;

public class DustAndroidGuiViewCreator implements DustUtilsConsts.DustCreator<View>, DustAndroidGuiConsts {
    Context ctx;

    public DustAndroidGuiViewCreator(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public View create(Object key, Object... hints) {
        View ret = null;

        DustHandle handle = (DustHandle) key;
        DustHandle hType = Dust.access(DustConsts.DustAccess.Peek, "", key, DustConsts.TOKEN_TYPE);
        String type = hType.getId();
        String id = Dust.access(DustConsts.DustAccess.Peek, "", key, DustConsts.TOKEN_ID);

        switch (type) {
            case DustConsts.TOKEN_TYPE_SERVICE:
                ImageButton ibtn = new ImageButton(ctx);
                String resKey = DustAndroidUtils.toResName(handle);
                File f = ctx.getFilesDir();
                f = new File(f, resKey + DUST_EXT_PNG);
                Dust.log(DustConsts.TOKEN_LEVEL_INFO, id, resKey, f.getPath());

                if ( f.isFile() ) {
                    Icon i = Icon.createWithFilePath(f.getPath());
                    ibtn.setImageIcon(i);
                } else {
                    ibtn.setImageResource(R.drawable.icon_wifi);
                }
                ret = ibtn;
                break;
            default:
                break;
        }

        return ret;
    }
}
