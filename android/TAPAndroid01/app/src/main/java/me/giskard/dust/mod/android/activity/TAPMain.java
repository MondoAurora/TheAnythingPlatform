package me.giskard.dust.mod.android.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.android.DustAndroidConsts;
import me.giskard.dust.mod.android.gui.DustAndroidGuiAgentFactory;

public class TAPMain extends Activity implements DustAndroidConsts {

    DustAndroidGuiAgentFactory viewAgentFactory;

    View.OnClickListener clMenu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewType vt = menuBtnFactory.getReverse(v);

            if (null == vt) {
                Dust.log(DustConsts.TOKEN_LEVEL_WARNING, "What view?");
            } else if (currView != vt) {
                View cv = viewAgentFactory.get(vt, TAPMain.this);
                center.removeAllViews();
                center.addView(cv, lpCenter);
                currView = vt;
            }
        }
    };

    DustUtilsFactory<ViewType, View> menuBtnFactory = new DustUtilsFactory<>(new DustUtilsConsts.DustCreator<View>() {
        @Override
        public View create(Object key, Object... hints) {
            ImageButton ibt = new ImageButton(TAPMain.this);
            ibt.setImageResource(((ViewType) key).res);
            ibt.setOnClickListener(clMenu);
            return ibt;
        }
    });

    Toolbar menuBar;
    Toolbar actionBar;

    ConstraintLayout center;
    ConstraintLayout.LayoutParams lpCenter;
    ViewType currView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewAgentFactory = new DustAndroidGuiAgentFactory();

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        center = new ConstraintLayout(this);
        lpCenter = new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        menuBar = new Toolbar(this);
        actionBar = new Toolbar(this);

        for (ViewType vt : ViewType.values()) {
            menuBar.addView(menuBtnFactory.get(vt));
        }

        Button bt;
        bt = new Button(this);
        bt.setText("szilva");
        actionBar.addView(bt);

        LinearLayout.LayoutParams lp;

        lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        menuBar.setBackgroundColor(Color.CYAN);
        main.addView(menuBar, lp);

        lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
        lp.weight = 1;
        center.setBackgroundColor(Color.YELLOW);

        main.addView(center, lp);

        lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        actionBar.setBackgroundColor(Color.MAGENTA);
        main.addView(actionBar, lp);

        setContentView(main);
    }
}
