package com.kr.bettersoft.domain;

// 내 마일리지 목록 보기 용도. 가맹점 정보는 가져온 정보들 중에서 가맹점 ID를 통해 2차 검색을 하여 가져온다.

import android.graphics.Bitmap;

public class CheckMileageMileage {
	private String idCheckMileageMileages;
	private String mileage;
	private String activateYN;
	private String modifyDate;
	private String registerDate;
	private String checkMileageMembersCheckMileageID;
	private String checkMileageMerchantsMerchantID;
	
	private String merchantName;
	private String merchantImg;
	private Bitmap merchantImage;
	
	public String getIdCheckMileageMileages() {
		return idCheckMileageMileages;
	}
	public void setIdCheckMileageMileages(String idCheckMileageMileages) {
		this.idCheckMileageMileages = idCheckMileageMileages;
	}
	public String getMileage() {
		return mileage;
	}
	public void setMileage(String mileage) {
		this.mileage = mileage;
	}
	public String getActivateYN() {
		return activateYN;
	}
	public void setActivateYN(String activateYN) {
		this.activateYN = activateYN;
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
	public String getCheckMileageMembersCheckMileageID() {
		return checkMileageMembersCheckMileageID;
	}
	public void setCheckMileageMembersCheckMileageID(
			String checkMileageMembersCheckMileageID) {
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;
	}
	public String getCheckMileageMerchantsMerchantID() {
		return checkMileageMerchantsMerchantID;
	}
	public void setCheckMileageMerchantsMerchantID(
			String checkMileageMerchantsMerchantID) {
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;
	}
	
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getMerchantImg() {
		return merchantImg;
	}
	public void setMerchantImg(String merchantImg) {
		this.merchantImg = merchantImg;
	}
	public Bitmap getMerchantImage() {
		return merchantImage;
	}
	public void setMerchantImage(Bitmap merchantImage) {
		this.merchantImage = merchantImage;
	}
	public CheckMileageMileage(String idCheckMileageMileages, String mileage, String modifyDate, String checkMileageMembersCheckMileageID, String checkMileageMerchantsMerchantID){
		this.idCheckMileageMileages = idCheckMileageMileages;		// 키 값 --> 로그볼때 필요
		this.mileage = mileage;										// 마일리지
		this.modifyDate = modifyDate;								// 수정일
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;	// 내 아이디
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;		// 가맹점 아이디.
	}
}
