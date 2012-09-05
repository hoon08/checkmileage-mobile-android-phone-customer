package com.utils.adapters;

import java.util.List;

import com.kr.bettersoft.domain.CheckMileageMerchants;
import com.kr.bettersoft.domain.CheckMileageMileage;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * 가맹점 목록보기에 나오는 아답터. 가맹점 목록 --> 그리드 뷰로 화면에 보여줌. 1줄에 두개. 정렬 되있음.
 */
public class ImageAdapterList extends BaseAdapter {
	private Context context;
	private final List<CheckMileageMileage> entries;
 
	public ImageAdapterList(Context context, List<CheckMileageMileage> entriesFn) {		
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
			textViewMerchantPhone.setText(((CheckMileageMileage)entries.get(position)).getWorkPhoneNumber());
			
			// set image based on selected text
			ImageView imageView = (ImageView) listView
					.findViewById(R.id.merchantImage);
			imageView.setImageBitmap(((CheckMileageMileage)entries.get(position)).getMerchantImage());
 
		} else {
			listView = (View) convertView;
		}
 
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