package com.kr.bettersoft.domain;

// �� ���ϸ��� ��� ���� �뵵. ������ ������ ������ ������ �߿��� ������ ID�� ���� 2�� �˻��� �Ͽ� �����´�.

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
		this.idCheckMileageMileages = idCheckMileageMileages;		// Ű �� --> �α׺��� �ʿ�
		this.mileage = mileage;										// ���ϸ���
		this.modifyDate = modifyDate;								// ������
		this.checkMileageMembersCheckMileageID = checkMileageMembersCheckMileageID;	// �� ���̵�
		this.checkMileageMerchantsMerchantID = checkMileageMerchantsMerchantID;		// ������ ���̵�.
	}
}
