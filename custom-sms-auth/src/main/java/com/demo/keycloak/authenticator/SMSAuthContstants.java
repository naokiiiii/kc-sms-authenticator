package com.demo.keycloak.authenticator;

/**
 * [Twilio SMS Verrify] 定数クラス
 */
public class SMSAuthContstants {

	/* 電話番号 */
	public static final String ATTR_PHONE_NUMBER = "phoneNumber";

	/* Twilio サービスSID */
	public static final String CONFIG_SMS_SEVICE_SID = "verifySMS.service-sid";

	/* Twilio アカウントSID */
	public static final String CONFIG_SMS_ACCOUNT_SID = "verifySMS.account-sid";

	/* Twilio API認証トークン */
	public static final String CONFIG_SMS_AUTH_TOKEN = "verifySMS.auth-token";

}
