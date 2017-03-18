package com.pallettown.core.data;

import lombok.Data;

@Data
public class AccountData implements Cloneable{

	// Birth Date : YYYY-MM-DD
	private String birthDate;

	// 2 char Country Code (US, FR, ...) 
	public String country;
	
	public AccountData clone() {
		AccountData clonedData = new AccountData();
		clonedData.birthDate = this.birthDate;
		clonedData.country = this.country;
        return clonedData;
    }
}
