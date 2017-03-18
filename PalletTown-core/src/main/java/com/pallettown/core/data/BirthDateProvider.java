package com.pallettown.core.data;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Calendar;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

/**
 * Generator for Birth Date
 * @author drallieiv
 *
 */
@Getter
@Setter
public class BirthDateProvider {

	public int minAge = 13;
	public int maxAge = 50;
	
	private static String PTC_DATE_FORMAT = "yyyy-MM-dd";
	
	public String getFormattedDate(){
		Random r = new Random();
		
		LocalDate date = LocalDate.now();
		date = date.minusYears(r.ints(minAge, maxAge).findFirst().getAsInt());
		date = date.minusDays(r.ints(0, 365).findFirst().getAsInt());

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PTC_DATE_FORMAT);
		return date.format(formatter);
	}
}
