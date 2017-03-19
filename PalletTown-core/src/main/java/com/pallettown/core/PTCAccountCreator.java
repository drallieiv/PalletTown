package com.pallettown.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.data.AccountData;
import com.pallettown.core.errors.AccountCreationException;

public class PTCAccountCreator {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private PTCWebClient client;
	
	public PTCAccountCreator(){
		client = new PTCWebClient();
	}
	
	// Create an account
	public void createAccount(AccountData account) throws AccountCreationException {

		logger.debug("Create account with username {}", account);
		
		// 1. Grab a CRSF token
		String crsfToken = client.sendAgeCheckAndGrabCrsfToken();
		if(crsfToken == null){
			throw new AccountCreationException("Could not grab CRSF token");
		}
		logger.debug("CRSF token found : {}", crsfToken);
		
		// 2. TODO Captcha
		String captcha = "03AI-r6f4XaK-dD6HR4lwSRMKmXbAG4YT4aDbYs4PHQwN3Ma0KtwQlIu5eFnwPt71zPiL1NLXfzjhsD0KFWAtD0JRyODJD9n529qZg7jahvHCs8spBmoaQ-n23M7vaegGCUy2Q4nIrEp0QxUryscXw6COEfJ1TJHDIGLZ2S2L4Dj3u2aOq5rihyQdkxDNikevINaQH6Q379sxS930SRxwuElM-9ON3NEIl82pjw1RpQknyItnIrAIQ4Lvyy4oTra0tp2ovlUGQwmylfSVbSEmHxDAiuaDzqh5RLGf0u_HZfO6qprct7sGXoubdc5NCfJRouXwRE-v-CS4Jf8BGkILNtqaruc4EIctRNvcsSOPpXeXhbl_9LGRpYDtfZyWMshY3R2uXQhnIGKJZacOujKbRam3QVURsYc7nOynowDVfHwTLmdXetMVDsGY";
		
		// 3. TODO name check
		
		// 4. Account Creation
		client.createAccount(account, crsfToken, captcha);
		
	}
}
