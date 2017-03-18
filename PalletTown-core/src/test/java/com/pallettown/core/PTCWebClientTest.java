package com.pallettown.core;

import org.junit.Before;
import org.junit.Test;

import com.pallettown.core.data.AccountData;
import com.pallettown.core.data.AccountDataProvider;

public class PTCWebClientTest {

	private AccountDataProvider accountDataProvider;
	
	@Before
	public void init() {
		accountDataProvider = new AccountDataProvider();
		AccountData defaultData = new AccountData();
		defaultData.setBirthDate("1984-10-02");
		defaultData.setCountry("FR");
		
		accountDataProvider.setDefaultData(defaultData);
	}

	@Test
	public void test() {
		PTCWebClient client = new PTCWebClient();

		AccountData data = accountDataProvider.getData();

		client.createAccount(data);
	}

}
