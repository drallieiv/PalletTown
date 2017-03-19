package com.pallettown.core.captcha;

import java.io.IOException;
import java.net.CookieManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.errors.CaptchaSolvingException;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class TwoCaptchaService implements CaptchaProvider {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * The URL where the recaptcha is placed. For example:
	 * https://www.google.com/recaptcha/api2/demo
	 */
	private String pageUrl;

	/**
	 * Captcha API key
	 */
	private String apiKey;

	/**
	 * Google Site key
	 */
	private String googleSiteKey = "6LdpuiYTAAAAAL6y9JNUZzJ7cF3F8MQGGKko1bCy";

	// URLs
	private static final String PAGE_URL = "https://club.pokemon.com/us/pokemon-trainer-club/parents/sign-up";
	private static final String CAPTCHA_IN = "http://2captcha.com/in.php";
	private static final String CAPTCHA_OUT = "http://2captcha.com/res.php";

	private OkHttpClient captchaClient;

	public TwoCaptchaService(String apiKey) {
		this.apiKey = apiKey;
		this.captchaClient = new OkHttpClient();
	}

	@Override
	public String getCaptcha() throws CaptchaSolvingException {	
		
		HttpUrl url = HttpUrl.parse(CAPTCHA_IN).newBuilder()
				.addQueryParameter("key", apiKey)
				.addQueryParameter("method", "userrecaptcha")
				.addQueryParameter("googlekey", googleSiteKey)
				.addQueryParameter("pageurl", PAGE_URL)
				.build();

		Request request = new Request.Builder()
				.url(url)
				.build();

		try {
			Response response = captchaClient.newCall(request).execute();
			String body = response.body().string();
			if(body != null && body.equals("ERROR_WRONG_USER_KEY")){
				throw new CaptchaSolvingException("Invalid 2 Captcha API Key");
			}else{
				logger.info("Captcha send to 2 captcha, id: {}", body);
			}
		} catch (IOException e) {
			throw new CaptchaSolvingException(e);
		}

		return null;
	}

}
