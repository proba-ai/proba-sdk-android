package ai.proba.exampleappjava;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import ai.proba.exampleappjava.R;
import ai.proba.probasdk.ProbaSdk;
import com.google.android.material.button.MaterialButton;

import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> defaults = new HashMap<>();
        defaults.put("buttonColor", "blue");


        final ProbaSdk sdk = new ProbaSdk.Builder(this)
                .appId("11314")
                .sdkToken("94EBA72B439D4A18B971231088C77D5F")
                .defaults(defaults)
                .build();

        ((MaterialButton) findViewById(R.id.probaExampleButton))
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sdk.launchDebugMode(MainActivity.this);
                            }
                        });

        if (sdk.getValue("buttonColor") != null) {
            ((MaterialButton) findViewById(R.id.probaExampleButton))
                    .setBackgroundColor(Color.parseColor(sdk.getValue("buttonColor")));
        }

        sdk.fetch(new ProbaSdk.OnSuccessListener() {
                      @Override
                      public void onSuccess() {
                          ((MaterialButton) findViewById(R.id.probaExampleButton))
                                  .setBackgroundColor(Color.parseColor(sdk.getValue("buttonColor")));
                      }
                  },
                new ProbaSdk.OnErrorListener() {
                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
    }
}
