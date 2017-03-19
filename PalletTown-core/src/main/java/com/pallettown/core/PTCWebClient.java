package com.pallettown.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;
import com.pallettown.core.errors.AccountDuplicateException;
import com.pallettown.core.errors.AccountRateLimitExceededException;

// Web client that will create a PTC account
public class PTCWebClient {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String url_ptc = "https://club.pokemon.com/us/pokemon-trainer-club";
	private String pathAgeCheck = "/sign-up/";
	private String pathSignup = "/parents/sign-up";

	// Dom element that contains the Cross-Site Request Forgery (CSRF) Token
	private static final Pattern RegexCsrf = Pattern.compile("<input type='hidden' name='csrfmiddlewaretoken' value='(\\w+)' />");

	private HttpClient client;

	private CookieStore cookieStore;

	public PTCWebClient() {
		cookieStore = new BasicCookieStore();
		// Initialize Http Client
		client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).setDefaultCookieStore(cookieStore).build();
		// client = HttpClientBuilder.create().build();
	}

	// Simulate new account creation age check and dump CRSF token
	public String sendAgeCheckAndGrabCrsfToken() throws AccountCreationException {
		try {
			HttpResponse httpResponse = client.execute(buildAgeCheckRequest());

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

				logger.debug("Cookies are now  : {}", cookieStore.getCookies());

				Matcher matcher = RegexCsrf.matcher(result);
				if (matcher.find()) {

					String crsfToken = matcher.group(1);
					sendAgeCheck(crsfToken);
					return crsfToken;
				}
				logger.error("CSRF Token not found");
			}
		} catch (ParseException | IOException e) {
			logger.error("Technical error getting CSRF Token", e);
		}
		return null;
	}

	public void sendAgeCheck(String crsfToken) throws AccountCreationException {
		try {
			// Create Request
			HttpPost request = this.buildAgeCheckSubmitRequest(crsfToken);

			// Send Request
			logger.debug("Sending age check request");
			HttpResponse response = client.execute(request);

			// Parse Response
			if (response.getStatusLine().getStatusCode() == 200) {
				logger.debug("Cookies are now : {}", cookieStore.getCookies());
			}

		} catch (IOException e) {
			throw new AccountCreationException(e);
		}
	}

	public void createAccount(AccountData account, String crsfToken, String captcha) throws AccountCreationException {
		try {
			// Create Request
			HttpPost request = this.buildAccountCreationRequest(account, crsfToken, captcha);

			// Send Request
			logger.debug("Sending creation request");
			HttpResponse response = client.execute(request);

			// Parse Response
			if (response.getStatusLine().getStatusCode() == 200) {

				Document doc = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "");

				Elements accessDenied = doc.getElementsContainingOwnText("Access Denied");
				if (!accessDenied.isEmpty()) {
					throw new AccountCreationException("Access Denied");
				}

				File debugFile = new File("debug.html");
				debugFile.delete();
				logger.debug("Saving response to {}", debugFile.toPath());
				try (OutputStream out = Files.newOutputStream(debugFile.toPath())) {
					out.write(doc.select(".container").outerHtml().getBytes());
				}

				Elements errors = doc.select(".errorlist");

				if (!errors.isEmpty()) {
					
					if(errors.size() ==  1){
						logger.error("Invalid Captcha");
						// Try Again maybe ?
						throw new AccountCreationException("Captcha failed");
					}else{
						logger.error("{} error(s) found creating account {} :", errors.size() , account.username);
						for (int i=0; i < errors.size()  - 1; i++) {
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

			} else {
				throw new AccountCreationException("PTC server bad response, HTTP " + response.getStatusLine().getStatusCode());
			}

		} catch (IOException e) {
			throw new AccountCreationException(e);
		}
	}

	private HttpPost buildAccountCreationRequest(AccountData account, String crsfToken, String captcha) throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(url_ptc + pathSignup);
		setHttpHeaders(post);

		List<NameValuePair> params = new ArrayList<>(2);
		// Given login and password
		params.add(new BasicNameValuePair("username", account.username));
		params.add(new BasicNameValuePair("email", account.email));
		params.add(new BasicNameValuePair("confirm_email", account.email));
		params.add(new BasicNameValuePair("password", account.password));
		params.add(new BasicNameValuePair("confirm_password", account.password));

		// Technical Tokens
		params.add(new BasicNameValuePair("csrfmiddlewaretoken", crsfToken));
		params.add(new BasicNameValuePair("g-recaptcha-response", captcha));

		params.add(new BasicNameValuePair("public_profile_opt_in", "False"));
		params.add(new BasicNameValuePair("screen_name", ""));
		params.add(new BasicNameValuePair("terms", "on"));

		UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(params, "UTF-8");

		post.setEntity(encodedFormEntity);

		return post;
	}

	// Http Request for account creation start and age check
	private HttpGet buildAgeCheckRequest() {
		return new HttpGet(url_ptc + pathAgeCheck);
	}

	private HttpPost buildAgeCheckSubmitRequest(String csrfToken) throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(url_ptc + pathAgeCheck);
		setHttpHeaders(post);

		List<NameValuePair> params = new ArrayList<>(2);
		// Given login and password
		params.add(new BasicNameValuePair("country", "US"));
		params.add(new BasicNameValuePair("csrfmiddlewaretoken", csrfToken));
		params.add(new BasicNameValuePair("dob", "1985-01-16"));

		UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(params, "UTF-8");

		post.setEntity(encodedFormEntity);

		return post;
	}

	// Add all HTTP headers
	private void setHttpHeaders(HttpPost post) {
		// Base browser User Agent
		post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");

		// Send Data as Form
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");

		// CORS
		post.addHeader("Origin", "https://club.pokemon.com");
		post.addHeader("Referer", "https://club.pokemon.com/us/pokemon-trainer-club/parents/sign-up");
		post.addHeader("Upgrade-Insecure-Requests", "https://club.pokemon.com");

		post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		post.addHeader("DNT", "1");

		post.addHeader("Accept-Encoding", "gzip, deflate, br");
		post.addHeader("Accept-Language", "en-GB,en-US;q=0.8,en;q=0.6");
	}

}
