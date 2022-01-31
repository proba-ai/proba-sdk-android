package ai.proba.example;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import ai.proba.example.R;
import ai.proba.probasdk.ProbaSdk;
import com.google.android.material.button.MaterialButton;
import com.my.tracker.MyTracker;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String MY_TRACKER_ID = "16733057808593240177";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> defaults = new HashMap<>();
        defaults.put("buttonColor", "blue");

        MyTracker.initTracker(MY_TRACKER_ID, getApplication());

        final ProbaSdk sdk = new ProbaSdk.Builder(this)
                .appId("25732")
                .sdkToken("430BBA69FBBC434AA6C1529F1E160EAD")
                .myTrackerId(MY_TRACKER_ID)
                .defaults(defaults)
                .build();

        ((MaterialButton) findViewById(R.id.probaExampleButton))
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyTracker.trackEvent("on button click event <java>");
                                MyTracker.flush();
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
