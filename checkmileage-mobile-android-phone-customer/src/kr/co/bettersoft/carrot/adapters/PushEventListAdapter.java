package kr.co.bettersoft.carrot.adapters;
// 푸시 이벤트 목록용 아답터
import java.util.List;

import kr.co.bettersoft.carrot.domain.CheckMileagePushEvent;
import kr.co.bettersoft.carrot.activities.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PushEventListAdapter extends BaseAdapter{
	private Context context;
	private final List<CheckMileagePushEvent> entries;
 
	public PushEventListAdapter(Context context, List<CheckMileagePushEvent> entriesFn) {		
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
			listView = inflater.inflate(R.layout.push_list_viewwrapper, null);
			
		} else {
			listView = (View) convertView;
		}
 
		// set value into textview
		TextView textViewSubject = (TextView) listView
				.findViewById(R.id.push_list_wrapper_subject);
		textViewSubject.setText(((CheckMileagePushEvent)entries.get(position)).getSubject());
//		System.out.println("((CheckMileagePushEvent)entries.get(position)).getSubject():"+((CheckMileagePushEvent)entries.get(position)).getSubject());
		// set value into textview
		TextView textViewCompanyName = (TextView) listView
				.findViewById(R.id.push_list_wrapper_companyname);
		textViewCompanyName.setText(((CheckMileagePushEvent)entries.get(position)).getCompanyName());
//		System.out.println("((CheckMileagePushEvent)entries.get(position)).getCompanyName():"+((CheckMileagePushEvent)entries.get(position)).getCompanyName());
		// set value into textview
		TextView textViewModifyDate = (TextView) listView
				.findViewById(R.id.push_list_wrapper_modifydate);
		textViewModifyDate.setText(((CheckMileagePushEvent)entries.get(position)).getModifyDate());		
		TextView textViewContent = (TextView) listView
		.findViewById(R.id.push_list_wrapper_content);
		textViewContent.setText(((CheckMileagePushEvent)entries.get(position)).getContent());
		// set image based on selected text
		ImageView imageView = (ImageView) listView
				.findViewById(R.id.push_list_wrapper_imageFile);
		imageView.setImageBitmap(((CheckMileagePushEvent)entries.get(position)).getImageFile());
		
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
