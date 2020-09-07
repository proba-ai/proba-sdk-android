package com.appbooster.exampleappjava;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.appbooster.appboostersdk.AppboosterSdk;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> defaults = new HashMap<>();
        defaults.put("buttonColor","blue");

        final AppboosterSdk sdk = new AppboosterSdk.Builder(this)
                .appId("11314")
                .sdkToken("94EBA72B439D4A18B971231088C77D5F")
                .defaults(defaults)
                .build();

        if (sdk.getValue("buttonColor") != null) {
            ((MaterialButton) findViewById(R.id.appboosterExampleButton))
                    .setBackgroundColor(Color.parseColor(sdk.getValue("buttonColor")));
        }

        sdk.fetch(new AppboosterSdk.OnSuccessListener() {
                      @Override
                      public void onSuccess() {
                          ((MaterialButton) findViewById(R.id.appboosterExampleButton))
                                  .setBackgroundColor(Color.parseColor(sdk.getValue("buttonColor")));
                      }
                  },
                new AppboosterSdk.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
    }
}
