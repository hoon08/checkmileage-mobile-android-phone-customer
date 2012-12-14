package kr.co.bettersoft.checkmileage.domain;
/*
 * viewwrapper �����ҽ� --  �̹���, �ؽ�Ʈ�� �̷��������.
 * (��� ����)
 * 
 */
import kr.co.bettersoft.checkmileage.activities.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MemberStoreListViewWrapper {
	  View base;
	  TextView label=null;
	  ImageView icon=null;
	  
	  public MemberStoreListViewWrapper(View base) {
		    this.base=base;
		  }
	  public TextView getLabel() {
		    if (label==null) {
		      label=(TextView)base.findViewById(R.id.label);
		    }
		    return(label);
		  }
	  public ImageView getIcon() {
		    if (icon==null) {
		      icon=(ImageView)base.findViewById(R.id.icon);
		    }
		    return(icon);
		  }
}