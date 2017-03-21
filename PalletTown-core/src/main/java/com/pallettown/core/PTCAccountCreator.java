package com.pallettown.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.captcha.CaptchaProvider;
import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;

public class PTCAccountCreator {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private PTCWebClient client;

	private CaptchaProvider captchaProvider;

	public PTCAccountCreator(CaptchaProvider captchaProvider) {
		client = new PTCWebClient();
		this.captchaProvider = captchaProvider;
	}

	// Create an account
	public void createAccount(AccountData account) throws AccountCreationException {

		logger.info("Create account with username {}", account);
		
		// 1. Grab a CRSF token
		String crsfToken = client.sendAgeCheckAndGrabCrsfToken();
		if(crsfToken == null){
			throw new AccountCreationException("Could not grab CRSF token. pokemon-trainer-club website may be unavailable");
		}
		logger.debug("CRSF token found : {}", crsfToken);
		
		// 2. TODO name check ?
		
		// 3. Captcha
		String captcha = captchaProvider.getCaptcha();
				
		// 4. Account Creation
		client.createAccount(account, crsfToken, captcha);
		
	}

}
