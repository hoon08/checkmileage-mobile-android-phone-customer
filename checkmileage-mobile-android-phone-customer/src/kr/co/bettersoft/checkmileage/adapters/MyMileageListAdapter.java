package kr.co.bettersoft.checkmileage.adapters;

import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMileage;


//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * 마일리지 목록 용 아답터.
 */
public class MyMileageListAdapter extends BaseAdapter {
	private Context context;
	private final List<CheckMileageMileage> entries;
 
	public MyMileageListAdapter(Context context, List<CheckMileageMileage> entriesFn) {		
		this.context = context;
		this.entries = entriesFn;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View listView;
 
		if (convertView == null) {
 
			listView = new View(context);
 
			// get layout from mobile.xml
			listView = inflater.inflate(R.layout.my_mileage_list, null);
 
			// ** 반복 문제 해결을 위해 아래로 옮김.
//			// set value into textview
//			TextView textViewMileage = (TextView) listView
//					.findViewById(R.id.mileage);
//			textViewMileage.setText(((CheckMileageMileage)entries.get(position)).getMileage());
//			// set value into textview
//			TextView textViewMerchantName = (TextView) listView
//					.findViewById(R.id.merchantName);
//			textViewMerchantName.setText(((CheckMileageMileage)entries.get(position)).getMerchantName());
//			// set value into textview
//			TextView textViewMerchantPhone = (TextView) listView
//					.findViewById(R.id.merchantPhone);
//			textViewMerchantPhone.setText(((CheckMileageMileage)entries.get(position)).getWorkPhoneNumber());
//			// set image based on selected text
//			ImageView imageView = (ImageView) listView
//					.findViewById(R.id.merchantImage);
//			imageView.setImageBitmap(((CheckMileageMileage)entries.get(position)).getMerchantImage());
 
		} else {
			listView = (View) convertView;
		}
 
		// ** 반복 문제 해결을 위해 여기로 옮김
		// set value into textview
		TextView textViewMileage = (TextView) listView
				.findViewById(R.id.mileage);
		textViewMileage.setText(((CheckMileageMileage)entries.get(position)).getMileage());
		// set value into textview
		TextView textViewMerchantName = (TextView) listView
				.findViewById(R.id.merchantName);
		textViewMerchantName.setText(((CheckMileageMileage)entries.get(position)).getMerchantName());
		// set value into textview
		TextView textViewMerchantPhone = (TextView) listView
				.findViewById(R.id.merchantPhone);
		textViewMerchantPhone.setText(((CheckMileageMileage)entries.get(position)).getModifyDate());		// 처음엔 전번이었으나 실제 값은 마지막 이용 일시로 한다.
		
		TextView textViewMerchantIntro = (TextView) listView
		.findViewById(R.id.merchantIntro);
		textViewMerchantIntro.setText(((CheckMileageMileage)entries.get(position)).getIntroduction());
		
		// set image based on selected text
		ImageView imageView = (ImageView) listView
				.findViewById(R.id.merchantImage);
		imageView.setImageBitmap(((CheckMileageMileage)entries.get(position)).getMerchantImage());
		
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