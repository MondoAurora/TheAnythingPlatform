package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.view.View;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.mod.android.DustAndroidConsts;

public interface DustAndroidGuiConsts extends DustAndroidConsts {
    public abstract class AndroidGuiAgent extends DustConsts.DustAgent implements DustAndroidGuiConsts {
        public abstract View createView(Context ctx);
    }

}
