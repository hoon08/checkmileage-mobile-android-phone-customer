package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 메인 메뉴들. 탭1:내QR보기 , 탭2:내마일리지, 탭3:가맹점목록, 탭4:설정
import com.pref.DummyActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

public class Main_TabsActivity extends TabActivity {
	DummyActivity dummyActivity = (DummyActivity)DummyActivity.dummyActivity;
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
        		.setContent(new Intent(this, MemberStoreListPageActivity.class)));
        
        // 설정
        tabhost.addTab(tabhost.newTabSpec("tab_4")
//        		.setIndicator("탭4", getResources().getDrawable(R.drawable.tab04_indicator))
//        		.setContent(R.id.fourth));
        		.setIndicator("설정", getResources().getDrawable(R.drawable.tab04_indicator))
    			.setContent(new Intent(this, com.pref.PrefActivityFromResource.class)));  
        
	}
	@Override			// 이 액티비티(인트로)가 종료될때 실행. (액티비티가 넘어갈때 종료됨)
	protected void onDestroy() {
		super.onDestroy();
//		Toast.makeText(Main_TabsActivity.this, "이힝.", Toast.LENGTH_SHORT).show();	
		dummyActivity.finish();		// 더미도 종료
		DummyActivity.count = 0;		// 개수 0으로 초기화 시켜준다. 다시 실행될수 있도록
	}
}
