/**  
 * SMART FP7 - Search engine for MultimediA enviRonment generated contenT
 * Webpage: http://smartfp7.eu
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 * 
 * The Original Code is Copyright (c) 2012-2014 the University of Glasgow
 * All Rights Reserved
 * 
 * Contributor(s):
 *  @author M-Dyaa Albakour <dyaa.albakour at glasgow.ac.uk>
 *  @author Romain Deveaud <romain.deveaud at glasgow.ac.uk>
 */

package eu.smartfp7.foursquare;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An item in a list of time series data.
 *
 */
public class  TimeSeriesItem<T>{
	long   timeInMilis;
	Double prob;
	String dateString;
	T 	   value;
	

	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2013-03-15 19:58:29
	
	public TimeSeriesItem(long timeInMilis, T value) {
		super();
		this.timeInMilis = timeInMilis;
		this.value = value;
	}
	
	public TimeSeriesItem(String timeStr, T value) {
		super();
		try{
			Calendar c = Calendar.getInstance();
			c.setTime(df.parse(timeStr));
			this.timeInMilis = c.getTimeInMillis();
		}catch (ParseException e) {
			e.printStackTrace();
		}
		this.value = value;
		this.dateString = timeStr;
	}
	

	public String getDateString() {
	  return dateString;
	}

	public void setDateString(String dateString) {
	  this.dateString = dateString;
	}

	public String getTimeFormatted(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMilis);
		return df.format(c.getTime());
	}
	
	public long getTimeInMilis() {
		return timeInMilis;
	}
	
	public void setTimeInMilis(long timeInMilis) {
		this.timeInMilis = timeInMilis;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public Double getProb() {
	  return prob;
	}

	public void setProb(Double prob) {
	  this.prob = prob;
	}
}
