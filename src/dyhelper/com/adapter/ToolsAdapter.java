package dyhelper.com.adapter;

import java.util.List;
import java.util.Map;

import android.R.color;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import xyz.monkeytong.hongbao.R;


public class ToolsAdapter extends BaseAdapter {
    private List<Map<String, String>> ar;
    private Context context;
    private LayoutInflater inflater;
    
    public static final String TOOLS_IMAGE = "TOOLS_IMAGE";
    public static final String TOOLS_TITLE = "TOOLS_TITLE";
    
    public ToolsAdapter(Context context, List<Map<String, String>> ar) {
        this.context = context;
        this.ar = ar;
        inflater = LayoutInflater.from(context);
    }
    
    public int getCount() {
        return ar.size();
    }
    
    public Map<String, String> getItem(int position) {
        return ar.get(position);
    }
    
    
    @Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		Log.i("ahr", "arg0:" + arg0);
		viewHolder holder;
		if (arg1 == null) {
			holder = new viewHolder();
			arg1 = inflater.inflate(R.layout.brand_item, null);
			holder.mIvBimage = (ImageView) arg1.findViewById(R.id.iv_bimage);
			holder.mTvBname = (TextView) arg1.findViewById(R.id.tv_bname);

			arg1.setTag(holder);
			holder.update();
		} else {
			holder = (viewHolder) arg1.getTag();
		}
		Map<String, String> map = (Map<String, String>) ar.get(arg0);
		holder.mIvBimage.setImageResource(Integer.parseInt(map.get(TOOLS_IMAGE)));
		holder.mIvBimage.setBackgroundColor(color.transparent);
		holder.mTvBname.setText(map.get(TOOLS_TITLE).toString());
		int width = arg1.getWidth();
		Log.i("ahr", "width:" + width);
		// arg1.setLayoutParams(new GridView.LayoutParams(
		// width, width));
		holder.mIvBimage.setTag(arg1);
		return arg1;
	}

	private class viewHolder {
		private ImageView mIvBimage;
		private TextView mTvBname;

		public void update() {
			mTvBname.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							View view = (View) mIvBimage.getTag();
							int width = view.getWidth();
							Log.i("ahr", "width:" + width);
							view.setLayoutParams(new GridView.LayoutParams(
									width, width));
							mTvBname.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						}
					});
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
