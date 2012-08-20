package com.kr.bettersoft.domain;
/*
 * 가맹점 이용 내역 조회할때 사용하는 도메인 클래스.
 * 
 * 마일리지 테이블에 삽입, 수정 등이 일어나는데 그 가맹점,아이디 - 별로 키 값이 존재.
 *  
 *  여기서는 그 키 값을 통해 해당 가맹점-아이디 간 발생한 마일리지 내역을 조회해서 리스트로 담아온다.
 */
public class CheckMileageMemberMileageLogs {

	private String checkMileageId;		// 내 아디
	private String merchantId;			// 가맹점 아디
	private String content;				// 내용 먹은거? 이용한것.
	private String mileage;				// 적립,사용한 마일리지
//	private String activateYn;			// 활성 비활성
	private String modifyDate;			// 수정일  - 사용o
	private String registerDate;		// 등록일 - 사용x
	private String checkMileageMileagesIdCheckMileageMileages;		// 키값. 중요. 키값으로 조회함.
	public String getCheckMileageId() {
		return checkMileageId;
	}
	public void setCheckMileageId(String checkMileageId) {
		this.checkMileageId = checkMileageId;
	}
	public String getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getMileage() {
		return mileage;
	}
	public void setMileage(String mileage) {
		this.mileage = mileage;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}
	public String getCheckMileageMileagesIdCheckMileageMileages() {
		return checkMileageMileagesIdCheckMileageMileages;
	}
	public void setCheckMileageMileagesIdCheckMileageMileages(String checkMileageMileagesIdCheckMileageMileages) {
		this.checkMileageMileagesIdCheckMileageMileages = checkMileageMileagesIdCheckMileageMileages;
	}
	public CheckMileageMemberMileageLogs(String checkMileageMileagesIdCheckMileageMileages, String content, String mileage, String modifyDate){
		this.checkMileageMileagesIdCheckMileageMileages = checkMileageMileagesIdCheckMileageMileages;
		this.content = content;
		this.mileage = mileage;
		this.modifyDate = modifyDate;
	}
}
