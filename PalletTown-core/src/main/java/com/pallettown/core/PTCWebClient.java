package com.pallettown.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pallettown.core.data.AccountData;
import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.BrowserFactory;
import com.ui4j.api.browser.Page;
import com.ui4j.api.browser.PageConfiguration;
import com.ui4j.api.dom.Document;

// Web client that will create a PTC account
public class PTCWebClient {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private String url_ptc = "https://club.pokemon.com/us/pokemon-trainer-club";
	private String path_signup = "/sign-up/";
	
	private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4) " + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.57 Safari/537.36";
	
	private PageConfiguration config;
	
	private BrowserEngine browser;
	// Current Page
	private Page page;
	
	public PTCWebClient(){
		browser = BrowserFactory.getWebKit();
		config = new PageConfiguration();
        config.setUserAgent(userAgent);
	}
	
	
	// Check for a username
	public boolean checkUsername(){
		
		
		Object result = page.executeScript("test");
		/*
		def _validate_username(driver, username):
		    try:
		        response = driver.request('POST','https://club.pokemon.com/api/signup/verify-username', data={"name": username})
		        response_data = response.json()

		        if response_data['valid'] and not response_data['inuse']:
		            print("User '" + username + "' is available, proceeding...")
		        else:
		            print("User '" + username + "' is already in use.")
		            driver.close()
		            raise PTCInvalidNameException("User '" + username + "' is already in use.")
		    except:
		        print("Failed to check if the username is available!")
		        */
		return true;
	}
	
	
	// Create an account
	public void createAccount(AccountData data){
		page = browser.navigate(url_ptc+path_signup, config);
		page.show();
		Document doc = page.getDocument();
				
        // Workaround for different region not having the same input type
		page.executeScript("var input = document.createElement('input'); input.type='text'; input.setAttribute('name', 'dob'); arguments[0].parentNode.replaceChild(input, arguments[0])");
		
        // Check Page Fields
        if( ! doc.query("[name=verify-age]").isPresent() || ! doc.query("[name=dob]").isPresent() || ! doc.query("[name=country]").isPresent()){
        	// Error Missing elements
        	logger.error("Missing age verification form elements");
        	browser.shutdown();
        	return;
        }
                
		// 1.1. Fill Date of Birth 
        doc.query("[name=dob]").get().setValue(data.getBirthDate());
        
        // 1.2. Set Country (2 letters country code)
        doc.query("[name=country]").get().setValue(data.getCountry());
                
        // 1.3. Submit
        doc.query("[name=verify-age]").get().getForm().get().submit();
        
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
}
