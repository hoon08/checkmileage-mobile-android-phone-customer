package kr.co.bettersoft.checkmileage.domain;

import android.graphics.Bitmap;

public class CheckMileagePushEvent {
//	private String idCheckMileageMerchantMarketings;
	// 49
	private String subject;					// �̺�Ʈ ����
	// ������ �Ľ�Ÿ �Դϴ�
	private String content;					// �̺�Ʈ �۱�
	//�˸� ����\r\n���پ���\r\n�׽�Ʈ �ϰڽ��ϴ�.
	private String imageFileUrl;			// �̺�Ʈ ���� �̹��� �ּ�
	private Bitmap imageFile;				// �̺�Ʈ ���� �̹���
	private String imageFileStr;			// �̺�Ʈ ���� �̹��� -> ���ڿ�ȭ
	// asdfasdfsfsdfsdfsdfsdf
//	private String activateYn;
	//Y
	private String modifyDate;				// �ۼ��� 
	//2012-11-08 17:43:58
//	private String registerDate;
	//2012-11-08 17:43:58
	private String companyName;				// ������ �̸� 
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
	//������ �Ľ�Ÿ
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
