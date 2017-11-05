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
	String[] from = { "detail", "word", "time" }; // ������ListView��ʾ����ÿһ�е�����
	int[] to = { R.id.detail, R.id.word, R.id.time }; // ������ListView��ʾÿһ�ж�Ӧ��list_item�пؼ���id

	List<HashMap<String, String>> list = null;
	HashMap<String, String> map = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hongbao_history_list); // ΪMainActivity����������
		// ����ArrayList����
		list = new ArrayList<HashMap<String, String>>();
		// �����ݴ�Ž�ArrayList�����У����ݰ��ŵĽṹ�ǣ�ListView��һ�����ݶ�Ӧһ��HashMap����
		// HashMap������������Ϊ�����Ը��е�ֵ��ΪValue����������Ϣ��ӽ�map�У�Ȼ���ٰ�ÿһ�ж�Ӧ
		// ��map������ӵ�ArrayList��
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
			String detail = "��" + friend + "�ĺ��������" + amount + "Ԫ";
			map.put("detail", detail);
			map.put("word", word);
			map.put("time", date);
			list.add(map);
		}
		ListView listView = (ListView) findViewById(R.id.list);

		// ����һ��SimpleAdapter����
		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.hongbao_history_item, from, to);
		// ����ListActivity��setListAdapter������ΪListView����������
		listView.setAdapter(adapter);
	}
}
