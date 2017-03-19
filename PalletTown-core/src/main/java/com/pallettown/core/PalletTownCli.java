package com.pallettown.core;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.captcha.CaptchaProvider;
import com.pallettown.core.captcha.TwoCaptchaService;
import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;
import com.squareup.okhttp.ConnectionPool;

public class PalletTownCli {

	private static Logger LOGGER = LoggerFactory.getLogger(PalletTownCli.class);
	
	public static void main(String[] args) throws AccountCreationException {
		
		Configuration config = Configuration.getInstance();
		if (config.checkConfiguration()) {
			CaptchaProvider captchaProvider = new TwoCaptchaService(config.getTwoCaptchaApiKey());

			PTCAccountCreator creator = new PTCAccountCreator(captchaProvider);

			AccountData account = new AccountData();

			String userName = "paltTst" + RandomStringUtils.randomAlphanumeric(5);

			account.setEmail(userName + "@dispostable.com");
			account.setUsername(userName);
			account.setPassword("testAA00+");

			creator.createAccount(account);

			LOGGER.info("DONE");
			
			// Cleanup
			ConnectionPool.getDefault().evictAll();
			
		}else{
			// Configuration missing
		}

	}

}
