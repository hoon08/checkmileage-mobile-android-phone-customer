package com.utils.adapters;

import java.util.List;

import com.kr.bettersoft.domain.CheckMileageMerchants;

import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/*
 * ������ ��Ϻ��⿡ ������ �ƴ���. ������ ��� --> �׸��� ��� ȭ�鿡 ������. 1�ٿ� �ΰ�. ���� ������.
 */
public class ImageAdapter extends BaseAdapter {
	private Context context;
	private final List<CheckMileageMerchants> entries;
 
	public ImageAdapter(Context context, List<CheckMileageMerchants> entriesFn) {		
		this.context = context;
		this.entries = entriesFn;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) {
 
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View gridView;
 
		if (convertView == null) {
 
			gridView = new View(context);
 
			// get layout from mobile.xml
			gridView = inflater.inflate(R.layout.member_store_list_viewwrapper_row, null);
 
			// set value into textview
			TextView textView = (TextView) gridView
					.findViewById(R.id.label);
			textView.setText(((CheckMileageMerchants)entries.get(position)).getCompanyName());
 
			// set image based on selected text
			ImageView imageView = (ImageView) gridView
					.findViewById(R.id.icon);
 
//			String mobile = entries[position];
			imageView.setImageBitmap(((CheckMileageMerchants)entries.get(position)).getMerchantImage());
 
		} else {
			gridView = (View) convertView;
		}
 
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