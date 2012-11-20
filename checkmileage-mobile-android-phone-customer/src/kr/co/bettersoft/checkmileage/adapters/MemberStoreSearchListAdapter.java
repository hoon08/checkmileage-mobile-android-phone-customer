package kr.co.bettersoft.checkmileage.adapters;

import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * 가맹점 검색용 아답터. 가맹점 목록 --> 그리드 뷰로 화면에 보여줌. 1줄에 두개. 정렬 되있음.
 */
public class MemberStoreSearchListAdapter extends BaseAdapter {
	private Context context;
	private final List<CheckMileageMerchants> entries;
 
	public MemberStoreSearchListAdapter(Context context, List<CheckMileageMerchants> entriesFn) {		
		this.context = context;
		this.entries = entriesFn;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View gridView;
 
		if (convertView == null) {
 
			gridView = new View(context);
 
			// get layout from mobile.xml
			gridView = inflater.inflate(R.layout.member_store_list_viewwrapper_row, null);
 
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
			gridView = (View) convertView;
		}
 
		// ** 이미지 반복 현상을 없애기 위해 위쪽의 소스를 여기로 옮겨준다. --> 문제 해결 됨.
		// set value into textview
		TextView textView = (TextView) gridView
				.findViewById(R.id.label);
		textView.setText(((CheckMileageMerchants)entries.get(position)).getCompanyName());
		// set image based on selected text
		ImageView imageView = (ImageView) gridView
				.findViewById(R.id.icon);
//		String mobile = entries[position];
		imageView.setImageBitmap(((CheckMileageMerchants)entries.get(position)).getMerchantImage());
		
		return gridView;
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