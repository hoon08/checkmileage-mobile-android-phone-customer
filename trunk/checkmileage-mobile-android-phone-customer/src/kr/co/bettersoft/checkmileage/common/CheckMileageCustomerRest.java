package kr.co.bettersoft.checkmileage.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kr.co.bettersoft.checkmileage.activities.R;
import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.domain.Locales;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class CheckMileageCustomerRest {
	String TAG = "PushChatRest";
	
	// thread return 
	ExecutorService service;
    Future<String>  task;
    
    
    
 // 서버 통신
	int responseCode = 0;
	String controllerName ="";
	String methodName ="";
	String serverName = CommonUtils.serverNames;
	URL postUrl2 = null;
	HttpURLConnection connection2 = null;
	int isRunning = 0;		// 통신 도중 중복 호출을 방지하기 위함.
	String fullUrl = "";
	String inputJson = "";
	JSONObject jsonObject;
	
	// 현 시각
	Date today ;
	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String nowDate;
	String nowTime = "";	// 현시각
    
	// 결과
	String callResult = "";
	String tempstr = "";
    
	
	// 고객 정보 저장
	CheckMileageMembers checkMileageMembers = new CheckMileageMembers();
	// 로그
	CheckMileageLogs checkMileageLogs = new CheckMileageLogs();
	// 가맹점 정보 저장
	CheckMileageMerchants checkMileageMerchants = new CheckMileageMerchants();
	// 로케일 정보 저장
	Locales locales = new Locales();
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 서버 통신
	
	/**
	 * RestCertificationStep_1
	 *   전화번호를 보내서 인증번호를 요청한다.
	 * @return  String
	 */
	public String RestCertificationStep_1(CheckMileageMembers checkMileageMembersParam){
		String resultStr = "";
		
		checkMileageMembers = checkMileageMembersParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new CertificationStep_1());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestCertificationStep_1 result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//  전화번호를 보내서 인증번호를 요청한다.
	class CertificationStep_1 implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageCertificationController";		
			methodName = "requestCertification";		
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());	
				obj.put("activateYn", "Y");
				obj.put("modifyDate", nowDate);
				obj.put("registerDate", nowDate);
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageCertification\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
			if(callResult.equals("S")){
				try {
					jsonObject = new JSONObject(tempstr);
					String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
			    	JSONObject jsonObject2 = new JSONObject(jstring2);
			    	callResult = jsonObject2.getString("result").toString(); 
			    	Log.d(TAG,"callResult:"+callResult);
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			return callResult;
		}
	}
    
    
	/**
	 * RestCertificationStep_2
	 *   인증2단계 수행.
	 * @return  String
	 */
	public String RestCertificationStep_2(CheckMileageMembers checkMileageMembersParam){
		String resultStr = "";
		
		checkMileageMembers = checkMileageMembersParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new CertificationStep_2());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestCertificationStep_2 result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//  인증2단계 수행.
	class CertificationStep_2 implements Callable<String>{
		@Override
		public String call() throws Exception {
//			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageCertificationController";		// 서버 조회시 컨트롤러 이름
	    	methodName = "requestAdmission";							// 서버 조회시 메서드 이름	
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());			// 전번
				obj.put("certificationNumber", checkMileageMembers.getCertiNum());	// 승인번호		
				obj.put("activateYn", "Y");
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageCertification\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
			if(callResult.equals("S")){
				try {
					jsonObject = new JSONObject(tempstr);
					String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
			    	JSONObject jsonObject2 = new JSONObject(jstring2);
			    	callResult = jsonObject2.getString("result").toString(); 
			    	Log.d(TAG,"certiResult:"+callResult);
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			}
			return callResult;
		}
	}
    
    
	/**
	 * RestSaveQRtoServer
	 *    서버에 qr 저장
	 * @return  String
	 */
	public String RestSaveQRtoServer(CheckMileageMembers checkMileageMembersParam){
		String resultStr = "";
		
		checkMileageMembers = checkMileageMembersParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new SaveQRtoServer());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestSaveQRtoServer result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//   서버에 qr 저장
	class SaveQRtoServer implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageMemberController";
			methodName = "registerMember";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
				obj.put("password", "");				
				obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());			
				obj.put("email", "");			
				obj.put("birthday", "");			
				obj.put("gender", "");			
				obj.put("latitude", "");			
				obj.put("longitude", "");			
				obj.put("deviceType", "AS");			
				obj.put("registrationId", "");			
				obj.put("activateYn", "Y");			
				obj.put("receiveNotificationYn", "Y");			

				obj.put( "countryCode", checkMileageMembers.getCountryCode()); 
				obj.put( "languageCode" , checkMileageMembers.getLanguageCode());

				obj.put("modifyDate", nowTime);			
				obj.put("registerDate", nowTime);		
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
//			if(callResult.equals("S")){
//				try {
//					jsonObject = new JSONObject(tempstr);
//					String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//			    	JSONObject jsonObject2 = new JSONObject(jstring2);
//			    	callResult = jsonObject2.getString("result").toString(); 
//			    	Log.d(TAG,"certiResult:"+callResult);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				} 
//			}
			return callResult;
		}
	}
    
    
	/**
	 * RestUpdateMyGCMtoServer
	 *     서버에 gcm 업데이트한다
	 * @return  String
	 */
	public String RestUpdateMyGCMtoServer(CheckMileageMembers checkMileageMembersParam){
		String resultStr = "";
		
		checkMileageMembers = checkMileageMembersParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new UpdateMyGCMtoServer());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestUpdateMyGCMtoServer result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//    서버에 gcm 업데이트한다
	class UpdateMyGCMtoServer implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageMemberController";
			methodName = "updateRegistrationId";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("activateYn", "Y");
				obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
				obj.put("registrationId", checkMileageMembers.getRegistrationId());							
				obj.put("modifyDate", nowDate);			
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
			if(callResult.equals("S")){
				Log.i(TAG, "S to update GCM ID to server");
			}else{
				Log.i(TAG, "F to update GCM ID to server");
			}
//			try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	callResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+callResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 

			return callResult;
		}
	}
    
    
	/**
	 * RestUpdateLogToServer
	 *   사용자의 위치 정보 및 정보 로깅
	 * @return  String
	 */
	public String RestUpdateLogToServer(CheckMileageLogs checkMileageLogsParam){
		String resultStr = "";
		
		checkMileageLogs = checkMileageLogsParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new UpdateLogToServer());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestUpdateLogToServer result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//    사용자의 위치 정보 및 정보 로깅
	class UpdateLogToServer implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageLogController";
			methodName = "registerLog";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("checkMileageId", checkMileageLogs.getCheckMileageId());	// checkMileageId 	사용자 아이디
				obj.put("merchantId", "");		// merchantId		가맹점 아이디.
				obj.put("viewName", checkMileageLogs.getViewName());		// viewName			출력된 화면.
				obj.put("parameter01", checkMileageLogs.getParameter01());		// parameter01		사용자 전화번호.
				
				obj.put("parameter02", "");		// parameter02		위도.
				obj.put("parameter03", "");		// parameter03		경도.
				obj.put("parameter04", checkMileageLogs.getParameter04());		// parameter04		검색일 경우 검색어.
				obj.put("parameter05", "");		// parameter05		예비용도.
				obj.put("parameter06", "");		// parameter06		예비용도.
				obj.put("parameter07", "");		// parameter07		예비용도.
				obj.put("parameter08", "");		// parameter08		예비용도.
				obj.put("parameter09", "");		// parameter09		예비용도.
				obj.put("parameter10", "");		// parameter10		예비용도.
				obj.put("registerDate", nowDate);		// registerDate		등록 일자.
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageLog\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
			if(callResult.equals("S")){
				Log.i(TAG, "S to update GCM ID to server");
			}else{
				Log.i(TAG, "F to update GCM ID to server");
			}
//			try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	callResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+callResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 

			return callResult;
		}
	}
    
	
	/**
	 * RestGetMerchantInfo
	 *  가맹점 정보를 가져온다
	 * @return  String
	 */
	public String RestGetMerchantInfo(CheckMileageMerchants checkMileageMerchantsParam){
		String resultStr = "";
		
		checkMileageMerchants = checkMileageMerchantsParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new GetMerchantInfo());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestGetMerchantInfo result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//   가맹점 정보를 가져온다
	class GetMerchantInfo implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			// 파라미터 셋팅
			controllerName = "checkMileageMerchantController";
			methodName = "selectMerchantInformation";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("activateYn", "Y");
				obj.put("merchantId", checkMileageMerchants.getMerchantId());
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageMerchant\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
			if(callResult.equals("S")){
				Log.i(TAG, "S to update GCM ID to server");
			}else{
				Log.i(TAG, "F to update GCM ID to server");
			}
//			try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	callResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+callResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 

			return callResult;
		}
	}
    
    
	/**
	 * RestGetBusinessKindList
	 *  검색을 위한 가맹점 업종리스트를 가져온다.
	 * @return  String
	 */
	public String RestGetBusinessKindList(Locales localesParam){
		String resultStr = "";
		
		locales = localesParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new GetBusinessKindList());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestGetBusinessKindList result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//   검색을 위한 가맹점 업종리스트를 가져온다.
	class GetBusinessKindList implements Callable<String>{
		@Override
		public String call() throws Exception {
			nowDate = getNowDate();
			
			// 파라미터 셋팅
			controllerName = "checkMileageBusinessKindController";
			methodName = "selectBusinessKindList";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("countryCode", locales.getCountryCode());		// 국가 코드
				obj.put("languageCode", locales.getLanguageCode());			// 언어코드
				obj.put("activateYn", "Y");
			}catch(Exception e){
				e.printStackTrace();
			}
			inputJson = "{\"checkMileageBusinessKind\":" + obj.toString() + "}";
//			inputJson = obj.toString();
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
//			if(callResult.equals("S")){
//				Log.i(TAG, "S to update GCM ID to server");
//			}else{
//				Log.i(TAG, "F to update GCM ID to server");
//			}
//			try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	callResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+callResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 

			return callResult;
		}
	}
    
	
	/**
	 * RestGetMemberStoreList
	 * 가맹점 목록을 가져온다
	 * @return  String
	 */
	public String RestGetMemberStoreList(CheckMileageMerchants checkMileageMerchantsParam){
		String resultStr = "";
		
		checkMileageMerchants = checkMileageMerchantsParam;
		
		service = Executors.newFixedThreadPool(1);        
        task    = service.submit(new GetMemberStoreList());
        try 
        {
        	resultStr = task.get();
            Log.d(TAG,"RestGetMemberStoreList result : "+resultStr);
        }catch(Exception ex)  {
            ex.printStackTrace();
        }
        service.shutdownNow();
        return resultStr;
	}
	//   가맹점 목록을 가져온다
	class GetMemberStoreList implements Callable<String>{
		@Override
		public String call() throws Exception {
//			nowDate = getNowDate();
			
			// 파라미터 셋팅
			controllerName = "checkMileageMerchantController";
			methodName = "selectSearchMerchantList";
			fullUrl = serverName+"/"+controllerName+"/"+methodName;
			JSONObject obj = new JSONObject();
			try{		 
				obj.put("activateYn", "Y");
				obj.put("businessKind03", checkMileageMerchants.getBusinessKind03());		// 업종					// 고유 번호 얻으려면, 내 아이디도 필요...
				obj.put("checkMileageId", checkMileageMerchants.getCheckMileageId());			// 내 아이디
				obj.put("companyName", checkMileageMerchants.getCompanyName());			// 내 아이디
			}catch(Exception e){
				e.printStackTrace();
			}
//			inputJson = obj.toString();
			inputJson = "{\"checkMileageMerchant\":" + obj.toString() + "}";
			
			// 서버호출 메소드 실행
			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
//			if(callResult.equals("S")){
//				Log.i(TAG, "S to update GCM ID to server");
//			}else{
//				Log.i(TAG, "F to update GCM ID to server");
//			}
//			try {
//			jsonObject = new JSONObject(tempstr);
//			String jstring2 = jsonObject.getString("checkMileageCertification").toString(); 
//	    	JSONObject jsonObject2 = new JSONObject(jstring2);
//	    	callResult = jsonObject2.getString("result").toString(); 
//	    	Log.d(TAG,"certiResult:"+callResult);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} 

			return callResult;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    
	
    
    
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// 공용 서버 호출 메소드
	public String callServerMethod(String fullUrl, String inputJson){
		// 현시각
		nowDate = getNowDate();
		try{
			postUrl2 = new URL(fullUrl);
			connection2 = (HttpURLConnection) postUrl2.openConnection();
			connection2.setConnectTimeout(CommonUtils.serverConnectTimeOut);
			connection2.setDoOutput(true);
			connection2.setInstanceFollowRedirects(false);
			connection2.setRequestMethod("POST");
			connection2.setRequestProperty("Content-Type", "application/json");
			Thread.sleep(200);
			OutputStream os2 = connection2.getOutputStream();
			os2.write(inputJson.getBytes("UTF-8"));
			os2.flush();
			responseCode = connection2.getResponseCode();
			InputStream in =  connection2.getInputStream();
			if(responseCode==200 || responseCode==204){
				// 결과를 처리.
				BufferedReader reader = new BufferedReader(new InputStreamReader(in), 8192);
				StringBuilder builder = new StringBuilder();
				String line =null;
				try {
					while((line=reader.readLine())!=null){
						builder.append(line).append("\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				tempstr = builder.toString();
				callResult = "S";
			}else{
				Log.d(TAG,"Fail["+responseCode+"]");
				callResult="";
			}
		}catch(Exception e){ 
			e.printStackTrace();
			callResult="";
			initNetwork();
		}
		isRunning = 0;
		return callResult;
	}
    
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	// 처리 결과 반환 메소드
	

	
	// tempstr
	public String getTempstr(){
		return tempstr;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////
// 유틸
	
	
	// 현시각
	public String getNowDate(){
		today = new Date();
		nowDate = sf.format(today);
		return nowDate;
	}
	// 넷웍 초기화
	public void initNetwork(){
		if(connection2!=null){
			connection2.disconnect();
			connection2 = null;
		}
		postUrl2 = null;
	}
}

