package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.view.View;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsFactory;

public class DustAndroidGuiViewFactory extends DustUtilsFactory<DustConsts.DustHandle, View> implements DustAndroidGuiConsts {
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DustHandle h = getReverse(v);

            Dust.log(DustConsts.TOKEN_LEVEL_TRACE, "Selected handle", h);
        }
    };


    public DustAndroidGuiViewFactory(Context ctx) {
        super(false);

        creator = new DustAndroidGuiViewCreator(ctx) {
            @Override
            public void initNew(View item, Object key, Object... hints) {
                item.setOnClickListener(onClickListener);
            }
        };
    }
}
