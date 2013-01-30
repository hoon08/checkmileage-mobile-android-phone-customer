package kr.co.bettersoft.checkmileage.domain;
/*
 * viewwrapper 예제소스 --  이미지, 텍스트로 이루어져있음.
 * (사용 안함)
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
