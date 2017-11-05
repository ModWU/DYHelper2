package dyhelper.com.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import xyz.monkeytong.hongbao.R;
import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import dyhelper.com.util.SQLManager;

public class HongbaoHistoryActivity extends Activity {
	String[] from = { "detail", "word", "time" }; // 这里是ListView显示内容每一列的列名
	int[] to = { R.id.detail, R.id.word, R.id.time }; // 这里是ListView显示每一列对应的list_item中控件的id

	List<HashMap<String, String>> list = null;
	HashMap<String, String> map = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hongbao_history_list); // 为MainActivity设置主布局
		// 创建ArrayList对象；
		list = new ArrayList<HashMap<String, String>>();
		// 将数据存放进ArrayList对象中，数据安排的结构是，ListView的一行数据对应一个HashMap对象，
		// HashMap对象，以列名作为键，以该列的值作为Value，将各列信息添加进map中，然后再把每一列对应
		// 的map对象添加到ArrayList中
		List<HashMap<String, String>> originList = SQLManager.getInstance(this)
				.queryData("record");
		for (HashMap<String, String> originMap : originList) {
			HashMap<String, String> map = new HashMap<String, String>();
			String friend = originMap.get("friend");
			String amount = originMap.get("amount");
			String word = originMap.get("word");
			String timestamp = originMap.get("timestamp");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = sdf.format(new Date(Long.valueOf(timestamp)));
			String detail = "从" + friend + "的红包中抢到" + amount + "元";
			map.put("detail", detail);
			map.put("word", word);
			map.put("time", date);
			list.add(map);
		}
		ListView listView = (ListView) findViewById(R.id.list);

		// 创建一个SimpleAdapter对象
		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.hongbao_history_item, from, to);
		// 调用ListActivity的setListAdapter方法，为ListView设置适配器
		listView.setAdapter(adapter);
	}
}
