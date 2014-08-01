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
 *  @author Romain Deveaud <romain.deveaud at glasgow.ac.uk>
 */

package eu.smartfp7.foursquare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class represents time series constructed from the files
 * produced by the SpecificVenueCrawler. The R prefix stands for the
 * R data analysis software, because input files are in CSV format (which
 * is convenient when working with R). Or maybe it is because Romain
 * coded this class. Or maybe both.
 *
 */
public class RTimeSeries {
  
  static final private long Milliseconds_in_1_hour = 3600000;

  /**
   * There are several information duplicates (with the dates) but it is very
   * convenient to access the right values.
   */
  private Map<String,TimeSeriesItem<Double>> here_now_time_series;
  private Map<String,TimeSeriesItem<Double>> hour_checkins_time_series;
  private Map<String,TimeSeriesItem<Double>> total_checkins_time_series;
  private ArrayList<Date>					 dates;
  private Double							 total_here_now;
  
  private Collection<Date>					 missings;
  
  public RTimeSeries(String file) throws IOException, ParseException {
	total_here_now 			   = 0.0;
	here_now_time_series       = new HashMap<String,TimeSeriesItem<Double>>();
	hour_checkins_time_series  = new HashMap<String,TimeSeriesItem<Double>>();
	total_checkins_time_series = new HashMap<String,TimeSeriesItem<Double>>();
	dates					   = new ArrayList<Date>();
	missings				   = new ArrayList<Date>();
	
	// We read the time series from a SpecificVenueCrawler .ts file.
	BufferedReader buffer_forecast = new BufferedReader(new FileReader(file));
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Ignore the first line since it only contains headers (used in R).
	String tmp = buffer_forecast.readLine();
	
	// Iterate through all lines (i.e. hours) to initialise the object.
	while((tmp = buffer_forecast.readLine()) != null) {
	  String[] line = tmp.split(",");
	  here_now_time_series.put(line[0],new TimeSeriesItem<Double>(line[0], Double.parseDouble(line[1])));
	  hour_checkins_time_series.put(line[0],new TimeSeriesItem<Double>(line[0], Double.parseDouble(line[2])));
	  total_checkins_time_series.put(line[0],new TimeSeriesItem<Double>(line[0], Double.parseDouble(line[3])));
	  dates.add(df.parse(line[0]));
	  
	  total_here_now += Double.parseDouble(line[1]);
	}
	buffer_forecast.close();
	
	// Sort the dates in case it's not already the case.
	Collections.sort(this.dates);
  }
  
  /**
   * A function returning a set of Dates that should be present in the time
   * series.
   */
  public Collection<Date> missingDates() {
//	System.out.println("Start missing dates... ");
	if(missings.isEmpty()) {
	  Collection<Date> collection = idealDates();
	  //System.out.println(collection.size()+ " ideal dates.");
	 
	  for(Iterator<Date> i=collection.iterator(); i.hasNext();)
		if (this.dates.contains(i.next()))
		  i.remove();

	  missings.addAll(collection);
	}
	//System.out.println(missings.size()+" missing dates.");
	
	return missings;
  }
  
  /**
   * Returns true if the current RTimeSeries misses some Dates, false otherwise.
   */
  public boolean isBroken() {
	return !missingDates().isEmpty();
  }
  
  /**
   * Returns a set of Date representing the ideal points of the time series.
   */
  public Collection<Date> idealDates() {
	Date first_date = this.dates.get(0);
	Date last_date  = this.dates.get(this.dates.size()-1);
	
	ArrayList<Date> ideal_dates = new ArrayList<Date>();
	
	Date tmp_date = first_date;
	while(!tmp_date.after(last_date)) {
	  ideal_dates.add(tmp_date);

	  Calendar c = Calendar.getInstance();
	  c.setTime(tmp_date);
	  c.add(Calendar.HOUR_OF_DAY, 1);
	  
	  tmp_date = c.getTime();
	}
	
	return ideal_dates;
  }
  
  /**
   * For each missing date, we get the previous and the next date. They will be
   * used to calculate the interpolation.
   * This function can handle two consecutive missing dates, but they will have
   * the same estimated value.
   */
  public Map<Date,Date[]> getInterpolationBounds() {
	Map<Date,Date[]> interp_bounds = new HashMap<Date, Date[]>();
	
	
	for(Date d: this.dates) {
	  for(Date m: missingDates()) {
		
		Date[] bounds = interp_bounds.get(m);
		if(bounds == null) {
		  bounds = new Date[2];
		  interp_bounds.put(m, bounds); 
		}
		
		if(d.before(m))
		  bounds[0] = d;
		else if(d.after(m) && bounds[1] == null)
		  bounds[1] = d;
	
	  }
	}
	
	
	return interp_bounds;
  }
  
  /**
   * Modifies the time series to integrate the estimation of the missing points.
   * There are two implemented methods for recovering missing points: linear interpolation
   * and seasonal naïve. The first one is called only one point is missing, while the 
   * second one is called when successive points are missing.
   */
  public void generateMissingPoints() {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	Map<Date, Date[]> interp_bounds = getInterpolationBounds();
	//System.out.println(System.currentTimeMillis() - start + "ms");
	
	// We iterate over all the missing points we identify.
	for(Date m: missingDates()) {
	  // This variable contains the number of milliseconds between the nearest observations
	  // surrounding the missing point.
	  Long interval = interp_bounds.get(m)[1].getTime() - interp_bounds.get(m)[0].getTime();
	  Double here_now_inter_val   = 0.0;
	  Double hour_check_inter_val = 0.0;
	  
	  String before = df.format(interp_bounds.get(m)[0]);
	  String after  = df.format(interp_bounds.get(m)[1]);
	  
	  // If only one hour is missing, we interpolate the values.
	  if(interval == 2*Milliseconds_in_1_hour) {
		here_now_inter_val   = getLinearlyInterpolatedValue(before, after, this.here_now_time_series);
		hour_check_inter_val = getLinearlyInterpolatedValue(before, after, this.hour_checkins_time_series);
	  }
	  // If several points are missing, we use the seasonal naïve method which
	  // gets the same value as the day before.
	  else if(interval > 2*Milliseconds_in_1_hour) {
		Date n = new Date();
		n.setTime(m.getTime()-24*Milliseconds_in_1_hour);
		
		if(!this.here_now_time_series.containsKey(df.format(n)))
		  n.setTime(m.getTime()-Milliseconds_in_1_hour);
		
		here_now_inter_val   = getSeasonalNaiveValue(df.format(n), this.here_now_time_series);
		hour_check_inter_val = getSeasonalNaiveValue(df.format(n), this.hour_checkins_time_series);
	  }
	  
	  String formatted_m = df.format(m);
	  
	  // Putting the estimated values in the time series.
	  this.here_now_time_series.put(formatted_m, new TimeSeriesItem<Double>(formatted_m, here_now_inter_val));
	  this.hour_checkins_time_series.put(formatted_m, new TimeSeriesItem<Double>(formatted_m, hour_check_inter_val));
	  this.total_checkins_time_series.put(formatted_m, new TimeSeriesItem<Double>(formatted_m, this.total_checkins_time_series.get(before).getValue()+hour_check_inter_val));
	  
	  this.dates.add(m);
	}
	
	Collections.sort(this.dates);
  }
  
  /**
   * For a given time series, get the same value as the day before - same hour.
   */
  public Double getSeasonalNaiveValue(String yesterday_same_hour, Map<String,TimeSeriesItem<Double>> comp) {
	return (comp.get(yesterday_same_hour).getValue());
  }
  
  /**
   * For a given time series and two bounds, calculates the linearly interpolated point.
   */
  public Double getLinearlyInterpolatedValue(String b, String a, Map<String,TimeSeriesItem<Double>> comp) {
	return (comp.get(b).getValue()+comp.get(a).getValue())/2;
  }

  public ArrayList<Date> getDates() {
    return dates;
  }

  public void setDates(ArrayList<Date> dates) {
    this.dates = dates;
    Collections.sort(this.dates);
  }
  
  public Double getTotal_here_now() {
    return total_here_now;
  }
  
  public Double getTotalCheckins() {
	DateFormat    df   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	return total_checkins_time_series.get(df.format(this.dates.get(this.dates.size()-1))).getValue();
  }
  
  
  /**
   * Prints the time series in the .ts file format.
   */
  public String toString() {
	String time_series_file_format = "Date,here_now,hour_checkins,total_checkins\n" ;
	
	DateFormat    df   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DecimalFormat decf = new DecimalFormat("###.#");
	
	for(Date d: this.dates) {
	  time_series_file_format += df.format(d)+","+decf.format(here_now_time_series.get(df.format(d)).getValue())
		  									 +","+decf.format(hour_checkins_time_series.get(df.format(d)).getValue())
		  									 +","+decf.format(total_checkins_time_series.get(df.format(d)).getValue())
		  									 +"\n";
	}
	
	return time_series_file_format;
  }
}