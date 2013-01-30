package kr.co.bettersoft.checkmileage.domain;

import android.graphics.Bitmap;

public class CheckMileagePushEvent {
//	private String idCheckMileageMerchantMarketings;
	// 49
	private String subject;					// 이벤트 제목
	// 쉐프의 파스타 입니다
	private String content;					// 이벤트 글귀
	//알림 서비스\r\n한줄씩만\r\n테스트 하겠습니다.
	private String imageFileUrl;			// 이벤트 광고 이미지 주소
	private Bitmap imageFile;				// 이벤트 광고 이미지
	private String imageFileStr;			// 이벤트 광고 이미지 -> 문자열화
	// asdfasdfsfsdfsdfsdfsdf
//	private String activateYn;
	//Y
	private String modifyDate;				// 작성일 
	//2012-11-08 17:43:58
//	private String registerDate;
	//2012-11-08 17:43:58
	private String companyName;				// 가맹점 이름 
	public CheckMileagePushEvent(String subject, String content,
			String imageFileUrl, String modifyDate,
			String companyName, String imageFileStr,
			Bitmap imageFile) {
		this.subject = subject;
		this.content = content;
		this.imageFileUrl = imageFileUrl;
		this.imageFile = imageFile;
		this.imageFileStr = imageFileStr;
		this.modifyDate = modifyDate;
		this.companyName = companyName;
	}
	public CheckMileagePushEvent() { }
	//셰프의 파스타
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getImageFileUrl() {
		return imageFileUrl;
	}
	public void setImageFileUrl(String imageFileUrl) {
		this.imageFileUrl = imageFileUrl;
	}
	public Bitmap getImageFile() {
		return imageFile;
	}
	public void setImageFile(Bitmap imageFile) {
		this.imageFile = imageFile;
	}
	public String getImageFileStr() {
		return imageFileStr;
	}
	public void setImageFileStr(String imageFileStr) {
		this.imageFileStr = imageFileStr;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
}
