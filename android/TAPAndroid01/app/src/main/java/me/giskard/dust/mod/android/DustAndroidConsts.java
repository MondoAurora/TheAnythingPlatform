package me.giskard.dust.mod.android;

import me.giskard.dust.core.DustConsts;

public interface DustAndroidConsts extends DustConsts {
    enum ViewType {
        RichText(R.drawable.view_richtext), Calendar(R.drawable.view_calendar), Cards(R.drawable.view_cards),
        DataGrid(R.drawable.view_datagrid), Properties(R.drawable.view_properties),
        Player(R.drawable.view_player), Progress(R.drawable.view_progress);

        ViewType(int res) {
            this.res = res;
        }

        public final int res;
    }
}
