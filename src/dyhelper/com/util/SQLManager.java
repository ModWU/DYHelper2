package dyhelper.com.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLManager {
	private static final String DB_NAME = "hongbaoRecord.db";
	private static final String RECORD_TABLE_NAME = "record";
	private static SQLiteDatabase db = null;
	private static SQLManager sqlManager;
	private static Context context;

	public static SQLManager getInstance(Context context) {
		SQLManager.context = context;
		if (sqlManager == null) {
			sqlManager = new SQLManager();
		}
		return sqlManager;
	}

	public SQLiteDatabase getDB() {
		if (db == null || db.isOpen() == false) {
			db = SQLManager.context.openOrCreateDatabase(DB_NAME,
					Context.MODE_PRIVATE, null);
		}
		createRecordTable();
		return db;
	}

	private void createRecordTable() {
		String record_table = "create table if not exists "
				+ RECORD_TABLE_NAME
				+ " (_id integer primary key autoincrement,friend text,amount text,word text ,timestamp text)";
		Log.i("qhb", "record_table:" + record_table);
		db.execSQL(record_table);
	}

	public long insertData(String tableName, HashMap<String, String> map) {
		ContentValues cv = new ContentValues();
		for (Entry<String, String> entry : map.entrySet()) {
			cv.put(entry.getKey(), entry.getValue());
		}
		long insertRes = getDB().insert(tableName, null, cv);
		Log.i("qhb", "insertRes:" + insertRes);
		return insertRes;
	}

	public List<HashMap<String, String>> queryData(String tableName) {
		List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

		Cursor cursor = getDB().query(tableName, null, null, null, null, null,
				"_id desc");

		// 判断游标是否为空
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				HashMap<String, String> oneData = new HashMap<String, String>();
				int id = cursor.getInt(0);
				oneData.put("friend", cursor.getString(1));
				oneData.put("amount", cursor.getString(2));
				oneData.put("word", cursor.getString(3));
				oneData.put("timestamp", cursor.getString(4));
				dataList.add(oneData);
				cursor.moveToNext();
			}
		}
		Log.i("qhb", "dataList:" + dataList.size());
		return dataList;
	}
}
