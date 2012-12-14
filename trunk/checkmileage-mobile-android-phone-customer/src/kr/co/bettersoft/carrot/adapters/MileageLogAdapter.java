package kr.co.bettersoft.carrot.adapters;

import java.util.List;

import kr.co.bettersoft.carrot.activities.MemberStoreLogPageActivity;
import kr.co.bettersoft.carrot.domain.CheckMileageMemberMileageLogs;
import kr.co.bettersoft.carrot.domain.CheckMileageMerchants;
import kr.co.bettersoft.carrot.activities.R;


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * 마일리지 로그용 아답터
 */
public class MileageLogAdapter extends BaseAdapter {
	private Context context;
	private final List<CheckMileageMemberMileageLogs> entries;
 
	public MileageLogAdapter(Context context, List<CheckMileageMemberMileageLogs> entriesFn) {		
		this.context = context;
		this.entries = entriesFn;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View listView;
 
		if (convertView == null) {
 
			listView = new View(context);
 
			listView = inflater.inflate(R.layout.member_store_log_list, null);
 
			// ** 이 소스를 여기에 두면 한 화면의 이미지가 반복 된다. 아래쪽 return 위로 옮겨주면 이미지 반복 현상이 없다. 
//			// set value into textview
//			TextView textView = (TextView) gridView
//					.findViewById(R.id.label);
//			textView.setText(((CheckMileageMerchants)entries.get(position)).getCompanyName());
//			// set image based on selected text
//			ImageView imageView = (ImageView) gridView
//					.findViewById(R.id.icon);
////			String mobile = entries[position];
//			imageView.setImageBitmap(((CheckMileageMerchants)entries.get(position)).getMerchantImage());
		} else {
			listView = (View) convertView;
		}
 
		// ** 이미지 반복 현상을 없애기 위해 위쪽의 소스를 여기로 옮겨준다. --> 문제 해결 됨.
		// set value into textview
		TextView textView1 = (TextView) listView
		.findViewById(R.id.merchant_log_info2);
		textView1.setText(MemberStoreLogPageActivity.storeName);

		TextView textView2 = (TextView) listView
		.findViewById(R.id.merchant_log_content);
		textView2.setText(((CheckMileageMemberMileageLogs)entries.get(position)).getContent());

		TextView textView3 = (TextView) listView
		.findViewById(R.id.merchant_log_mileage);
		textView3.setText("("+((CheckMileageMemberMileageLogs)entries.get(position)).getMileage()+")");

		TextView textView4 = (TextView) listView
		.findViewById(R.id.merchant_log_time2);
		textView4.setText(((CheckMileageMemberMileageLogs)entries.get(position)).getModifyDate());

		return listView;
	}
 
	@Override
	public int getCount() {
		return entries.size();
	}
 
	@Override
	public Object getItem(int position) {
		return null;
	}
 
	@Override
	public long getItemId(int position) {
		return 0;
	}
 
}