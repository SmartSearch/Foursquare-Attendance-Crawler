package eu.smartfp7.foursquare;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * An item in a list of time series data.
 *  
 * @author Dyaa Albakour
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
