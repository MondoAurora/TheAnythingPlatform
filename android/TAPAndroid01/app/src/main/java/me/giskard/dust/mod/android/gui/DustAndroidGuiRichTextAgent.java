package me.giskard.dust.mod.android.gui;

import android.content.Context;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CalendarView;

public class DustAndroidGuiRichTextAgent extends DustAndroidGuiConsts.AndroidGuiAgent {
    WebViewClient wvc = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    };

    @Override
    public View createView(Context ctx) {
        WebView wv = new WebView(ctx);
        wv.setWebViewClient(wvc);

        wv.loadUrl("https://www.columbia.edu/~fdc/sample.html");

        return wv;
    }

    @Override
    protected Object process(DustAccess access) throws Exception {
        return null;
    }
}
