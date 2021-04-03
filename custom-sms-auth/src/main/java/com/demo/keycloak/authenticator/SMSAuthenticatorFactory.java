package com.demo.keycloak.authenticator;

import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * [Twilio SMS Verrify] AuthenticatorFactoryクラス
 */
public class SMSAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

	/* Logger */
	private static final Logger logger = Logger.getLogger(SMSAuthenticatorFactory.class.getPackage().getName());

	/* シングルトンモデルのためのクラス */
	private static final SMSAuthenticator SINGLETON = new SMSAuthenticator();

	/* プロバイダーID */
	public static final String PROVIDER_ID = "sms-authenticator-with-twilio";

	/* 管理コンソールから設定可能とするExecution情報 */
	private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED,
			AuthenticationExecutionModel.Requirement.DISABLED
	};

	/* 管理コンソールから設定可能とするコンフィグレーション情報 */
	private static final List<ProviderConfigProperty> configProperties;
	static {
		configProperties = ProviderConfigurationBuilder
				// SERVICE SID
				.create()
				.property()
				.name(SMSAuthContstants.CONFIG_SMS_SEVICE_SID)
				.label("SERVICE SID")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the SERVICE SID to connect to Twilio. ex.VAXXXXXXXXXXXXX")
				.add()

				// ACCOUNT SID
				.property()
				.name(SMSAuthContstants.CONFIG_SMS_ACCOUNT_SID)
				.label("ACCOUNT SID")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the ACCOUNT SID to connect to Twilio.　ex.SKXXXXXXXXXXXXX")
				.add()

				// AUTH TOKEN
				.property()
				.name(SMSAuthContstants.CONFIG_SMS_AUTH_TOKEN)
				.label("AUTH TOKEN")
				.type(ProviderConfigProperty.STRING_TYPE)
				.defaultValue("")
				.helpText("Set the AUTH TOKEN to connect to Twilio.　ex.XXXXXXXXXXXXXXX")
				.add()

				.build();
	}

	/*
	 * セッション生成
	 */
	public Authenticator create(KeycloakSession session) {
		return SINGLETON;
	}

	/*
	 * プロバイダーID取得
	 */
	public String getId() {
		return PROVIDER_ID;
	}

	/*
	 * コンフィグ情報
	 */
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	/*
	 * ツールチップ
	 */
	public String getHelpText() {
		return "SMS Authenticate using Twilio.";
	}

	/*
	 * 表示名
	 */
	public String getDisplayType() {
		return "Twilio SMS Authentication";
	}

	/*
	 * リファレンスカテゴリ
	 */
	public String getReferenceCategory() {
		return "sms-auth-code";
	}

	/*
	 * 設定可否
	 */
	public boolean isConfigurable() {
		return true;
	}

	/*
	 * デフォルトの選択内容
	 */
	public Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES == null ? null : (Requirement[]) REQUIREMENT_CHOICES.clone();
	}

	/*
	 * リファレンスカテゴリ取得
	 */
	public boolean isUserSetupAllowed() {
		return true;
	}

	/*
	 * このクラスの初期化時に動作するフックメソッド
	 */
	public void init(Scope scope) {
		logger.debug("Method [init]");
	}

	/*
	 * POSTを受けた時に動作するフックメソッド
	 */
	public void postInit(KeycloakSessionFactory factory) {
		logger.debug("Method [postInit]");
	}

	/*
	 * このクラスが終了する時に動作するフックメソッド
	 */
	public void close() {
		logger.debug("<<<<<<<<<<<<<<< SMSAuthenticatorFactory close");
	}

}
