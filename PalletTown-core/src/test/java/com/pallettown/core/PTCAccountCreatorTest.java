package com.pallettown.core;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.pallettown.core.captcha.CaptchaProvider;
import com.pallettown.core.captcha.TwoCaptchaService;
import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;

public class PTCAccountCreatorTest {

	@Test
	public void accountCreationTest() throws AccountCreationException {

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
		}else{
			// Configuration missing
		}

	}

}
