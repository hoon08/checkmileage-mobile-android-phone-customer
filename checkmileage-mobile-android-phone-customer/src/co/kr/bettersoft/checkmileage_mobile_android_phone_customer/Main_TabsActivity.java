package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// ���� �޴���. ��1:��QR���� , ��2:�����ϸ���, ��3:���������, ��4:����
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
        		.setIndicator("��QR�ڵ�", getResources().getDrawable(R.drawable.tab01_indicator))
      .setContent(new Intent(this, MyQRPageActivity.class))); // Optimizer.class �ҽ��� tab_1 �ǿ��� ����. Optimizer.java
//         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_2")
        		.setIndicator("���ϸ���", getResources().getDrawable(R.drawable.tab02_indicator))
    			.setContent(new Intent(this, MyMileagePageActivity.class)));  
        
        
        tabhost.addTab(tabhost.newTabSpec("tab_3")
        		.setIndicator("������", getResources().getDrawable(R.drawable.tab03_indicator))
        		.setContent(new Intent(this, MemberStoreListPageActivity.class)));
        
        // ����
        tabhost.addTab(tabhost.newTabSpec("tab_4")
//        		.setIndicator("��4", getResources().getDrawable(R.drawable.tab04_indicator))
//        		.setContent(R.id.fourth));
        		.setIndicator("����", getResources().getDrawable(R.drawable.tab04_indicator))
    			.setContent(new Intent(this, com.pref.PrefActivityFromResource.class)));  
        
	}
	@Override			// �� ��Ƽ��Ƽ(��Ʈ��)�� ����ɶ� ����. (��Ƽ��Ƽ�� �Ѿ�� �����)
	protected void onDestroy() {
		super.onDestroy();
//		Toast.makeText(Main_TabsActivity.this, "����.", Toast.LENGTH_SHORT).show();	
		dummyActivity.finish();		// ���̵� ����
		DummyActivity.count = 0;		// ���� 0���� �ʱ�ȭ �����ش�. �ٽ� ����ɼ� �ֵ���
	}
}
