package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 메인 메뉴들. 탭1:내QR보기 , 탭2:내마일리지, 탭3:가맹점목록, 탭4:설정
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
        		.setIndicator("내QR코드", getResources().getDrawable(R.drawable.tab01_indicator))
      .setContent(new Intent(this, MyQRPageActivity.class))); // Optimizer.class 소스는 tab_1 탭에에 속함. Optimizer.java
//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_2")
        		.setIndicator("마일리지", getResources().getDrawable(R.drawable.tab02_indicator))
    			.setContent(new Intent(this, MyMileagePageActivity.class)));  
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_3")
        		.setIndicator("가맹점", getResources().getDrawable(R.drawable.tab03_indicator))
        		.setContent(R.id.third));
        
        // 설정
        tabhost.addTab(tabhost.newTabSpec("tab_4")
//        		.setIndicator("탭4", getResources().getDrawable(R.drawable.tab04_indicator))
//        		.setContent(R.id.fourth));
        		.setIndicator("설정", getResources().getDrawable(R.drawable.tab04_indicator))
    			.setContent(new Intent(this, com.pref.PrefActivityFromResource.class)));  
        
	}
	
}
