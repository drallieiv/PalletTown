package com.pallettown.core;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;

public class PTCAccountCreatorTest {

	@Test
	public void accountCreationTest() throws AccountCreationException {
		PTCAccountCreator creator = new PTCAccountCreator();

		AccountData account = new AccountData();
				
		String userName = "paltTst"+RandomStringUtils.randomAlphanumeric(5);
		
		
		account.setEmail(userName+"@dispostable.com");
		account.setUsername(userName);
		account.setPassword("testAA00+");

		creator.createAccount(account);
	}

}
