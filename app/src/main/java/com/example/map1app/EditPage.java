package com.example.map1app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EditPage extends AppCompatActivity {

    private TextView mTxtInfo;
    private EditText mEdtTitle, mEdtMultiText;
    private String mId;
    private String mTime;
    private String mTitle;
    private String mSnippet;
    private String mAddr;
    private Button mBtnEdt, mBtnShow, mBtnCancel;
    private String itMid, itId, itTime, itLocation, itTitle, itSnippet;

    public static final String KEY = "ArrayIntent";

    // 定義 資料庫檔名,資料表檔名 (用 public 其他類別才看的到)
    public static final String
            DB_FILE = "traveldb.db",
            DB_TABLE = "traveltbls";
    // 定義 TravelDbOpenHelper 類別的變數
    private TravelDbOpenHelper mTravelDbOpenHelper;
    // 定義 SQLiteDatabase 類別的變數
    private SQLiteDatabase mTravelDb;

//    private String mId2 = "m5";
//    private String mTime2 = "2020-12-16T08:50:00";
//    private String mTitle2 = "嘉義縣";
//    private String mSnippet2 = "這是測試字串";
//    private String mAddr2 = "110 這是地址";

    @Override
    public boolean deleteDatabase(String name) {
        return super.deleteDatabase(name);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editpage_layout);

//        deleteDatabase(DB_FILE);  // 如果資料庫版本出問題，再開啟 deleteDatabase()，正常開啟時記得關掉

        mTravelDbOpenHelper = new TravelDbOpenHelper(getApplicationContext(), DB_FILE, null, 1);
        mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
        Log.d("測試資料庫 prepare", "prepare");

        // 檢查資料表是否已經存在, rawQuery可以有2或3個參數, 第2個參數是 where子句的傳入字串,第3個參數是取消正在進行的操作
        // 有 cursor 在的地方不要加 Log.d()
        Cursor cursor = mTravelDb.rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '" +
                        DB_TABLE + "'", null);

        if (cursor != null) {
            if (cursor.getCount() == 0) {    // 沒有資料表，需要建立一個資料表

//                mTravelDb.execSQL(
//                        "DROP TABLE IF EXISTS " + DB_TABLE + ";");      // 如果資料庫出問題，可以刪除資料表試看看

                mTravelDb.execSQL(
                        "CREATE TABLE " + DB_TABLE + " (" +
                                "_id INTEGER PRIMARY KEY," +
                                "id TEXT," +
                                "time TEXT," +
                                "location TEXT," +
                                "title TEXT," +
                                "snippet TEXT);");

                Log.d("新建資料表", "新建DB_TABLE");
            }

            cursor.close();
        }
        mTravelDb.close();



        mTxtInfo = findViewById(R.id.txtInfo);
        mEdtTitle = findViewById(R.id.edtTitle);
        mEdtMultiText = findViewById(R.id.edtMultiText);
        mBtnEdt = findViewById(R.id.btnEdt);
        mBtnShow = findViewById(R.id.btnShow);
        mBtnCancel = findViewById(R.id.btnClear);

        mBtnEdt.setOnClickListener(mBtnEdtOnClick);
        mBtnShow.setOnClickListener(mBtnShowOnClick);
        mBtnCancel.setOnClickListener(mBtnCancelOnClick);

        mId = getIntent().getStringExtra(MapsActivity.KEYID);    // 從背包取值
        mTime = getIntent().getStringExtra(MapsActivity.KEY1);    // 從背包取值
        mTitle = getIntent().getStringExtra(MapsActivity.KEY2);
        mSnippet = getIntent().getStringExtra(MapsActivity.KEY3);
        mAddr = mSnippet;

        mTxtInfo.setText("時間:"+ mTime + "\n座標:" + mId + "\n地點:" + mSnippet);
        mEdtTitle.setText(mTitle);
//        mEdtMultiText.setText(mSnippet +"\n");


    }


    private View.OnClickListener mBtnEdtOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String sTitle = mEdtTitle.getText().toString();
            String sSnippet = mEdtMultiText.getText().toString();

            // 開啟資料庫
            SQLiteDatabase mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
            // 跳出儲存對話框
            AlertDialog.Builder altDlgBuilder =
                    new AlertDialog.Builder(EditPage.this);
            altDlgBuilder.setTitle("注意！");
            altDlgBuilder.setMessage("即將儲存紀錄。\n確定要繼續執行嗎?");
            altDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            altDlgBuilder.setCancelable(false);
            altDlgBuilder.setNegativeButton("確定",      // NegativeButton在左邊(不能指定位置)，設定為"確定"
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ContentValues newRow = new ContentValues();
                            newRow.put("id", mId);
                            newRow.put("time", mTime);
                            newRow.put("location", mAddr);
                            newRow.put("title", sTitle);
                            newRow.put("snippet", sSnippet);
                            mTravelDb.insert(DB_TABLE, null, newRow);
                            Log.d("測試資料庫 insert", mId.replace("m","地點") + "," + sSnippet);
                            mTravelDb.close();

                            Toast.makeText(EditPage.this, "記錄已儲存", Toast.LENGTH_LONG).show();

                            EditPage.this.finish();
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
        }
    };

    private View.OnClickListener mBtnShowOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(EditPage.this, RecordPage.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener mBtnCancelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder altDlgBuilder =
                    new AlertDialog.Builder(EditPage.this);
            altDlgBuilder.setTitle("注意！");
            altDlgBuilder.setMessage("取消將不能再編輯相同地標的內容。\n確定要繼續執行嗎?");
            altDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            altDlgBuilder.setCancelable(false);
            altDlgBuilder.setNegativeButton("確定",      // NegativeButton在左邊(不能指定位置)，設定為"確定"
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            EditPage.this.finish();
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

        }
    };

}
