package com.example.map1app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingPage extends AppCompatActivity {

    private EditText mEdtLocTime, mEdtLocDist, mEdtZoom;
    private Button mBtnSettingOK, mBtnSettingCancel;

    public static final String LOC_TIME_S = "LTime";
    public static final String LOC_DIST_S= "LDist";
    public static final String LOC_ZOOM_S = "LZoom";

    private int locTime = 5000;
    private int locDist = 5;
    private float locZoom = 16;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);          // 這行要記得加

        mEdtLocTime = findViewById(R.id.edtLocTime);
        mEdtLocDist = findViewById(R.id.edtLocDist);
        mEdtZoom = findViewById(R.id.edtZoom);

        mBtnSettingOK = findViewById(R.id.btnSettingOK);
        mBtnSettingCancel = findViewById(R.id.btnSettingCancel);

        mBtnSettingOK.setOnClickListener(btnSettingOKOnClickListener);
        mBtnSettingCancel.setOnClickListener(btnSettingCancelOnClickListener);

    }

    private View.OnClickListener btnSettingOKOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mEdtLocTime.getText().toString().matches("") || mEdtLocDist.getText().toString().matches("") || mEdtZoom.getText().toString().matches("")) {
                Toast.makeText(SettingPage.this, "所有欄位必須填入，請重新輸入。", Toast.LENGTH_LONG).show();
            } else {
                if ((Integer.valueOf(mEdtLocTime.getText().toString()) < 1 || Integer.valueOf(mEdtLocTime.getText().toString()) > 1000) || (Integer.valueOf(mEdtLocDist.getText().toString()) < 1 || Integer.valueOf(mEdtLocDist.getText().toString()) > 10000) || (Float.valueOf(mEdtZoom.getText().toString()) < 1 || Float.valueOf(mEdtZoom.getText().toString()) > 21)) {
                    mEdtLocTime.setText("5");
                    locTime = 5000;
                    mEdtLocDist.setText("5");
                    locDist = 5;
                    mEdtZoom.setText("16");
                    locZoom = 16;
                    Toast.makeText(SettingPage.this, "超出預設範圍，請重新輸入。", Toast.LENGTH_LONG).show();
                } else {
                    locTime = Integer.valueOf(mEdtLocTime.getText().toString()) * 1000;   // 單位為 ms，所以要*1000
                    locDist = Integer.valueOf(mEdtLocDist.getText().toString());
                    locZoom = Float.valueOf(mEdtZoom.getText().toString());

                    Intent intent = new Intent(SettingPage.this, FirstPage.class);
                    intent.putExtra(LOC_TIME_S, locTime);
                    intent.putExtra(LOC_DIST_S, locDist);
                    intent.putExtra(LOC_ZOOM_S, locZoom);
                    SettingPage.this.startActivity(intent);
                    SettingPage.this.finish();
                }
            }

        }
    };

    private View.OnClickListener btnSettingCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SettingPage.this.finish();
        }
    };
}
