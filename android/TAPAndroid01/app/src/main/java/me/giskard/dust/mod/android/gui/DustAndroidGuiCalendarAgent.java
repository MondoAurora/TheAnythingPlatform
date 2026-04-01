package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.view.View;
import android.widget.CalendarView;

public class DustAndroidGuiCalendarAgent extends DustAndroidGuiConsts.AndroidGuiAgent {
    @Override
    public View createView(Context ctx) {
        CalendarView cv = new CalendarView(ctx);
        return cv;
    }

    @Override
    protected Object process(DustAccess access) throws Exception {
        return null;
    }
}
