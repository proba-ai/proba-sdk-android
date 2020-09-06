package com.appbooster.exampleappjava;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.appbooster.appboostersdk.AppboosterSdk;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> defaults = new HashMap<>();
        defaults.put("android_1", "");
        defaults.put("android_2", "");

        final AppboosterSdk sdk = new AppboosterSdk.Builder(this)
                .appId("657")
                .sdkToken("e3b0c44298fc1c149afbf4c8996fb924")
                .usingShake(true)
                .defaults(defaults)
                .build();

        sdk.fetch(new AppboosterSdk.OnSuccessListener() {
                      @Override
                      public void onSuccess() {
                          String valueVal = sdk.getValue("android_1");
                      }
                  },
                new AppboosterSdk.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
    }
}
