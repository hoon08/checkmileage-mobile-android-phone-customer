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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import kr.co.bettersoft.checkmileage.domain.CheckMileageLogs;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMemberSettings;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMembers;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMerchants;
import kr.co.bettersoft.checkmileage.domain.CheckMileageMileage;
import kr.co.bettersoft.checkmileage.domain.CheckMileagePushEvent;
import kr.co.bettersoft.checkmileage.domain.Locales;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class CheckMileageCustomerRest {
	String TAG = "CheckMileageCustomerRest";
	
 // 서버 통신
	int responseCode = 0;
	String controllerName ="";
	String methodName ="";
	String serverName = CommonConstant.serverNames;
	

	int isRunning = 0;		// 통신 도중 중복 호출을 방지하기 위함.
	String fullUrl = "";
	String inputJson = "";
	JSONObject jsonObject;
	JSONObject jsonObj;
	JSONArray jsonArray2;
	JSONObject obj = new JSONObject();
	
	// 현 시각
//	Date today ;
//	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String nowTime = "";	// 현시각
    
	// 결과
	String callResult = "";
	static String tempstr = "";
    
	
	// 고객 정보 저장
	static CheckMileageMembers checkMileageMembers = new CheckMileageMembers();
	// 고객 설정 저장
	static CheckMileageMemberSettings checkMileageMemberSettings = new CheckMileageMemberSettings();
	// 마일리지 정보 저장
	CheckMileageMileage checkMileageMileage = new CheckMileageMileage();
	// 로그
	CheckMileageLogs checkMileageLogs = new CheckMileageLogs();
	// 가맹점 정보 저장
	CheckMileageMerchants checkMileageMerchants = new CheckMileageMerchants();
	// 이벤트 정보 저장
	CheckMileagePushEvent checkMileagePushEvent = new CheckMileagePushEvent();
	// 로케일 정보 저장
	Locales locales = new Locales();
    
	public List<CheckMileageMileage> checkMileageMileageEntries;
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	// 서버 통신
	
	/**
	 * RestCertificationStep_1
	 *   전화번호를 보내서 인증번호를 요청한다.
	 * @return  String
	 */
	public String RestCertificationStep_1(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		nowTime = CommonUtils.getNowDate();
		// 파라미터 셋팅
		controllerName = "checkMileageCertificationController";		
		methodName = "requestCertification";		
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());	
			obj.put("activateYn", "Y");
			obj.put("modifyDate", nowTime);
			obj.put("registerDate", nowTime);
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageCertification\":" + obj.toString() + "}";
		
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
    
	/**
	 * RestCertificationStep_2
	 *   인증2단계 수행.
	 * @return  String
	 */
	public String RestCertificationStep_2(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		controllerName = "checkMileageCertificationController";		// 서버 조회시 컨트롤러 이름
    	methodName = "requestAdmission";							// 서버 조회시 메서드 이름	
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());			// 전번
			obj.put("certificationNumber", checkMileageMembers.getCertiNum());	// 승인번호		
			obj.put("activateYn", "Y");
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageCertification\":" + obj.toString() + "}";
		
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
    
	/**
	 * RestSaveQRtoServer
	 *    서버에 qr 저장
	 * @return  String
	 */
	public String RestSaveQRtoServer(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		nowTime = CommonUtils.getNowDate();
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "registerMember";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
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
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
    
	/**
	 * RestUpdateMyGCMtoServer
	 *     서버에 gcm 업데이트한다
	 * @return  String
	 */
	public String RestUpdateMyGCMtoServer(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		nowTime = CommonUtils.getNowDate();
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "updateRegistrationId";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("activateYn", "Y");
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
			obj.put("registrationId", checkMileageMembers.getRegistrationId());							
			obj.put("modifyDate", nowTime);			
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			Log.i(TAG, "S to update GCM ID to server");
		}else{
			Log.i(TAG, "F to update GCM ID to server");
		}

		return callResult;
	}
    
	/**
	 * RestUpdateLogToServer
	 *   사용자의 위치 정보 및 정보 로깅
	 * @return  String
	 */
	public String RestUpdateLogToServer(CheckMileageLogs checkMileageLogsParam){
		
		checkMileageLogs = checkMileageLogsParam;
		
		nowTime = CommonUtils.getNowDate();
		// 파라미터 셋팅
		controllerName = "checkMileageLogController";
		methodName = "registerLog";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("checkMileageId", checkMileageLogs.getCheckMileageId());	// checkMileageId 	사용자 아이디
			obj.put("merchantId", "");		// merchantId		가맹점 아이디.
			obj.put("viewName", checkMileageLogs.getViewName());		// viewName			출력된 화면.
			obj.put("parameter01", checkMileageLogs.getParameter01());		// parameter01		사용자 전화번호.
			obj.put("parameter02", "");		// parameter02		위도.		// **** 일단 공백 보냄. 나중에 교체
			obj.put("parameter03", "");		// parameter03		경도.		// **** 일단 공백 보냄. 나중에 교체
			obj.put("parameter04", checkMileageLogs.getParameter04());		// parameter04		검색일 경우 검색어.
			obj.put("parameter05", "");		// parameter05		예비용도.
			obj.put("parameter06", "");		// parameter06		예비용도.
			obj.put("parameter07", "");		// parameter07		예비용도.
			obj.put("parameter08", "");		// parameter08		예비용도.
			obj.put("parameter09", "");		// parameter09		예비용도.
			obj.put("parameter10", "");		// parameter10		예비용도.
			obj.put("registerDate", nowTime);		// registerDate		등록 일자.
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageLog\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			Log.d(TAG,"updateLogToServer S");
		}else{
			Log.d(TAG,"updateLogToServer F / "+responseCode);
		}
		return callResult;
	}
	
	
	/**
	 * RestGetMerchantInfo
	 *  가맹점 정보를 가져온다
	 * @return  String
	 */
	public String RestGetMerchantInfo(CheckMileageMerchants checkMileageMerchantsParam){
		
		checkMileageMerchants = checkMileageMerchantsParam;
		
		nowTime = CommonUtils.getNowDate();
		// 파라미터 셋팅
		controllerName = "checkMileageMerchantController";
		methodName = "selectMerchantInformation";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("activateYn", "Y");
			obj.put("merchantId", checkMileageMerchants.getMerchantId());
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMerchant\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
//		if(callResult.equals("S")){
//			Log.i(TAG, "S");
//		}else{
//			Log.i(TAG, "F");
//		}
		return callResult;
	}
	
    
	/**
	 * RestGetBusinessKindList
	 *  검색을 위한 가맹점 업종리스트를 가져온다.
	 * @return  String
	 */
	public String RestGetBusinessKindList(Locales localesParam){
		
		locales = localesParam;
		
		nowTime = CommonUtils.getNowDate();
		
		// 파라미터 셋팅
		controllerName = "checkMileageBusinessKindController";
		methodName = "selectBusinessKindList";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("countryCode", locales.getCountryCode());		// 국가 코드
			obj.put("languageCode", locales.getLanguageCode());			// 언어코드
			obj.put("activateYn", "Y");
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageBusinessKind\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
	
	/**
	 * RestGetMemberStoreList
	 * 가맹점 목록을 가져온다
	 * @return  String
	 */
	public String RestGetMemberStoreList(CheckMileageMerchants checkMileageMerchantsParam){
		
		checkMileageMerchants = checkMileageMerchantsParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMerchantController";
		methodName = "selectSearchMerchantList";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("activateYn", "Y");
			obj.put("businessKind03", checkMileageMerchants.getBusinessKind03());		// 업종					// 고유 번호 얻으려면, 내 아이디도 필요...
			obj.put("checkMileageId", checkMileageMerchants.getCheckMileageId());			// 내 아이디
			obj.put("companyName", checkMileageMerchants.getCompanyName());			// 내 아이디
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMerchant\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}

	
	/**
	 * RestGetQRNumFromServerByPhoneNumber
	 *  사용자 전번을 가지고 서버와 통신하여 QR 코드가 있다면 가져오는 함수 호출한다
	 * @return  String
	 */
	public String RestGetQRNumFromServerByPhoneNumber(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMileageController";
		methodName = "selectMemberExistByPhoneNumber";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());
			obj.put("activateYn", "Y");
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			
			Log.d(TAG,"tempstr:"+tempstr);
			
			// @@@ 있으면 가져오는건데.. 해당 전번으로 고객 수 많으면 곤란하니까 일단 주석 처리 해둠..
//			// 파라미터 셋팅
//			controllerName = "checkMileageMemberController";
//			methodName = "selectMemberInformationByPhoneNumber";
//			fullUrl = serverName+"/"+controllerName+"/"+methodName;
//			obj = new JSONObject();
//			try{		 
//				obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());
//				obj.put("activateYn", "Y");
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
//			
//			// 서버호출 메소드 실행
//			callResult = callServerMethod(fullUrl, inputJson);
			
			// 서버 호출 결과 처리
		}else{
			Log.i(TAG, "F");
		}
		return callResult;
	}
	
	
	/**
	 * RestGetMyEventList
	 *  이벤트 목록 가져온다
	 * @return  String
	 */
	public String RestGetMyEventList(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMerchantMarketingController";
		methodName = "selectMemberMerchantMarketingList";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("activateYn", "Y");
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMerchantMarketing\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
	
	/**
	 * RestCheckAlreadyExistID
	 *  사용자 전번을 가지고 서버와 통신하여 QR 코드가 있는지 확인
	 * @return  String
	 */
	public String RestCheckAlreadyExistID(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberExist";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
			obj.put("activateYn", "Y");		
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
	
	/**
	 * RestGetUserSettingsFromServer
	 *  서버에서 설정 정보 가져온다
	 * @return  String
	 */
	public String RestGetUserSettingsFromServer(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
			obj.put("activateYn", "Y");	
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				// 데이터를 전역 변수 도메인에 저장하고  설정에 저장..
				try{
					checkMileageMemberSettings.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){
					checkMileageMemberSettings.setEmail("");
				}
				try{
					checkMileageMemberSettings.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){
					checkMileageMemberSettings.setBirthday("");
				}
				try{
					checkMileageMemberSettings.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){
					checkMileageMemberSettings.setGender("");
				}
				try{
					checkMileageMemberSettings.setReceive_notification_yn(jsonobj2.getString("receiveNotificationYn"));	
				}catch(Exception e){
					checkMileageMemberSettings.setReceive_notification_yn("");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} 
		}else{
		}
		return callResult;
	}
	
	
	/**
	 * RestGetUserInfo
	 *  서버로부터 개인 정보를 받아와서 도메인에 저장한다
	 * @return  String
	 */
	public String RestGetUserInfo(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "selectMemberInformation";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		try{		 
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());			  
			obj.put("activateYn", "Y");	
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				try{  // 아이디
					//					Log.i(TAG, "checkMileageId:::"+jsonobj2.getString("checkMileageId"));
					checkMileageMembers.setCheckMileageId(jsonobj2.getString("checkMileageId"));				
				}catch(Exception e){ checkMileageMembers.setCheckMileageId(""); }
				try{  // 비번
					checkMileageMembers.setPassword(jsonobj2.getString("password"));				
				}catch(Exception e){ checkMileageMembers.setPassword(""); }
				try{  //전번 
					checkMileageMembers.setPhoneNumber(jsonobj2.getString("phoneNumber"));				
				}catch(Exception e){ checkMileageMembers.setPhoneNumber(""); }
				try{	// 멜
					checkMileageMembers.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){ checkMileageMembers.setEmail(""); }
				try{	// 생일
					checkMileageMembers.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){ checkMileageMembers.setBirthday(""); }
				try{	// 성별
					checkMileageMembers.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){ checkMileageMembers.setGender(""); }
				try{	// 위도
					checkMileageMembers.setLatitude(jsonobj2.getString("latitude"));				
				}catch(Exception e){ checkMileageMembers.setLatitude(""); }
				try{	// 경도
					checkMileageMembers.setLongitude(jsonobj2.getString("longitude"));				
				}catch(Exception e){ checkMileageMembers.setLongitude(""); }
				try{	// 타입
					checkMileageMembers.setDeviceType(jsonobj2.getString("deviceType"));				
				}catch(Exception e){ checkMileageMembers.setDeviceType(""); }
				try{	// 등록ID
					checkMileageMembers.setRegistrationId(jsonobj2.getString("registrationId"));				
				}catch(Exception e){ checkMileageMembers.setRegistrationId(""); }
				try{	// 액티베이트
					checkMileageMembers.setActivateYn(jsonobj2.getString("activateYn"));				
				}catch(Exception e){ checkMileageMembers.setActivateYn(""); }
				try{	// 변경일
					checkMileageMembers.setModifyDate(jsonobj2.getString("modifyDate"));				
				}catch(Exception e){ checkMileageMembers.setModifyDate(""); }
				try{	// 알림 수신 여부 
					checkMileageMembers.setReceiveNotificationYn(jsonobj2.getString("receiveNotificationYn"));				
				}catch(Exception e){ checkMileageMembers.setReceiveNotificationYn(""); }
				try{	// 국가 코드
					checkMileageMembers.setCountryCode(jsonobj2.getString("countryCode"));				
				}catch(Exception e){ checkMileageMembers.setCountryCode(checkMileageMembersParam.getCountryCode()); }
				try{	// 언어 코드
					checkMileageMembers.setLanguageCode(jsonobj2.getString("languageCode"));				
				}catch(Exception e){ checkMileageMembers.setLanguageCode(checkMileageMembersParam.getLanguageCode()); }
				// 그 외 activateYn 는 수동 조작. 이시점에 저장 완료.
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
		}
		return callResult;
	}
	
	
	/**
	 * RestUpdateToServer
	 *  서버로 변경된 설정 등을 업데이트 한다
	 * @return  String
	 */
	public String RestUpdateToServer(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "updateMemberInformation";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		nowTime = CommonUtils.getNowDate();
		try{		 
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());
			obj.put("password", checkMileageMembers.getPassword());
			obj.put("phoneNumber", checkMileageMembers.getPhoneNumber());
			obj.put("email", checkMileageMembers.getEmail());
			obj.put("birthday", checkMileageMembers.getBirthday());
			obj.put("gender", checkMileageMembers.getGender());
			obj.put("latitude", checkMileageMembers.getLatitude());
			obj.put("longitude", checkMileageMembers.getLongitude());
			obj.put("deviceType", checkMileageMembers.getDeviceType());
			obj.put("registrationId", checkMileageMembers.getRegistrationId());
			obj.put("activateYn", checkMileageMembers.getActivateYn());
			obj.put("countryCode", checkMileageMembers.getCountryCode());
			obj.put("languageCode", checkMileageMembers.getLanguageCode());
			obj.put("receiveNotificationYn", checkMileageMembers.getReceiveNotificationYn());
			obj.put("modifyDate", nowTime);		// 지금 시간으로.
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		if(callResult.equals("S")){
			try {
				jsonObject = new JSONObject(tempstr);
				JSONObject jsonobj2 = jsonObject.getJSONObject("checkMileageMember");
				try{  // 아이디
					checkMileageMembers.setCheckMileageId(jsonobj2.getString("checkMileageId"));				
				}catch(Exception e){ checkMileageMembers.setCheckMileageId(""); }
				try{  // 비번
					checkMileageMembers.setPassword(jsonobj2.getString("password"));				
				}catch(Exception e){ checkMileageMembers.setPassword(""); }
				try{  //전번 
					checkMileageMembers.setPhoneNumber(jsonobj2.getString("phoneNumber"));				
				}catch(Exception e){ checkMileageMembers.setPhoneNumber(""); }
				try{	// 멜
					checkMileageMembers.setEmail(jsonobj2.getString("email"));				
				}catch(Exception e){ checkMileageMembers.setEmail(""); }
				try{	// 생일
					checkMileageMembers.setBirthday(jsonobj2.getString("birthday"));				
				}catch(Exception e){ checkMileageMembers.setBirthday(""); }
				try{	// 성별
					checkMileageMembers.setGender(jsonobj2.getString("gender"));				
				}catch(Exception e){ checkMileageMembers.setGender(""); }
				try{	// 위도
					checkMileageMembers.setLatitude(jsonobj2.getString("latitude"));				
				}catch(Exception e){ checkMileageMembers.setLatitude(""); }
				try{	// 경도
					checkMileageMembers.setLongitude(jsonobj2.getString("longitude"));				
				}catch(Exception e){ checkMileageMembers.setLongitude(""); }
				try{	// 타입
					checkMileageMembers.setDeviceType(jsonobj2.getString("deviceType"));				
				}catch(Exception e){ checkMileageMembers.setDeviceType(""); }
				try{	// 등록ID
					checkMileageMembers.setRegistrationId(jsonobj2.getString("registrationId"));				
				}catch(Exception e){ checkMileageMembers.setRegistrationId(""); }
				try{	// 액티베이트
					checkMileageMembers.setActivateYn(jsonobj2.getString("activateYn"));				
				}catch(Exception e){ checkMileageMembers.setActivateYn(""); }
				try{	// 변경일
					checkMileageMembers.setModifyDate(jsonobj2.getString("modifyDate"));				
				}catch(Exception e){ checkMileageMembers.setModifyDate(""); }
				try{	// 알림 수신 여부 
					checkMileageMembers.setReceiveNotificationYn(jsonobj2.getString("receiveNotificationYn"));				
				}catch(Exception e){ checkMileageMembers.setReceiveNotificationYn(""); }
				try{	// 국가 코드
					checkMileageMembers.setCountryCode(jsonobj2.getString("countryCode"));				
				}catch(Exception e){ checkMileageMembers.setCountryCode(checkMileageMembersParam.getCountryCode()); }
				try{	// 언어 코드
					checkMileageMembers.setLanguageCode(jsonobj2.getString("languageCode"));				
				}catch(Exception e){ checkMileageMembers.setLanguageCode(checkMileageMembersParam.getLanguageCode()); }
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
		}
		return callResult;
	}
	
	
	
	/**
	 * RestUpdateGCMToServer
	 *  서버로 변경된 설정 등을 업데이트 한다
	 * @return  String
	 */
	public String RestUpdateGCMToServer(CheckMileageMembers checkMileageMembersParam){
		
		checkMileageMembers = checkMileageMembersParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMemberController";
		methodName = "updateReceiveNotification";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		nowTime = CommonUtils.getNowDate();
		try{		 
			obj.put("checkMileageId", checkMileageMembers.getCheckMileageId());
			obj.put("receiveNotificationYn", checkMileageMembers.getReceiveNotificationYn());						// 정해서 넣어.
			obj.put("activateYn", "Y");
			obj.put("modifyDate", nowTime);		// 지금 시간으로.

		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMember\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
	
	/**
	 * RestGetMyMileageList
	 *  마일리지 목록 가져온다
	 * @return  String
	 */
	public String RestGetMyMileageList(CheckMileageMileage checkMileageMileageParam){
		
		checkMileageMileage = checkMileageMileageParam;
		
		// 파라미터 셋팅
		controllerName = "checkMileageMileageController";
		methodName = "selectMemberMerchantMileageList";
		fullUrl = serverName+"/"+controllerName+"/"+methodName;
		obj = new JSONObject();
		nowTime = CommonUtils.getNowDate();
		try{		 
			obj.put("activateYn", "Y");
			obj.put("checkMileageMembersCheckMileageId", checkMileageMileage.getCheckMileageMembersCheckMileageID());
		}catch(Exception e){
			e.printStackTrace();
		}
		inputJson = "{\"checkMileageMileage\":" + obj.toString() + "}";
		
		// 서버호출 메소드 실행
		callResult = callServerMethod(fullUrl, inputJson);
		
		// 서버 호출 결과 처리
		return callResult;
	}
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// 공용 서버 호출 메소드
	public String callServerMethod(String fullUrl, String inputJson){
		try{
			URL postUrl2 = new URL(fullUrl);
			HttpURLConnection connection2 = (HttpURLConnection) postUrl2.openConnection();
			connection2.setConnectTimeout(CommonConstant.serverConnectTimeOut);
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
//			initNetwork();
		}
		isRunning = 0;
		return callResult;
	}
    
    
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	// 처리 결과 반환 메소드
	
	// checkMileageMembers
	public static CheckMileageMembers getCheckMileageMembers(){
		return checkMileageMembers;
	}
	
	// checkMileageMemberSettings
	public static CheckMileageMemberSettings getCheckMileageMemberSettings(){
		return checkMileageMemberSettings;
	}
	
	// tempstr
	public String getTempstr(){
		return tempstr;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////////
// 유틸
	
//	// 넷웍 초기화
//	public void initNetwork(){
//		if(connection2!=null){
//			connection2.disconnect();
//			connection2 = null;
//		}
//		postUrl2 = null;
//	}
}

