package com.demo.keycloak.authenticator;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import com.demo.keycloak.authenticator.servicecall.SMSVerify;

/**
 * [Twilio SMS Verrify] Authenticatorクラス
 */
public class SMSAuthenticator implements Authenticator {

	/* Logger */
	private static final Logger logger = Logger.getLogger(SMSAuthenticator.class.getPackage().getName());

	/*
	 *  認証処理(認証フローで設定された順序で行われる最初の認証処理）
	 */
	public void authenticate(AuthenticationFlowContext context) {
		logger.debug("Method [authenticate]");

		AuthenticatorConfigModel config = context.getAuthenticatorConfig();

		UserModel user = context.getUser();
		String phoneNumber = getPhoneNumber(user);
		logger.debugv("phoneNumber : {0}", phoneNumber);

		if (phoneNumber != null) {
			// VerifySMS送信
			SMSVerify smsVerify = new SMSVerify(
					getConfigString(config, SMSAuthContstants.CONFIG_SMS_SEVICE_SID),
					getConfigString(config, SMSAuthContstants.CONFIG_SMS_ACCOUNT_SID),
					getConfigString(config, SMSAuthContstants.CONFIG_SMS_AUTH_TOKEN));
			if (smsVerify.sendSMS(phoneNumber)) {
				// Verifyコード入力画面を返却する
				Response challenge = context.form().createForm("sms-validation.ftl");
				context.challenge(challenge);

			} else {
				// VerifySMS送信に失敗した場合、エラー画面を返却する
				Response challenge = context.form().addError(new FormMessage("sendSMSCodeErrorMessage"))
						.createForm("sms-validation-error.ftl");
				context.challenge(challenge);
			}
		} else {
			// 電話番号が設定されていない場合、エラー画面を返却する
			Response challenge = context.form().addError(new FormMessage("missingTelNumberMessage"))
					.createForm("sms-validation-error.ftl");
			context.challenge(challenge);
		}

	}

	/*
	 * アクション処理（Verifyコード確認画面の「Sign in」ボタン押下時の処理）
	 */
	public void action(AuthenticationFlowContext context) {
		logger.debug("Method [action]");

		MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
		String enteredCode = inputData.getFirst("smsCode");

		UserModel user = context.getUser();
		String phoneNumber = getPhoneNumber(user);
		logger.debugv("phoneNumber : {0}", phoneNumber);

		// Verifyコード確認
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		SMSVerify smsVerify = new SMSVerify(
				getConfigString(config, SMSAuthContstants.CONFIG_SMS_SEVICE_SID),
				getConfigString(config, SMSAuthContstants.CONFIG_SMS_ACCOUNT_SID),
				getConfigString(config, SMSAuthContstants.CONFIG_SMS_AUTH_TOKEN) );
		if (smsVerify.verifySMS(phoneNumber, enteredCode)) {
			// Verifyコード確認に成功した場合は、認証成功とする
			logger.info("verify code check : OK");
			context.success();
		} else {
			// Verifyコード確認に失敗した場合は、エラー画面を返却する
			Response challenge = context.form()
					.setAttribute("username", context.getAuthenticationSession().getAuthenticatedUser().getUsername())
					.addError(new FormMessage("invalidSMSCodeMessage")).createForm("sms-validation-error.ftl");
			context.challenge(challenge);
		}

	}

	/*
	 * ユーザー必須を決定するフック判定メソッド
	 */
	public boolean requiresUser() {
		logger.debug("Method [requiresUser]");
		return false;
	}

	/*
	 * 設定値の変更を決定するフック判定メソッド
	 */
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		logger.debug("Method [configuredFor]");
		return false;
	}

	/*
	 * RequiredActionsが設定された時に動作するフックメソッド
	 */
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		logger.debug("Method [setRequiredActions]");
	}

	/*
	 * このクラスが終了する時に動作するフックメソッド
	 */
	public void close() {
		logger.debug("<<<<<<<<<<<<<<< SMSAuthenticator close");
	}

	/*
	 * ユーザ情報から電話番号を取得する
	 */
	private String getPhoneNumber(UserModel user) {
		List<String> phoneNumberList = user.getAttribute(SMSAuthContstants.ATTR_PHONE_NUMBER);
		if (phoneNumberList != null && !phoneNumberList.isEmpty()) {
			return phoneNumberList.get(0);
		}
		return null;
	}

	/*
	 * 認証フローから設定された設定値を取得する
	 */
	private String getConfigString(AuthenticatorConfigModel config, String configName) {
		String value = null;
		if (config.getConfig() != null) {
			// Get value
			value = config.getConfig().get(configName);
		}
		return value;
	}
}