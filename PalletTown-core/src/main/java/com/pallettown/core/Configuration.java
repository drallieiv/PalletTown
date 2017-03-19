package com.pallettown.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public class Configuration {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String CONFIG_FILE = "config.properties";

	private static Configuration instance;

	public String twoCaptchaApiKey;

	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
			instance.load(CONFIG_FILE);
		}

		return instance;
	}

	/**
	 * Check if all config are OK
	 * 
	 * @return
	 */
	public boolean checkConfiguration() {
		if (twoCaptchaApiKey == null || twoCaptchaApiKey.isEmpty()) {
			return false;
		}

		return true;
	}

	/**
	 * Load config from prop file
	 * 
	 * @param configFile
	 */
	private void load(String configFile) {
		try {
			Properties prop = new Properties();
			InputStream in = getClass().getResourceAsStream("/" + CONFIG_FILE);
			prop.load(in);
			in.close();

			// Load Config
			this.setTwoCaptchaApiKey(prop.getProperty("twoCaptcha.key"));

		} catch (MissingResourceException e) {
			logger.info("You must copy config.example.properties as config.properties and edit your configuration");
		} catch (IOException e) {
			logger.error("failed loading config.properties");
		}

	}
}
