package com.example.map1app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.map1app.EditPage.DB_FILE;
import static com.example.map1app.EditPage.DB_TABLE;
import static com.example.map1app.SettingPage.LOC_DIST_S;
import static com.example.map1app.SettingPage.LOC_TIME_S;
import static com.example.map1app.SettingPage.LOC_ZOOM_S;

public class FirstPage extends AppCompatActivity {

    private ImageButton mBtnStart;
    private ImageButton mBtnRecord;

//    private Dialog mDlgSetting;

    private TravelDbOpenHelper mTravelDbOpenHelper;
    private SQLiteDatabase mTravelDb;

    public static final String LOC_TIME = "LTime";
    public static final String LOC_DIST = "LDist";
    public static final String LOC_ZOOM = "LZoom";

    private int locTime = 5000;
    private int locDist = 5;
    private float locZoom = 16;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage_layout);          // 這行要記得加

        mBtnStart = findViewById(R.id.btnStart);
        mBtnRecord = findViewById(R.id.btnRecord);

        mBtnStart.setOnClickListener(mBtnStartOnClick);
        mBtnRecord.setOnClickListener(mBtnRecordOnClick);

        locTime = getIntent().getIntExtra(LOC_TIME_S, 5000);
        locDist = getIntent().getIntExtra(LOC_DIST_S, 5);
        locZoom = getIntent().getFloatExtra(LOC_ZOOM_S,16);

    }

    private View.OnClickListener mBtnStartOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(FirstPage.this, MapsActivity.class);  // 這是切畫面
            intent.putExtra(LOC_TIME, locTime);
            intent.putExtra(LOC_DIST, locDist);
            intent.putExtra(LOC_ZOOM, locZoom);
            FirstPage.this.startActivity(intent);
        }
    };

    private View.OnClickListener mBtnRecordOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(FirstPage.this, RecordPage.class);  // 這是切畫面
            FirstPage.this.startActivity(intent);
        }
    };

    // Menu 選單
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_first, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSetting:

                Intent intent = new Intent(FirstPage.this, SettingPage.class);  // 這是切畫面
                startActivity(intent);
                return true;

            case R.id.menuReset:

                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(FirstPage.this);
                altDlgBuilder.setTitle("警告！");
                altDlgBuilder.setMessage("即將刪除並重置全部設定及紀錄！\n確定要繼續執行嗎?");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                altDlgBuilder.setCancelable(true);
                altDlgBuilder.setNegativeButton("確定",      // NegativeButton在左邊(不能指定位置)，設定為"確定"
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 重置定位精準度參數
                                locTime = 5000;
                                locDist = 5;
                                locZoom = 16;

                                // 資料庫前置作業
                                mTravelDbOpenHelper = new TravelDbOpenHelper(getApplicationContext(), DB_FILE, null, 1);
                                // 刪除 SQLiteDatabase資料庫
                                deleteDatabase(DB_FILE);  // 如果資料庫版本出問題，再開啟 deleteDatabase()，正常開啟時記得關掉

                                // 啟動 SQLiteDatabase
                                mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
                                // 刪除全部資料
                                Cursor cursor = mTravelDb.rawQuery(
                                        "select DISTINCT tbl_name from sqlite_master where tbl_name = '" +
                                                DB_TABLE + "'", null);
                                if (cursor != null) {
                                    if (cursor.getCount() == 0) {    // 沒有資料表，需要建立一個資料表
                                        mTravelDb.execSQL(
                                                "CREATE TABLE " + DB_TABLE + " (" +
                                                        "_id INTEGER PRIMARY KEY," +
                                                        "id TEXT," +
                                                        "time TEXT," +
                                                        "location TEXT," +
                                                        "title TEXT," +
                                                        "snippet TEXT);");
                                    }
                                    cursor.close();
                                }
                                // 關閉資料庫(這很重要 !!!)
                                mTravelDb.close();

                                Toast.makeText(FirstPage.this, "已重置全部設定！感謝您的使用！", Toast.LENGTH_LONG).show();
                            }
                        });
                altDlgBuilder.setPositiveButton("取消",       // PositiveButton在右邊，設定為"取消"
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                altDlgBuilder.show();
                return true;

            case R.id.menuItemAbout:

                new AlertDialog.Builder(FirstPage.this)
                        .setTitle("關於行腳輕旅行")
                        .setMessage("請多多支持我們！")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.star_on)
                        .setPositiveButton("確定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .show();
                return true;

            case R.id.menuItemExit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
