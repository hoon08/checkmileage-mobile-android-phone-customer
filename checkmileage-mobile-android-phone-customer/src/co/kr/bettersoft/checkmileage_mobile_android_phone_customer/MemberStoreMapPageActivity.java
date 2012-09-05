package co.kr.bettersoft.checkmileage_mobile_android_phone_customer;
// 가맹점 상세 - 지도 보기
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MemberStoreMapPageActivity extends MapActivity {
	OverlayItem overlayitem;
	private Drawable marker = null;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();

	MapView mMap;
	MyLocationOverlay2 mLocation;
	String latatude = "";
	String longitude = "";
	int myLat = 0;
	int myLon = 0;

	int storeLat = 0;
	int storeLon = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_store_map);

		Intent rIntent = getIntent();
		latatude = rIntent.getStringExtra("latatude");
		longitude = rIntent.getStringExtra("longitude");
		if(latatude.length()>0 && longitude.length()>0){
			storeLat = Integer.parseInt(latatude);
			storeLon = Integer.parseInt(longitude);
		}else{
			Toast.makeText(MemberStoreMapPageActivity.this, "저장된 가맹점 정보가 없습니다.", Toast.LENGTH_SHORT).show();
			finish();
		}


		mMap=(MapView)findViewById(R.id.mapview);
		final MapController mapControl = mMap.getController();
		mapControl.setZoom(13);
		mMap.setBuiltInZoomControls(true);

		mLocation = new MyLocationOverlay2(this, mMap); 

		List<Overlay> overlays = mMap.getOverlays();
		Drawable marker=getResources().getDrawable(R.drawable.pos);
		marker.setBounds(0,0,marker.getIntrinsicWidth() ,marker.getIntrinsicHeight());


		overlays.add(mLocation);

		mMap.getOverlays().add(new SitesOverlay(marker));

		mLocation.runOnFirstFix(new Runnable() {
			public void run() {
				// 시작은 내 위치로부터.
				GeoPoint pt = new GeoPoint(mLocation.getMyLocation().getLatitudeE6(), mLocation.getMyLocation().getLongitudeE6());				// 좌표로 지점 생성.!  내 위치 --> 해당 위치로 가자.		
				mapControl.setCenter(pt);									// (사용자가 보는 화면의 좌표.)

				//                 mMap.getController().animateTo(mLocation.getMyLocation());		// 현위치
				GeoPoint gp = new GeoPoint(storeLat,storeLon);
				mMap.getController().animateTo(gp);				//storeLat,storeLon

				myLat = mLocation.getMyLocation().getLatitudeE6();				// 현위치의 좌표 획득 *** 로그용
				myLon = mLocation.getMyLocation().getLongitudeE6();
				Log.i("runOnFirstFix", "location:"+myLat+", "+myLon);			// 37529466 126921069

			}
		});
	}

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

	class MyLocationOverlay2 extends MyLocationOverlay{
		public MyLocationOverlay2(Context context,MapView mapView){
			super(context,mapView);
		}
		protected boolean dispatchTap(){	// 안에있어야 동작. 터치시 토스트. 
			Toast.makeText(MemberStoreMapPageActivity.this, "사용자위치입니다.", Toast.LENGTH_SHORT).show();
//			Toast.makeText(MemberStoreMapPageActivity.this, "여기가 사용자위치입니다:"+myLat+","+myLon, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
		private Drawable marker = null;
		private List<OverlayItem> items = new ArrayList<OverlayItem>();

		public SitesOverlay(Drawable marker) {
			super(marker);
			this.marker = marker;
			items.add(new OverlayItem(new GeoPoint(storeLat,storeLon), "Store", "가맹점 위치입니다."));
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
	
}
