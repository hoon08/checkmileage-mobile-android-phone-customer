package kr.co.bettersoft.checkmileage.domain;
/**
 * 로케일 정보를 저장하는 도메인 클래스이다.
 * @author blue
 *
 */
public class Locales {
	private String countryCode;		// 국가 코드
	private String languageCode;	// 언어코드
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
