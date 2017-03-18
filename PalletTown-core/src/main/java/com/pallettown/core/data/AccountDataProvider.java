package com.pallettown.core.data;

import lombok.Getter;
import lombok.Setter;

public class AccountDataProvider {

	@Getter
	@Setter
	private AccountData defaultData;
	
	public AccountData getData(){
		AccountData data = defaultData.clone();
		return data;
	}
}
