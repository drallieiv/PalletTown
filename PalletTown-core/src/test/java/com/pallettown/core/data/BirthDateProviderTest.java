package com.pallettown.core.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

public class BirthDateProviderTest {

	@Test
	public void test() {
		BirthDateProvider provider = new BirthDateProvider();
		
		String date1 = provider.getFormattedDate();
		String date2 = provider.getFormattedDate();
		
		assertThat(date1).isNotEqualTo(date2);
		assertThat(date1).matches(Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}"));
	}

}
