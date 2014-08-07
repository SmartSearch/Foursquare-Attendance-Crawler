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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.time.DateUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import eu.smartfp7.foursquare.utils.Settings;


/**
 * This class contains methods for crawling the hourly activity of pre-identified
 * trending venues.
 * The crawl can operate for several cities at the same time.
 */

public class AttendanceCrawler {
  
  /**
   * This function loads the Foursquare IDs of the venues for a given city.
   * 
   * @param A city.
   * @return A list of String representing the IDs of the training venues
   * @throws IOException
   */
  public static Collection<String> loadVenues(String city) throws IOException {
	Collection<String> venues = new ArrayList<String>();
	
	String ids_file = Settings.getInstance().getFolder()+ city + File.separator + "venues.ids";
	
	BufferedReader city_file = new BufferedReader(new FileReader(ids_file));
	String line = null;
	while ((line = city_file.readLine()) != null)
	  venues.add(line);
	  
	System.out.println(venues.size()+" venues loaded for "+city);
	city_file.close();
	
	return venues;
  }
  
  /**
   * From GitHub issue: https://github.com/SmartSearch/Foursquare-Attendance-Crawler/issues/3
   * 
   * Venues can be deleted by/on Foursquare, resulting in errors when the crawler 
   * attempts to retrieve the hourly attendance. It also has bad consequences: the 
   * crawler tries to obtain the attendance over and over, draining the number of 
   * API calls, which also impacts the crawling of other venues and can lead to 
   * missing obervations.
   * 
   * This function removes a venue from the different files, so that it won't be
   * considered by the crawler anymore.
   */
  public static void removeVenue(String venue_id, String city) {
	/**
	 * First part: we need to remove `venue_id` from the ids file. We use a temporary
	 * file to do this.
	 */
	String ids_file     = Settings.getInstance().getFolder()+ city + File.separator + "venues.ids";
	String tmp_ids_file = Settings.getInstance().getFolder()+ city + File.separator + "venues.ids.tmp";

	try {
	  BufferedReader reader = new BufferedReader(new FileReader(ids_file));
	  BufferedWriter writer = new BufferedWriter(new FileWriter(tmp_ids_file));
	  
	  String line = null;
	  
	  while((line = reader.readLine()) != null) {
		// Skipping `venue_id` when rewriting the file.
		String trimmedLine = line.trim();
		if(trimmedLine.equals(venue_id)) continue;
		
		writer.write(line+"\n");
	  }
	  
	  reader.close();
	  writer.close();
	} catch (IOException e) {
	  e.printStackTrace();
	}
	
	// When we have finished rewriting, we rename the temporary file so that it
	// becomes the real one.
	new File(tmp_ids_file).renameTo(new File(ids_file));
	
	/** End of first part. */
	
	/**
	 * Second part: we need to delete the files related to the venue that have
	 * been created while crawling (i.e. .ts and .info).
	 * Instead, we move them into a .deleted folder that can allow us to recover
	 * from hypothetical errors.
	 */
	
	new File(Settings.getInstance().getFolder() + city + File.separator + "attendances_crawl" + File.separator + venue_id + ".ts").renameTo(
	new File(Settings.getInstance().getFolder() + city + File.separator + ".deleted"          + File.separator + venue_id + ".ts"));
	
	new File(Settings.getInstance().getFolder() + city + File.separator + "foursquare_venues" + File.separator + venue_id + ".info").renameTo(
	new File(Settings.getInstance().getFolder() + city + File.separator + ".deleted"          + File.separator + venue_id + ".info"));
	
	/** End of second part. */
  }
  
  
  /**
   * We use the entire hour to do all the calls. This method calculates the
   * amount of time the program has to sleep in order to finish crawling
   * every venue before the end of the current hour.
   * It does not account for already crawled venues: sleep time decreases
   * as the hour progresses.
   * Crawling all venues takes thus approximately 40 minutes.
   * 
   */
  public static void intelligentWait(int total_venues, long current_time, long avg_time_spent_crawling) {
	try {
	  double time = (DateUtils.truncate(new Date(current_time+3600000), Calendar.HOUR).getTime()-current_time)/(double)total_venues;
	  if(Math.round(time) < avg_time_spent_crawling)
		avg_time_spent_crawling = 0;
	  Thread.sleep(Math.round(time)-avg_time_spent_crawling);
	} catch (InterruptedException e) {
	  e.printStackTrace();
	}
  }
  
  /**
   * Adapted from Dyaa's code; gets the JSON String containing all the
   * information about a venue, given its ID.
   * 
   */
  public static String getFoursquareVenueById(String venue_id, String id, String secret) throws Exception {
	HttpsURLConnection c = null;
	
	InputStream is = null;
	int rc;
	String url="";
	
	/** This parameter represents the date of the Foursquare API version that we use. 
	 *  If you want to modify the source code, please see https://developer.foursquare.com/overview/versioning */
	String vParam = "20140801";
	
	try {
		url = "https://api.foursquare.com/v2/venues/"+URLEncoder.encode(venue_id,"UTF-8")
				+"?client_id="+ id +
				"&client_secret=" + secret +
				"&v="+vParam ;
		
	} catch (UnsupportedEncodingException e1) {
		e1.printStackTrace();
	}
	
	final StringBuilder out = new StringBuilder();
	try {
	  c = (HttpsURLConnection) (new URL(url)).openConnection();

	  c.setRequestMethod("GET");
	  c.setDoOutput(true);
	  c.setReadTimeout(20000);	             

	  c.connect();

	  // Getting the response code will open the connection,
	  // send the request, and read the HTTP response headers.
	  // The headers are stored until requested.
	  rc = c.getResponseCode();

	  is = c.getInputStream();

	  final char[] buffer = new char[2048];

	  final Reader in = new InputStreamReader(is, "UTF-8");
	  for (;;) {
		int rsz = in.read(buffer, 0, buffer.length);
		if (rsz < 0)
		  break;
		out.append(buffer, 0, rsz);
	  }
	  JsonElement parsed_line;
	  try {
		parsed_line = new JsonParser().parse(out.toString());
	  }
	  catch(com.google.gson.JsonSyntaxException e) {
		System.out.println("Skipped malformed line in the Foursquare crawl.");
		throw e;
	  }
	  
	  if (rc != HttpsURLConnection.HTTP_OK) {
		throw new FoursquareAPIException(out.toString());
	  }
	  	  
	  return parsed_line.getAsJsonObject().get("response").getAsJsonObject().get("venue").toString();

	} catch (ClassCastException e) {
	  throw new IllegalArgumentException("Not an HTTP URL");
	} catch (MalformedURLException e) {
	  throw e;
	} catch (IOException e) {				
	  throw e;
	}
  }
  
  public static String getFoursquareVenueById(String venue_id, String city) throws Exception {
	Map<String,String> credentials = Settings.getInstance().getCityCredentials(city);
	return getFoursquareVenueById(venue_id, credentials.get("client_id"), credentials.get("client_secret"));
  }
  
  
  /**
   * The main takes an undefined number of cities as arguments, then initializes
   * the specific crawling of all the trending venues of these cities.
   * The trending venues must have been previously identified using the `DownloadPages`
   * program.
   * 
   * Current valid cities are: london, amsterdam, goldcoast, sanfrancisco.
   * 
   */
  public static void main(String[] args) throws Exception{
	Settings settings = Settings.getInstance();
	String folder = settings.getFolder();
	
	// We keep info and error logs, so that we know what happened in case
	// of incoherence in the time series.
	Map<String,FileWriter> info_logs  = new HashMap<String, FileWriter>();
	Map<String,FileWriter> error_logs = new HashMap<String, FileWriter>();
	
	// For each city we monitor, we store the venue IDs that we got from
	// a previous crawl.
	Map<String,Collection<String>> city_venues = new HashMap<String, Collection<String>>();
	
	// Contains the epoch time when the last API call has been made for each 
	// venue. Ensures that we get data only once each hour. 
	Map<String,Long> venue_last_call = new HashMap<String, Long>();
	
	// Contains the epoch time when we last checked if time series were broken
	// for each city.
	// We do these checks once every day before the batch forecasting begins.
	Map<String,Long> sanity_checks   = new HashMap<String, Long>();
	
	// We also keep in memory the number of checkins for the last hour for
	// each venue.
	Map<String,Integer> venue_last_checkin = new HashMap<String, Integer>();
	
	Map<Long,Integer> APICallsCount = new HashMap<Long, Integer>();
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	int total_venues = 0 ;
	long total_calls = 0 ;
	long time_spent_on_API = 0;
	
	
	for(String c: args) {
	  settings.checkFileHierarchy(c);

	  city_venues.put(c, loadVenues(c));
	  total_venues += city_venues.get(c).size();
	  
	  info_logs.put (c,new FileWriter(folder + c + File.separator + "log" + File.separator + "info.log", true));
	  error_logs.put(c,new FileWriter(folder + c + File.separator + "log" + File.separator + "error.log",true));
	  
	  Calendar cal = Calendar.getInstance();
	  
	  info_logs.get(c).write("["+df.format(cal.getTime())+"] Crawler initialization for "+c+". "+city_venues.get(c).size()+" venues loaded.\n");
	  info_logs.get(c).flush();
	  
	  // If we interrupted the program for some reason, we can get back
	  // the in-memory data.
	  // Important: the program must not be interrupted for more than one
	  // hour, or we will lose time series data.
	  for(String venue_id: city_venues.get(c)){
		String ts_file = folder + c + File.separator + "attendances_crawl" + File.separator + venue_id + ".ts";
		
		if(new File(ts_file).exists()) {
		  BufferedReader buffer = new BufferedReader(new FileReader(ts_file));
		  String mem = null, line = null;
		  for(;(line = buffer.readLine()) != null; mem = line) ;
		  buffer.close();
		  
		  if(mem == null) continue;
		  
		  String[] tmp = mem.split(",");
		  venue_last_call.put(venue_id,df.parse(tmp[0]).getTime());
		  venue_last_checkin.put(venue_id, Integer.parseInt(tmp[3]));
		  		  
		  VenueUtil.fixBrokenTimeSeriesVenue(new File(ts_file));
		} // if
	  } // for
	  
	  sanity_checks.put(c, cal.getTimeInMillis());
	} // for
	
	if(total_venues > 5000) {
	  System.out.println("Too much venues for a single API account (max 5000).\nPlease create a new Foursquare API account and use these credentials.\nExiting now.");
	  return;
	}
	
	while(true) {
	  
	  for(String c: args) {
		// We create a FIFO queue and pop venue IDs one at a time.
		LinkedList<String> city_venues_buffer = new LinkedList<String>(city_venues.get(c));
		String venue_id = null;
		
		// Artificial wait to avoid processors looping at 100% of their capacity
		// when there is no more venues to crawl for the current hour.
		Thread.sleep(3000);
		
		while((venue_id = city_venues_buffer.pollFirst()) != null) {
		  // We get the current time according to the city's time zone
		  Calendar cal = Calendar.getInstance();
		  cal.add(Calendar.MILLISECOND, 
			  	  TimeZone.getTimeZone(settings.getCityTimezone(c)).getOffset(cal.getTime().getTime()) -
			  	  Calendar.getInstance().getTimeZone().getOffset(cal.getTime().getTime()));
			  	  //TimeZone.getTimeZone("Europe/London").getOffset(cal.getTime().getTime()));
		  
		  long current_time = DateUtils.truncate(cal.getTime(), Calendar.HOUR).getTime();
		  		  
		  // We query Foursquare only once per hour per venue.
		  if(venue_last_call.get(venue_id) != null && current_time < venue_last_call.get(venue_id)+3600000)
			continue;
		  
		  intelligentWait(total_venues,cal.getTime().getTime(),(total_calls == 0 ? 0 : Math.round(time_spent_on_API/total_calls)));

		  Venue venue = null;
		  
		  try {	
			long beforeCall = System.currentTimeMillis();
			venue = new Venue(getFoursquareVenueById(venue_id,c));

			// If there is no last call, this is the beginning of the time series
			// for this venue. We get the number of people "here now" to initialize
			// the series.
			if(venue_last_call.get(venue_id) == null) {
			  /** TODO: by doing this, we keep a representation of the venue dating from the beginning
			   * 	   of the specific crawl. we might want to change this and update this file once
			   *	   in a while.
			   */
			  FileWriter info = new FileWriter(folder + c + File.separator + "foursquare_venues" + File.separator + venue_id + ".info");
			  info.write(venue.getFoursquareJson());
			  info.close();

			  FileWriter out  = new FileWriter(folder + c + File.separator + "attendances_crawl" + File.separator + venue_id + ".ts");
			  out.write("Date,here_now,hour_checkins,total_checkins\n");
			  out.write(df.format(current_time)+","+venue.getHereNow()+","+venue.getHereNow()+","+venue.getCheckincount()+"\n");
			  out.close();
			}
			else {
			  FileWriter out  = new FileWriter(folder + c + File.separator + "attendances_crawl" + File.separator + venue_id + ".ts", true);
			  int checks = venue.getCheckincount()-venue_last_checkin.get(venue_id);
			  out.write(df.format(current_time)+","+venue.getHereNow()+","+Integer.toString(checks)+","+venue.getCheckincount()+"\n");
			  out.close();
			}
			
			if(APICallsCount.get(current_time) == null)
			  APICallsCount.put(current_time, 1);
			else
			  APICallsCount.put(current_time, APICallsCount.get(current_time)+1);
			
			total_calls++;

			venue_last_call.put(venue_id, current_time);
			venue_last_checkin.put(venue_id,venue.getCheckincount());
			
			time_spent_on_API += System.currentTimeMillis()-beforeCall;
		  } catch(FoursquareAPIException e) {
			// If something bad happens (crawler not available, IO error, ...), we put the
			// venue_id in the FIFO queue so that it gets reevaluated later.
			//e.printStackTrace();
			error_logs.get(c).write("["+df.format(cal.getTime().getTime())+"] Venue "+venue_id+" error with HTTP code "+e.getHttp_code()+". "+APICallsCount.get(current_time)+" API calls so far this hour, "+city_venues_buffer.size()+" venues remaining in the buffer.\n");
			error_logs.get(c).flush();
			
			System.out.println("["+df.format(cal.getTime().getTime())+"] "+c+" -- "+APICallsCount.get(current_time)+" API calls // "+city_venues_buffer.size()+" venues remaining "+" ("+e.getHttp_code()+")");
			
			if(e.getHttp_code().equals("400") && e.getError_detail().equals("Venue "+venue_id+" has been deleted")) {
			  city_venues.get(c).remove(venue_id);
			  removeVenue(venue_id,c);
			}
			else if(e.getHttp_code().equals("403") || e.getHttp_code().equals("500") || e.getHttp_code().equals("504") || e.getHttp_code().equals("502"))
			  city_venues_buffer.add(venue_id);
			
			continue;
		  }
		} // while
		
		
		// Every day between 0am and 2am, we repair all the broken time series (if there
		// is something to repair).
		Calendar cal = Calendar.getInstance();
		if(city_venues_buffer.peekFirst() == null && (cal.getTimeInMillis()-sanity_checks.get(c)) >= 86400000 && cal.get(Calendar.HOUR_OF_DAY) < 2 ) {
		  VenueUtil.fixBrokenTimeSeriesCity(c, folder);
		  sanity_checks.put(c,cal.getTimeInMillis());
		  info_logs.get(c).write("["+df.format(cal.getTime())+"] Sanity check OK.\n");
		  info_logs.get(c).flush();
		}
	  } // for
	} // while
  } // main
} // class
