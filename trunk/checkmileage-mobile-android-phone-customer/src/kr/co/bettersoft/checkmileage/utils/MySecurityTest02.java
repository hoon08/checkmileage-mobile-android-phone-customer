package kr.co.bettersoft.checkmileage.utils;

/**
 * 
 */
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
/**
 * AES 256 기반의 암/복호화 클래스이다.
 *  
 * @author John_Kim(cale9797@gmail.com)
 *
 * 2013. 1. 24.
 */
public class MySecurityTest02 {

	/**
	 * @param args
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidKeyException 
	 */
	public static void main(String[] args) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		// TODO Auto-generated method stub

		// 32 자리. Client 와 Server 모두 동일해야 함. 대칭키 방식.
//		String key = "abcdefghijklmnopqrstuvwxyz123456";
		String key = "Created_by_JohnKim_in_Bettersoft";
		
		String plainText;
		String encodeText;
		String decodeText;
		// Encrypt
		plainText  = "{\"phoneNumber\":\"01085858025\",\"checkMileageId\":\"cale9797\",\"password\":\"1234\"}";
		
		System.out.println("plainText     : " + plainText);
		
		encodeText = AES256Cipher.AES_Encode(plainText, key);		
		System.out.println("AES256_Encode : "+encodeText);
		 
		// Decrypt
		decodeText = AES256Cipher.AES_Decode(encodeText, key);
		System.out.println("AES256_Decode : "+decodeText);
	}

}
