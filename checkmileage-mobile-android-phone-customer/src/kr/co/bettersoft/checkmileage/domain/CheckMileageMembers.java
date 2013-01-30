package kr.co.bettersoft.checkmileage.domain;
// 도메인. 


/*
 * 개인 설정 변경 등 할때 도메인에 모두 담아서 도메인 채로 업데이트 실시한다. (설정 에서 사용)
 *      
 *       
 */
public class CheckMileageMembers {
	private String checkMileage;		// 마일리지
	private String merchantId;			// 가맹점 아이디
	private String viewName;			// 
	private String registerDate;		// 등록일
	
	private String checkMileageId;		// 고객 아이디
	private String password;			// 비번 -> 현재 사용 안함
	private String phoneNumber;			// 전번 -> 현재 사용 안함
	private String email;				// 이멜 주소
	private String birthday;			// 생일
	private String gender;				// 성별
	private String latitude;			// 좌표-위도
	private String longitude;			// 좌표-경도
	private String deviceType;			// 기기 타입 (받아서 다시 되돌려줌)
	private String registrationId;		// 등록 ID
	private String activateYn;			
	private String modifyDate;			// 수정일.
	
	private String receiveNotificationYn;	// 알림수신여부
	
	private String countryCode;			// 국가코드
	private String languageCode;		// 언어코드
	
	public String getCheckMileage() {
		return checkMileage;
	}
	public void setCheckMileage(String checkMileage) {
		this.checkMileage = checkMileage;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getViewName() {
		return viewName;
	}
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
	public String getCheckMileageId() {
		return checkMileageId;
	}
	public void setCheckMileageId(String checkMileageId) {
		this.checkMileageId = checkMileageId;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public String getRegistrationId() {
		return registrationId;
	}
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}
	public String getActivateYn() {
		return activateYn;
	}
	public void setActivateYn(String activateYn) {
		this.activateYn = activateYn;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getReceiveNotificationYn() {
		return receiveNotificationYn;
	}
	public void setReceiveNotificationYn(String receiveNotificationYn) {
		this.receiveNotificationYn = receiveNotificationYn;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	
}
