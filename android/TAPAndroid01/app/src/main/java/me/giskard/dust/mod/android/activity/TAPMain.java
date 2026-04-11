package me.giskard.dust.mod.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.DustMind;
import me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent;
import me.giskard.dust.core.stream.DustStreamSrcFileAgent;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.core.utils.DustUtilsFile;
import me.giskard.dust.mod.android.DustAndroidConsts;
import me.giskard.dust.mod.android.R;
import me.giskard.dust.mod.android.gui.DustAndroidGuiAgentFactory;
import me.giskard.dust.mod.android.gui.DustAndroidGuiConsts;
import me.giskard.dust.mod.android.gui.DustAndroidGuiViewFactory;

public class TAPMain extends Activity implements DustAndroidConsts, DustAndroidGuiConsts {

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

    Context appCtx;
    Resources appRes;

    DustAndroidGuiViewFactory viewFactory;

    ProgressBar progress;

    String server;
    String appUnitName;
    DustMind.Bootloader bootLoader;
    DustMind.StreamSource streamSource;
    DustHandle hApp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appCtx = getApplicationContext();
        appRes = appCtx.getResources();

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);

        viewFactory = new DustAndroidGuiViewFactory(this);

        try {
            ApplicationInfo ai = appCtx.getPackageManager().getApplicationInfo(appCtx.getPackageName(), PackageManager.GET_META_DATA);
            server = ai.metaData.getString(TAP_CONFIG_SERVER);
            appUnitName = ai.metaData.getString(TAP_CONFIG_APP_MODULE);

            DustMind.Bootloader bootLoader = new DustStreamJsonApiSerializerAgent();
            streamSource = new DustStreamSrcFileAgent() {
                @Override
                protected File getRootFolder(String root) {
                    File f = appCtx.getFilesDir();
                    if (!DustUtils.isEmpty(root)) {
                        f = new File(f, root);
                        try {
                            DustUtilsFile.ensureDir(f);
                        } catch (Throwable e) {
                            DustException.wrap(e, "Creating Dust root folder", f);
                        }
                    }
                    return f;
                }
            };

            String appUnitFile = appUnitName + DUST_EXT_JSON;

            copyRes(R.raw.mhcli, appUnitFile);
            copyRes(R.raw.mhcli_android, appUnitName + "." + DUST_PLATFORM_ANDROID + DUST_EXT_JSON);

            copyRes(R.raw.mhcli_1_display_welcome, "mhcli_1_display_welcome.png");
            copyRes(R.raw.mhcli_1_edit_user, "mhcli_1_edit_user.png");
            copyRes(R.raw.mhcli_1_edit_config, "mhcli_1_edit_config.png");

            hApp = Dust.start(DUST_PLATFORM_ANDROID, "MHCli", appUnitFile, bootLoader, streamSource);

        } catch (Throwable e) {
            DustException.wrap(e);
        }

        viewAgentFactory = new DustAndroidGuiAgentFactory();

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        center = new ConstraintLayout(this);
        lpCenter = new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        menuBar = new Toolbar(this);
        actionBar = new Toolbar(this);

        Collection<DustHandle> topMenuItems = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, hApp, TOKEN_GUI_MAIN, TOKEN_GUI_FOCUS, TOKEN_GUI_TOPMENU, TOKEN_MEMBERS);

        for ( DustHandle h : topMenuItems ) {
            actionBar.addView(viewFactory.get(h));
        }

        Dust.log(DustConsts.TOKEN_LEVEL_INFO, "init focus", topMenuItems);

        for (ViewType vt : ViewType.values()) {
            menuBar.addView(menuBtnFactory.get(vt));
        }

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

        center.addView(progress, lpCenter);
    }

    private void copyRes(int res, String file) {
        try (InputStream is = appRes.openRawResource(res);
             OutputStream os = streamSource.optGetStream(TOKEN_CMD_SAVE, ".", file)) {

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            os.flush();
        } catch (Throwable e) {
            DustException.wrap(e);
        }
    }

}
