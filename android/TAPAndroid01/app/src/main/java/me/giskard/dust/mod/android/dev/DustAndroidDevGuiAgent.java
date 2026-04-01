package me.giskard.dust.mod.android.dev;

import android.content.Context;
import android.view.View;

import me.giskard.dust.mod.android.gui.DustAndroidGuiConsts;

public class DustAndroidDevGuiAgent extends DustAndroidGuiConsts.AndroidGuiAgent {

    View v;

    public DustAndroidDevGuiAgent(View v) {
        this.v = v;
    }

    @Override
    public View createView(Context ctx) {
        return v;
    }

    @Override
    protected Object process(DustAccess access) throws Exception {
        return null;
    }
}
