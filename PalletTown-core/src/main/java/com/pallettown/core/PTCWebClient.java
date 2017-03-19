package com.pallettown.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.captcha.CaptchaProvider;
import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;
import com.pallettown.core.errors.AccountDuplicateException;
import com.pallettown.core.errors.AccountRateLimitExceededException;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

// Web client that will create a PTC account
public class PTCWebClient {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String url_ptc = "https://club.pokemon.com/us/pokemon-trainer-club";
	private String pathAgeCheck = "/sign-up/";
	private String pathSignup = "/parents/sign-up";

	private boolean dumpError = false;

	private OkHttpClient client;

	private CookieManager cookieManager;

	public PTCWebClient() {

		// Initialize Http Client
		client = new OkHttpClient();
		cookieManager = new CookieManager();
		client.setCookieHandler(cookieManager);
	}

	// Simulate new account creation age check and dump CRSF token
	public String sendAgeCheckAndGrabCrsfToken() throws AccountCreationException {
		try {
			Response response = client.newCall(buildAgeCheckRequest()).execute();

			if (response.isSuccessful()) {
				Document doc = Jsoup.parse(response.body().byteStream(), "UTF-8", "");
				response.body().close();

				logger.debug("Cookies are now  : {}", cookieManager.getCookieStore().getCookies());

				Elements tokenField = doc.select("[name=csrfmiddlewaretoken]");

				if (tokenField.isEmpty()) {
					logger.error("CSRF Token not found");
				} else {
					String crsfToken = tokenField.get(0).val();
					sendAgeCheck(crsfToken);
					return crsfToken;
				}
				logger.error("CSRF Token not found");
			}
		} catch (IOException e) {
			logger.error("Technical error getting CSRF Token", e);
		}
		return null;
	}

	/**
	 * Send the age check request, it will set up a cookie with the dod
	 * NOTE: it could be skipped by manually adding the dod cookie ?
	 */
	public void sendAgeCheck(String crsfToken) throws AccountCreationException {
		try {
			// Create Request
			Request request = this.buildAgeCheckSubmitRequest(crsfToken);

			// Send Request
			logger.debug("Sending age check request");
			Response response = client.newCall(request).execute();

			// Parse Response
			if (response.isSuccessful()) {
				logger.debug("Cookies are now : {}", cookieManager.getCookieStore().getCookies());
			}

		} catch (IOException e) {
			throw new AccountCreationException(e);
		}
	}
	

	/**
	 * The account creation itself
	 */
	public void createAccount(AccountData account, String crsfToken, String captcha) throws AccountCreationException {
		try {
			// Create Request
			Request request = this.buildAccountCreationRequest(account, crsfToken, captcha);

			// Send Request
			logger.debug("Sending creation request");
			Response response = client.newCall(request).execute();

			// Parse Response
			if (response.isSuccessful()) {

				String strResponse = response.body().string();
				Document doc = Jsoup.parse(strResponse);
				response.body().close();

				Elements accessDenied = doc.getElementsContainingOwnText("Access Denied");
				if (!accessDenied.isEmpty()) {
					throw new AccountCreationException("Access Denied");
				}

				if (dumpError) {
					File debugFile = new File("debug.html");
					debugFile.delete();
					logger.debug("Saving response to {}", debugFile.toPath());
					try (OutputStream out = Files.newOutputStream(debugFile.toPath())) {
						out.write(doc.select(".container").outerHtml().getBytes());
					}
				}

				Elements errors = doc.select(".errorlist");

				if (!errors.isEmpty()) {

					if (errors.size() == 1) {
						logger.error("Invalid Captcha");
						// Try Again maybe ?
						throw new AccountCreationException("Captcha failed");
					} else {
						logger.error("{} error(s) found creating account {} :", errors.size(), account.username);
						for (int i = 0; i < errors.size() - 1; i++) {
							Element error = errors.get(i);
							logger.error("- {}", error.toString().replaceAll("<[^>]*>", "").replaceAll("[\n\r]", "").trim());
						}
					}
				}

				if (!errors.isEmpty()) {
					String firstErrorTxt = errors.get(0).toString().replaceAll("<[^>]*>", "").replaceAll("[\n\r]", "").trim();

					if (firstErrorTxt.contains("username already exists")) {
						throw new AccountDuplicateException(firstErrorTxt);
					} else if (firstErrorTxt.contains("exceed")) {
						throw new AccountRateLimitExceededException(firstErrorTxt);
					} else {
						throw new AccountCreationException("Unknown creation error : " + firstErrorTxt);
					}
				}
				
				logger.debug("SUCCESS : Account created");

			} else {
				throw new AccountCreationException("PTC server bad response, HTTP " + response.code());
			}

		} catch (IOException e) {
			throw new AccountCreationException(e);
		}
	}

	private Request buildAccountCreationRequest(AccountData account, String crsfToken, String captcha) throws UnsupportedEncodingException {

		RequestBody body = new FormEncodingBuilder()
				// Given login and password
				.add("username", account.username)
				.add("email", account.email)
				.add("confirm_email", account.email)
				.add("password", account.password)
				.add("confirm_password", account.password)

				// Technical Tokens
				.add("csrfmiddlewaretoken", crsfToken)
				.add("g-recaptcha-response", captcha)

				.add("public_profile_opt_in", "False")
				.add("screen_name", "")
				.add("terms", "on")
				.build();

		Request request = new Request.Builder()
				.url(url_ptc + pathSignup)
				.method("POST", body)
				.headers(getHeaders())
				.build();

		return request;
	}

	// Http Request for account creation start and age check
	private Request buildAgeCheckRequest() {
		return new Request.Builder().url(url_ptc + pathAgeCheck).headers(getHeaders()).build();
	}

	private Request buildAgeCheckSubmitRequest(String csrfToken) throws UnsupportedEncodingException {

		RequestBody body = new FormEncodingBuilder()
				.add("dob", "1985-01-16")
				.add("country", "US")
				.add("csrfmiddlewaretoken", csrfToken)
				.build();

		Request request = new Request.Builder()
				.url(url_ptc + pathAgeCheck)
				.method("POST", body)
				.headers(getHeaders())
				.build();

		return request;
	}

	// Add all HTTP headers
	private Headers getHeaders() {

		Map<String, String> headersMap = new HashMap<>();
		// Base browser User Agent
		headersMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

		// Send Data as Form
		// headersMap.put("Content-Type", "application/x-www-form-urlencoded");

		// CORS
		headersMap.put("Origin", "https://club.pokemon.com");
		headersMap.put("Referer", "https://club.pokemon.com/us/pokemon-trainer-club/parents/sign-up");
		headersMap.put("Upgrade-Insecure-Requests", "https://club.pokemon.com");

		headersMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headersMap.put("DNT", "1");

		headersMap.put("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6");

		return Headers.of(headersMap);
	}


}
