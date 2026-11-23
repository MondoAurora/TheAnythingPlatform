package me.giskard.dust.mod.android.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import gen.boot.DustGenBootConsts;
import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustException;
import me.giskard.dust.mod.android.TAPAndroidUtils;

public class TAPMainActivity extends AppCompatActivity implements DustGenBootConsts {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAPAndroidUtils.setAppCtx(getApplicationContext());
        TAPAndroidUtils.setAssets(getAssets());

        String appId = "Lorand/test01$AndroidClient";
        String platform = TOKEN_DUST_IDEA_ANDROID24;
        try {
            String[] a = getAssets().list("");

            Dust.start(appId, platform);
        } catch (Exception e) {
            DustException.wrap(e, "failed to launch", appId);
        }

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_tapmain);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}