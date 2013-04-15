package kr.co.bettersoft.checkmileage.activities;
/**
 * MemberStoreMapPageActivity
 * 가맹점 상세 - 지도 보기
 * 
 */
import java.util.ArrayList;
import java.util.List;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.activities.MyQRPageActivity.backgroundUpdateLogToServer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

//import co.kr.bettersoft.checkmileage_mobile_android_phone_customer.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MemberStoreMapPageActivity extends MapActivity {
	
	String TAG = "MemberStoreMapPageActivity";
	
	OverlayItem overlayitem;
	private Drawable marker = null;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();

	MapController mapControl;
	MapView mMap;
	MyLocationOverlay2 mLocation;
	String latatude = "";
	String longitude = "";
	String companyName = "";
	int myLat = 0;
	int myLon = 0;

	int storeLat = 0;
	int storeLon = 0;

	GeoPoint gt;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_store_map);

		
		// 가맹점 좌표는 받은 것을 사용
		Intent rIntent = getIntent();
		latatude = rIntent.getStringExtra("latatude");
		longitude = rIntent.getStringExtra("longitude");
		companyName = rIntent.getStringExtra("companyName");
		if(latatude.length()>0 && longitude.length()>0){
			storeLat = Integer.parseInt(latatude);
			storeLon = Integer.parseInt(longitude);
//			Log.d(TAG,"storeLat::"+storeLat+"//storeLon::"+storeLon);
			
			// test test test ***  
//			storeLat = 37429000;
//			storeLon = 126820000;
//			Log.d(TAG,"storeLat test ::"+storeLat+"//storeLon::"+storeLon);
			
		}else{
			Toast.makeText(MemberStoreMapPageActivity.this, R.string.no_saved_shop_info, Toast.LENGTH_SHORT).show();
			finish();
		}
		
		// *** 
		getMyLocationForNow();

		mMap=(MapView)findViewById(R.id.mapview);
		mapControl = mMap.getController();
		mapControl.setZoom(13);
		mMap.setBuiltInZoomControls(true);

		mLocation = new MyLocationOverlay2(this, mMap); 

		List<Overlay> overlays = mMap.getOverlays();
		Drawable marker=getResources().getDrawable(R.drawable.pos);
		marker.setBounds(0,0,marker.getIntrinsicWidth() ,marker.getIntrinsicHeight());

		overlays.add(mLocation);

//		mMap.getOverlays().add(new MyLocationOverlay2(this, mMap));		// 좌표를 추가..  test
		mMap.getOverlays().add(new SitesOverlay(marker));		// 좌표를 추가..
		
		mLocation.enableMyLocation();
		mLocation.enableCompass();
		try{
			// 시작은 내 위치로부터.
			GeoPoint pt = new GeoPoint(myLat, myLon);				// 좌표로 지점 생성.!  내 위치 --> 해당 위치로 가자.		
			mapControl.setCenter(pt);									// (사용자가 보는 화면의 좌표.)

			gt = new GeoPoint(storeLat,storeLon);
			mMap.getController().animateTo(gt);				//storeLat,storeLon	// 가맹점 위치로 이동
		}catch(Exception e){
			e.printStackTrace();
		}
		
		mLocation.runOnFirstFix(new Runnable() {		// GPS 수신시 동작.
			public void run() {
				Log.d(TAG,"mLocation.runOnFirstFix - run");
				
				mMap.getController().animateTo(mLocation.getMyLocation());
				
			}
		});
		
		
//		// *** test  // -- GPS .. - 화면에 하늘색 표시가 나타남. GPS 의존적임.
//		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		LocationListener mlocListener = new MyLocationListener();
//		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0 , mlocListener);
	}

	
//	/**
//	 * test *** 
//	 * MyLocationListener
//	 */
//	public class MyLocationListener implements LocationListener{
//		@Override
//		public void onLocationChanged(Location loc) {
//			loc.getLatitude();
//			loc.getLongitude();
//			String Text = "My Location : " + "Latitude="+loc.getLatitude() + "Longitude="+loc.getLongitude();
//			Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
//		}
//		@Override
//		public void onProviderDisabled(String provider) {
//			Toast.makeText(getApplicationContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
//		}
//		@Override
//		public void onProviderEnabled(String provider) {
//			Toast.makeText(getApplicationContext(), "onProviderEnabled", Toast.LENGTH_SHORT).show();
//		}
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
////			Toast.makeText(getApplicationContext(), "onProviderDisabled", Toast.LENGTH_SHORT).show();
//		}
//	}
	
	
	// 새로 좌표값을 구한다.
	public void getMyLocationForNow(){
		try{
//			Log.d(TAG,"get my location start");
			LocationManager lm;
			Location location;
			String provider;
			String bestProvider;
			lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			
			
			provider = LocationManager.GPS_PROVIDER;
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 정확도
			criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원 소비량
			criteria.setAltitudeRequired(false); // 고도
			criteria.setBearingRequired(false); // ..
			criteria.setSpeedRequired(false); // 속도
			criteria.setCostAllowed(true); // 금전적 비용
			bestProvider = lm.getBestProvider(criteria, true);
			location =  lm.getLastKnownLocation(bestProvider);
			if(location!=null){
				myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
				myLon = (int) (location.getLongitude()*1000000);	
				Log.d(TAG, "runOnFirstFix// location1:"+myLat+", "+myLon);			// 37529466 126921069
//				Toast.makeText(getApplicationContext(), bestProvider+"-location:"+myLat+", "+myLon, Toast.LENGTH_SHORT).show();	// *** 
				//			new backgroundUpdateLogToServer().execute();	// 비동기로 서버에 위치 업뎃		
			}else{		// gps
				location =  lm.getLastKnownLocation(provider);
				if(location==null){
					Log.d(TAG,"location = null");	
				}else{
					myLat = (int) (location.getLatitude()*1000000);				// 현위치의 좌표 획득
					myLon = (int) (location.getLongitude()*1000000);	
					Log.d("runOnFirstFix", "location2:"+myLat+", "+myLon);		
//					Toast.makeText(getApplicationContext(), "GPS_PROVIDER-location2:"+myLat+", "+myLon, Toast.LENGTH_SHORT).show();	// *** 
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			Log.w(TAG,"fail to update my location to server");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * getPoint
	 *  좌표 단위 수정한다
	 *
	 * @param lat
	 * @param lon
	 * @return 
	 */
	private GeoPoint getPoint(double lat, double lon) {
		return (new GeoPoint((int) (lat * 1000000.0), (int) (lon * 1000000.0)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	public void onResume(){
		super.onResume();
		mLocation.enableMyLocation();
		mLocation.enableCompass();
	}

	public void onPause(){
		super.onPause();
		mLocation.disableMyLocation();
		mLocation.disableCompass();
	}

	/**
	 * MyLocationOverlay2
	 *  맵위에 오버레이 (터치시 이벤트)
	 *
	 */
	class MyLocationOverlay2 extends MyLocationOverlay{
		public MyLocationOverlay2(Context context,MapView mapView){
			super(context,mapView);
		}
		
		protected boolean dispatchTap(){	// 안에있어야 동작. 터치시 토스트. 
			// 실시간 위치
//			Log.d(TAG,"MyLocationOverlay2 dispatchTap");
//			Toast.makeText(MemberStoreMapPageActivity.this, getString(R.string.its_user_location)+"(l:"+myLat+", "+myLon+")", Toast.LENGTH_SHORT).show();
			Toast.makeText(MemberStoreMapPageActivity.this, R.string.its_user_location, Toast.LENGTH_SHORT).show();
			//			Toast.makeText(MemberStoreMapPageActivity.this, "여기가 사용자위치입니다:"+myLat+","+myLon, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	
	
	/**
	 * SitesOverlay
	 *  맵위에 가맹점 마커를 추가한다.
	 *
	 */
	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private Drawable marker = null;
		private List<OverlayItem> items = new ArrayList<OverlayItem>();
		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;
//			String shop_loc_msg = getString(R.string.its_shop_location);
			
			
			String customer_loc_msg = getString(R.string.its_user_location)+"(m:"+myLat+", "+myLon+")";	// *** 
//			Log.d(TAG,"storeLat:"+storeLat+"//storeLon:"+storeLon);
//			Log.d(TAG,"myLat:"+myLat+"//myLon:"+myLon);
//			items.add(new OverlayItem(new GeoPoint(storeLat,storeLon), "Store", shop_loc_msg));
			items.add(new OverlayItem(new GeoPoint(storeLat,storeLon), "Store", companyName));
//			items.add(new OverlayItem(new GeoPoint(myLat,myLon), "Customer", customer_loc_msg));	// *** 
			
			//			items.add(new OverlayItem(getPoint(40.748963847316034,-73.96807193756104), "UN", "United Nations"));
			//			items.add(new OverlayItem(getPoint(40.76866299974387,-73.98268461227417), "Lincoln Center","Home of Jazz at Lincoln Center"));
			//			items.add(new OverlayItem(getPoint(40.765136435316755,-73.97989511489868), "Carnegie Hall","Where you go with practice, practice, practice"));
			//			items.add(new OverlayItem(getPoint(40.70686417491799,-74.01572942733765), "The Downtown Club","Original home of the Heisman Trophy"));
			// 생성된 OverlayItem 을 목록으로 지정
			populate();
		}

		// 지정한 번호에 대해 OverlayItem을 return

		@Override
		protected OverlayItem createItem(int i) {
			return items.get(i);
		}
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			// 마커의 아래의 중간 부분이 좌표에 위치하도록 지정
			boundCenterBottom(marker);
		}
		@Override
		protected boolean onTap(int i) {
			Toast.makeText(MemberStoreMapPageActivity.this, items.get(i).getSnippet(),Toast.LENGTH_SHORT).show();
			return true;
		}
		//레이어가 처리할 수 있는 항목의 개수를 리턴
		@Override
		public int size() {
			return items.size();
		}
	}

	
	
    ////////////////////////   하드웨어 메뉴 버튼.  ////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         String tmpstr = getString(R.string.my_location );
         menu.add(Menu. NONE , Menu.FIRST+1, Menu. NONE, tmpstr );             // 신규등록 메뉴 추가.
         tmpstr = getString(R.string.shop_location );
         menu.add(Menu. NONE , Menu.FIRST+2, Menu. NONE, tmpstr );  
          //              getMenuInflater().inflate(R.menu.activity_main, menu);
          return (super .onCreateOptionsMenu(menu));
   }
    // 옵션 메뉴 특정 아이템 클릭시 필요한 일 처리
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
          return (itemCallback(item)|| super .onOptionsItemSelected(item));
   }
    // 아이템 아이디 값 기준 필요한 일 처리
    public boolean itemCallback(MenuItem item){
          switch (item.getItemId()){
          case Menu. FIRST+1:
        	  // *** 
        	  getMyLocationForNow();
  			gt = new GeoPoint(myLat, myLon);
  			mMap.getController().animateTo(gt);				
              return true ;
          case Menu. FIRST+2:
        	  gt = new GeoPoint(storeLat, storeLon);
    			mMap.getController().animateTo(gt);				//storeLat,storeLon	// 가맹점 위치로 이동..  -- 안먹음
        	  return true ;
         }
          return false ;
   }
    ////////////////////////////////////////////////////////////

	
	
}
