package com.dji.ui.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

/** Activity that shows all the UI elements together */
public class CompleteWidgetActivity extends Activity {
    private Button Mapbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_widgets);
        Mapbutton=(Button)findViewById(R.id.mapButton);
        Mapbutton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                switch (v.getId()) {
                    case R.id.mapButton: {
                        Intent intent = new Intent(CompleteWidgetActivity.this, MapActivity.class);
                        startActivity(intent);
                        break;
                    }
                    default:
                        break;
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
