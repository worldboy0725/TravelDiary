package com.example.map1app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.map1app.EditPage.DB_FILE;
import static com.example.map1app.EditPage.DB_TABLE;

public class RecordPage extends AppCompatActivity {

    private TextView mTxtR;
    private ListView mListViewRegion;
    private Button mBtnDelRecord;

    private String s, s1, s2, db_s1, db_s2, db_s3, db_s4;
    private ArrayList<String> db_al1, db_al2;
    private String[] db_strs1, db_strs2;
    //    private String[] sal_arr; // 主標題:ArrayList<String>要先轉成String[]
    private String[] strs;    // 副標題:要放在迴圈外其他人才看的
    // 定義 TravelDbOpenHelper 類別的變數
    private TravelDbOpenHelper mTravelDbOpenHelper;
    // 定義 SQLiteDatabase 類別的變數
    private SQLiteDatabase mTravelDb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_layout);      // 這個不要忘記加

        mTravelDbOpenHelper = new TravelDbOpenHelper(getApplicationContext(), DB_FILE, null, 1);
        mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
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


        mTxtR = findViewById(R.id.txtR);
        mListViewRegion = findViewById(R.id.listViewRegion);
        mBtnDelRecord = findViewById(R.id.btnDelRecord);
        mTxtR.setText("記事資料");
        mBtnDelRecord.setOnClickListener(mBtnDelRecordOnClickListener);

        db_al1 = new ArrayList<>();
        db_al2 = new ArrayList<>();

        // 從 EditPage 儲存到資料庫，再由 RecordPage 來查詢資料庫
        // 要先準備一個 List<Map<String, Object>> 物件
        List<Map<String, Object>> mapList;
        mapList = new ArrayList<>();

        SQLiteDatabase mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
        Cursor c = null;
        c = mTravelDb.query(false, DB_TABLE, new String[]{"_id", "id", "time" ,"location", "title", "snippet"},
                null,
                null, null, null, null, null);


        if (c == null)
            return;

        if (c.getCount() == 0) {

            Toast.makeText(RecordPage.this, "沒有記事資料", Toast.LENGTH_LONG).show();

        } else {
            c.moveToFirst();     // 一定要將游標設定到最前面

            db_s1 = c.getString(2) + "\n" + c.getString(3) + "\n" +  c.getString(4);
            db_al1.add(db_s1);
            db_s2 = c.getString(5);
            db_al2.add(db_s2);
        }
        while (c.moveToNext()) {
            db_s1 = c.getString(2) + "\n" + c.getString(3) + "\n" +  c.getString(4);
            db_al1.add(db_s1);
            db_s2 = c.getString(5);
            db_al2.add(db_s2);
            mTravelDb.close();
        }

        db_strs1 = new String[db_al1.size()];
        db_strs2 = new String[db_al2.size()];
        for(int i=0; i<db_al1.size(); i++) {
            db_strs1[i] = db_al1.get(i);
            db_strs2[i] = db_al2.get(i);
        }

        // 把標題(key)與內容物件(value)綁定
        for (int i=0; i<db_strs1.length; i++) {
            Map<String, Object> item = new HashMap<>();  // item是 Map<String, String[]>物件
            item.put("dbtitle", db_strs1[i]);
            item.put("dbsnippet", db_strs2[i]);
            mapList.add(item);
        }

        // SimpleAdapter類別可以改成ListAdapter，效果一樣
        SimpleAdapter listAdapter = new SimpleAdapter(this,
                mapList,
                android.R.layout.simple_list_item_2,
                new String[]{"dbtitle","dbsnippet"},
                new int[] {android.R.id.text1, android.R.id.text2});   // 只能選android.開頭的內建樣式

        mListViewRegion.setAdapter(listAdapter);

    }

    private View.OnClickListener mBtnDelRecordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog.Builder altDlgBuilder =
                    new AlertDialog.Builder(RecordPage.this);
            altDlgBuilder.setTitle("警告！");
            altDlgBuilder.setMessage("即將刪除全部的紀錄！\n確定要繼續執行嗎?");
            altDlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            altDlgBuilder.setCancelable(true);
            altDlgBuilder.setNegativeButton("確定",      // NegativeButton在左邊(不能指定位置)，設定為"確定"
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 啟動 SQLiteDatabase
                            SQLiteDatabase mTravelDb = mTravelDbOpenHelper.getWritableDatabase();
                            // 刪除全部資料
                            mTravelDb.delete(DB_TABLE,null,null);
                            // 關閉資料庫(這很重要 !!!)
                            mTravelDb.close();

                            Toast.makeText(RecordPage.this, "紀錄已全部刪除！感謝您的使用！", Toast.LENGTH_LONG).show();

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

