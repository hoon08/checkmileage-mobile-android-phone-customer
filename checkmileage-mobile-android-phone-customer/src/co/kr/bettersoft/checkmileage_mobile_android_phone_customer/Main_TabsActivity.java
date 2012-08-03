package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

public class Main_TabsActivity extends TabActivity {

	static String barCode = "";
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tabs);
        
        TabHost tabhost = getTabHost();
        
        tabhost.addTab(tabhost.newTabSpec("tab_1")
        		.setIndicator("ÅÇ1", getResources().getDrawable(R.drawable.tab01_indicator))
//        		.setContent(R.id.first));
      .setContent(new Intent(this, MyQRPageActivity.class))); // Optimizer.class ¼Ò½º´Â tab_1 ÅÇ¿¡¿¡ ¼ÓÇÔ. Optimizer.java
//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_2")
        		.setIndicator("ÅÇ2", getResources().getDrawable(R.drawable.tab02_indicator))
        		.setContent(R.id.second));
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_3")
        		.setIndicator("ÅÇ3", getResources().getDrawable(R.drawable.tab03_indicator))
        		.setContent(R.id.third));
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_4")
        		.setIndicator("ÅÇ4", getResources().getDrawable(R.drawable.tab04_indicator))
        		.setContent(R.id.fourth));
        
	}
	
}
