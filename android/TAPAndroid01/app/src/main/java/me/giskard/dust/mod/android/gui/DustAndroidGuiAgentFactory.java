package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.VideoView;

import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.android.R;
import me.giskard.dust.mod.android.dev.DustAndroidDevGuiAgent;
import me.giskard.dust.mod.android.dev.DustAndroidDevGuiUtils;

public class DustAndroidGuiAgentFactory extends DustUtilsFactory<DustAndroidGuiConsts.ViewType, View> implements DustAndroidGuiConsts {

    private static final DustCreator<DustAndroidGuiConsts.AndroidGuiAgent> AGENT_CREATOR = new DustCreator<DustAndroidGuiConsts.AndroidGuiAgent>() {
        @Override
        public DustAndroidGuiConsts.AndroidGuiAgent create(Object key, Object... hints) {
            Context ctx = (Context) hints[0];

            switch ((ViewType) key) {
                case RichText:
                    WebView wv = new WebView(ctx);
                    wv.loadUrl("https://www.columbia.edu/~fdc/sample.html");
                    WebViewClient wvc = new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            return super.shouldOverrideUrlLoading(view, request);
                        }
                    };
                    wv.setWebViewClient(wvc);
                    return new DustAndroidDevGuiAgent(wv);
                case Calendar:
                    return new DustAndroidGuiCalendarAgent();
                case Cards:
                    TextView tv = new TextView(ctx);
                    tv.setText("Card view");
                    return new DustAndroidDevGuiAgent(tv);
                case DataGrid:
                    TableLayout grid = new TableLayout(ctx);
                    DustAndroidDevGuiUtils.fillTable(grid, 10, "A", "B", "C", "D");
                    return new DustAndroidDevGuiAgent(grid);
                case Properties:
                    TableLayout prop = new TableLayout(ctx);
                    DustAndroidDevGuiUtils.fillTable(prop, 0, "A", "B", "C", "D");
                    return new DustAndroidDevGuiAgent(prop);
                case Player:
                    VideoView vv = new VideoView(ctx);
                    String u = "android.resource://me.giskard.dust.mod.android/" + R.raw.test_video;
                    Uri uri = Uri.parse(u);
                    vv.setVideoURI(uri);
                    vv.start();
                    return new DustAndroidDevGuiAgent(vv);
                case Progress:
                    ImageView iv = new ImageView(ctx);
                    iv.setImageResource(R.drawable.view_progress);
                    return new DustAndroidDevGuiAgent(iv);
            }
            return null;
        }
    };

    private static final DustUtilsFactory<ViewType, AndroidGuiAgent> AGENT_FACTORY = new DustUtilsFactory<>(AGENT_CREATOR);

    public DustAndroidGuiAgentFactory() {
        super(true);

        creator = new DustCreator<View>() {
            @Override
            public View create(Object key, Object... hints) {
                AndroidGuiAgent a = AGENT_FACTORY.get((ViewType) key, hints);
                return a.createView((Context) hints[0]);
            }
        };
    }
}
