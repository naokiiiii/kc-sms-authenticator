package com.demo.keycloak.authenticator.servicecall;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * [Twilio SMS Verrify] APIコール結果格納クラス
 */
public class SMSVerifyResult {

	/* Twilio API ステータス */
	private final String status;
	/* Twilio API Valid確認状態 */
	private final Boolean valid;
	/* Twilio API トークン作成日時 */
	private final String dateCreated;
	/* Twilio API トークン更新日時 */
	private final String dateUpdated;
	/* Twilio API JSONデータ全体 */
	private final String jsonString;

	/**
	 * コンストラクタ
	 */
	SMSVerifyResult(String status, Boolean valid, String dateCreated, String dateUpdated, String jsonString){
		this.status = status;
		this.valid = valid;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.jsonString = jsonString;
	}

	/**
	 * コンストラクタ
	 */
	SMSVerifyResult(String status){
		this.status = status;
		this.valid = false;
		this.dateCreated = null;
		this.dateUpdated = null;
		this.jsonString = null;
	}

	/**
	 * JSON -> オブジェクト変換
	 */
	public static SMSVerifyResult fromJson(final String json) {
		try {
			JSONObject result = new JSONObject(json);
			return new SMSVerifyResult(result.getString("status")
							, result.getBoolean("valid")
							, result.getString("date_created")
							, result.getString("date_updated")
							, json);
		} catch (final JSONException e) {
			return new SMSVerifyResult(e.getMessage());
		}
	}

	/**
	 * ステータス取得
	 */
	public final String getStatus() {
		return this.status;
	}

	/**
	 * Valid確認状態取得
	 */
	public final Boolean getValid() {
		return this.valid;
	}

	/**
	 * トークン作成日時取得
	 */
	public String getDateCreated() {
		return dateCreated;
	}

	/**
	 * トークン更新日時取得
	 */
	public String getDateUpdated() {
		return dateUpdated;
	}

	/**
	 *  Json文字列取得
	 */
	public final String getJsonString() {
		return this.jsonString;
	}

}