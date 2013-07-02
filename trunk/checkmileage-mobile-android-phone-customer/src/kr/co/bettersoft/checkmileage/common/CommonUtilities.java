/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.bettersoft.checkmileage.common;
/**
 * CommonUtilities
 * GCM 용. SENDER_ID , DISPLAY_MESSAGE_ACTION 사용
 */
import android.content.Context;
import android.content.Intent;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

//	/**
//	 * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
//	 */
//	//    static final String SERVER_URL = "http://192.168.10.112:8080/GCMServerTest1/";		// blue.
//	//    static final String SERVER_URL = "http://chattingday.onemobileservice.com/chattingDayGatheringMessageController/testGCM/";
//
//	/**
//	 * Google API project id registered to use GCM.
//	 */
//	//    static final String SENDER_ID = "568602772620";				// yes. blue.
//	public static final String SENDER_ID = "944691534021";				// yes. server / gcm register 할때 사용
//
//	/**
//	 * Tag used on log messages.
//	 */
//	public static final String TAG = "GCMCommonUtilities";
//
//	/**
//	 * Intent used to display a message in the screen.
//	 */
//	public static final String DISPLAY_MESSAGE_ACTION =
//		"co.kr.bettersoft.checkmileage_mobile_android_phone_customer.DISPLAY_MESSAGE";
//
//	/**
//	 * Intent's extra that contains the message to be displayed.
//	 */
//	public static final String EXTRA_MESSAGE = "message";
//
//	/**
//	 * Notifies UI to display a message.
//	 * <p>
//	 * This method is defined in the common helper because it's used both by
//	 * the UI and the background service.
//	 *
//	 * @param context application's context.
//	 * @param message message to be displayed.
//	 */
//	public static void displayMessage(Context context, String message) {
//		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
//		intent.putExtra(EXTRA_MESSAGE, message);
//		context.sendBroadcast(intent);
//	}
}
