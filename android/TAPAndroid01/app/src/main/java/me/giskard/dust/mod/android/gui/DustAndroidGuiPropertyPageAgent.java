package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TableLayout;

import me.giskard.dust.mod.android.dev.DustAndroidDevGuiUtils;

public class DustAndroidGuiPropertyPageAgent extends DustAndroidGuiConsts.AndroidGuiAgent {
    @Override
    public View createView(Context ctx) {
        TableLayout prop = new TableLayout(ctx);
        DustAndroidDevGuiUtils.fillTable(prop, 0, "A", "B", "C", "D");
        return prop;
    }

    @Override
    protected Object process(DustAccess access) throws Exception {
        return null;
    }
}
