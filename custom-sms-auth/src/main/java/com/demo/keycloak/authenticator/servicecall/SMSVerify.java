package com.demo.keycloak.authenticator.servicecall;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.logging.Logger;

/**
 * [Twilio SMS Verrify] SMSサービスコール制御クラス
 */
public class SMSVerify {

	/* ロガー */
	private static final Logger logger = Logger.getLogger(SMSVerify.class.getPackage().getName());

	/* Twilio API URL ベース */
	public static final String DEFAULT_API_URI = "https://verify.twilio.com/v2/Services/";
	/* Twilio API URL SMS送信 */
	public static final String PHONE_VERIFICATION_SEND_API_PATH = "/Verifications";
	/* Twilio API URL Verifyチェック */
	public static final String PHONE_VERIFICATION_CHECK_API_PATH = "/VerificationCheck";
	/* POST メソッド */
	public static final String METHOD_POST = "POST";
	/* 電話番号 国 */
	public static final String COUTRY_CODE = "+81";  // 日本
	/* HTTP Client コネクションタイムアウト */
	private static final int CONNECTION_TIMEOUT = 10000;
	/* HTTP Client コネクションタイムアウト */
	private static final int SOCKET_TIMEOUT = 30500;
	/* Twilio Service SID */
	private final String serviceSid;
	/* Twilio Account SID */
	private final String accountSid;
	/* Twilio authToken */
	private final String authToken;

	/**
	 * コンストラクタ
	 */
	public SMSVerify(String serviceSid, String accountSid, String authToken) {
		this.serviceSid = serviceSid;
		this.accountSid = accountSid;
		this.authToken = authToken;
	}

	/**
	 * SMS送信
	 */
	public boolean sendSMS(String telNum) {

		logger.infov("sendSMS start : {0}", telNum);

		RequestBuilder param = RequestBuilder.create(METHOD_POST)
			.setUri(DEFAULT_API_URI + serviceSid + PHONE_VERIFICATION_SEND_API_PATH)
			.setVersion(HttpVersion.HTTP_1_1)
			.setCharset(StandardCharsets.UTF_8);
		param.addHeader(HttpHeaders.ACCEPT, "application/json");
		param.addHeader(HttpHeaders.ACCEPT_ENCODING, "utf-8");
		param.addHeader(HttpHeaders.AUTHORIZATION, getAuthString(accountSid, authToken));
		param.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		param.addParameter("To", COUTRY_CODE + telNum);
		param.addParameter("Channel", "sms");

		HttpResponse responce = request(param);
		logger.infov("verifySMS Status Code : {0}", responce.getStatusLine().getStatusCode());

		SMSVerifyResult result = SMSVerifyResult.fromJson(getString(responce));
		if (responce.getStatusLine().getStatusCode() >= 200
				&& responce.getStatusLine().getStatusCode() <= 299 ) {
			logger.infov("sendSMS result : {0}", result.getJsonString());
			logger.infov("sendSMS end : {0}", telNum);
			return true;
		} else {
			logger.infov("sendSMS end : {0}", telNum);
			return false;
		}
	}

	/**
	 * Verifyチェック
	 */
	public boolean verifySMS(String telNum, String code) {

		logger.infov("verifySMS start : {0}", telNum);

		RequestBuilder param = RequestBuilder.create(METHOD_POST)
			.setUri(DEFAULT_API_URI + serviceSid + PHONE_VERIFICATION_CHECK_API_PATH)
			.setVersion(HttpVersion.HTTP_1_1)
			.setCharset(StandardCharsets.UTF_8);
		param.addHeader(HttpHeaders.ACCEPT, "application/json");
		param.addHeader(HttpHeaders.ACCEPT_ENCODING, "utf-8");
		param.addHeader(HttpHeaders.AUTHORIZATION, getAuthString(accountSid, authToken));
		param.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
		param.addParameter("To", COUTRY_CODE + telNum);
		param.addParameter("Code", code);

		HttpResponse responce = request(param);
		logger.infov("verifySMS Status Code : {0}", responce.getStatusLine().getStatusCode());

		SMSVerifyResult result = SMSVerifyResult.fromJson(getString(responce));
		if (responce.getStatusLine().getStatusCode() >= 200
				&& responce.getStatusLine().getStatusCode() <= 299 ) {
			logger.infov("verifySMS result : {0}", result.getJsonString());
			if (result.getStatus().equals("approved") && result.getValid() == true ) {
				logger.infov("verifySMS end : {0}", telNum);
				return true;
			} else {
				logger.infov("verifySMS end : {0}", telNum);
				return false;
			}
		} else {
			logger.infov("verifySMS end : {0}", telNum);
			return false;
		}
	}

	/**
	 *  HTTP リクエスト送信
	 */
	private HttpResponse request(final RequestBuilder param) {

		HttpResponse response = null;
		try {
			HttpClient client = buildHttpClient();
			response = client.execute(param.build());
			return response;
		} catch (IOException e) {
			logger.errorv("HTTP Request send error:{0}", e.getMessage());
			return null;
		}
	}

	/*
	 * HTTP クライアント作成
	 */
	private HttpClient buildHttpClient() {
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(CONNECTION_TIMEOUT)
			.setSocketTimeout(SOCKET_TIMEOUT)
			.build();

		org.apache.http.impl.client.HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		clientBuilder.useSystemProperties();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setDefaultMaxPerRoute(10);
		connectionManager.setMaxTotal(10*2);
		clientBuilder
			.setConnectionManager(connectionManager)
			.setDefaultRequestConfig(config);

		return clientBuilder.build();
	}

	/*
	 *  認証情報エンコード
	 */
	private String getAuthString(String accountSid, String authToken) {
		String credentials = accountSid + ":" + authToken;
		try {
			String encoded = DatatypeConverter.printBase64Binary(credentials.getBytes("ascii"));
			return "Basic " + encoded;
		} catch (final UnsupportedEncodingException e) {
			logger.errorv("TelephoneNumberVerify UnsupportedEncodingException:{0}", e.getMessage());
			return null;
		}
	}

	/*
	 * レスポンスデータ処理（json文字列に変換）
	 */
	private String getString(HttpResponse response) {
		try {
			HttpEntity entity = response.getEntity();
			InputStream stream = null;
			if (entity != null) {
				stream = new BufferedHttpEntity(entity).getContent();
			}
			String content = getContent(stream);
			return content;
		} catch (final IOException e) {
			logger.errorv("ExecuteVerify.getString IOException={0}",e.getMessage());
			return null;
		}
	}

	/**
	 * Stream入力を文字配列に変換
	 */
	private String getContent(InputStream stream) {
		if (stream != null) {
			Scanner scanner = new Scanner(stream, "UTF-8").useDelimiter("\\A");
			if (!scanner.hasNext()) {
				scanner.close();
				return "";
			}
			String data = scanner.next();
			scanner.close();
			return data;
		}
		return "";
	}
}
