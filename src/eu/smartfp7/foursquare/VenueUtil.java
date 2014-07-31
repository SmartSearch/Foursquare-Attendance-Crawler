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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * This class contains several static utility functions all related to
 * Foursquare crawled venues.
 *
 */
public class VenueUtil {
  
  public static ArrayList<File> getAllFilesEndingWith(String path, final String extension) {
	File directory = new File(path);
	ArrayList<File> files = new ArrayList<File>(Arrays.asList(directory.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	        return name.endsWith(extension);
	    }
	})));
	
	return files;
  }
  
  public static ArrayList<File> getAllVenueFilesEndingWith(String folder, String city, final String extension) {
	return getAllFilesEndingWith(folder + city + File.separator + "attendances_crawl", extension);
  }
  
  
  public static Collection<String> getVenueIdsFromJSON(JsonObject jsonObj) {
	Collection<String> return_ids = new ArrayList<String>();
	
	for(JsonElement e: jsonObj.get("response").getAsJsonObject().get("groups").getAsJsonArray().get(0).getAsJsonObject().get("items").getAsJsonArray())
	  return_ids.add(e.getAsJsonObject().get("venue").getAsJsonObject().get("id").getAsString());
	
	return return_ids;
  }
  
  public static Collection<Venue> listAllVenues(String folder, Collection<String> cities) throws IOException {
	Collection<Venue> venues = new ArrayList<Venue>();
	
	for(String city: cities)
	  venues.addAll(listAllVenues(folder, city));
	
	return venues;
  }
  
  public static Collection<Venue> listAllVenues(String folder, String city) throws IOException {
	return listAllVenues(folder, city, ".info");
  }
  
  public static Collection<Venue> listAllVenues(String folder, String city, String ext) throws IOException {
	Collection<Venue> venues = new ArrayList<Venue>();
	
	for(File file: getAllVenueFilesEndingWith(folder, city, ext)) {
		BufferedReader info = new BufferedReader(new FileReader(file));
		Venue venue = new Venue(info.readLine());
		venues.add(venue);
		info.close();
	  }
	
	return venues;
  }
  
  public static void generateInvertedGeohashFile(String city, String folder, int precision) throws IOException {
	FileWriter inverted_file = new FileWriter(folder+ city + ".geohash." + precision);
	
	for(File file: getAllVenueFilesEndingWith(folder, city, ".info")) {
	  BufferedReader info = new BufferedReader(new FileReader(file));
	  Venue venue = new Venue(info.readLine());
	  inverted_file.write(venue.getGeoHash(precision)+"\t"+venue.getId()+"\n");
	  info.close();
	}
	
	inverted_file.close();
  }
  
  /**
   * This is a very important function which estimates points in a time series
   * when the crawler couldn't get it.
   * If points are missing, the entire forecasting method will not work as
   * intended, that's why we need complete time series (even if they are
   * not 100% accurate).
   * 
   * This function loops over all the venues of a city, identifies broken
   * time series and fixes them. 
   * 
   */
  public static void fixBrokenTimeSeriesCity(String city, String folder) throws IOException, ParseException {
	for(File file: getAllVenueFilesEndingWith(folder, city, ".ts"))
	  fixBrokenTimeSeriesVenue(file);
  }
  
  public static void fixBrokenTimeSeriesVenue(File file) throws IOException, ParseException {
	RTimeSeries ts = new RTimeSeries(file.getAbsolutePath());

	if(ts.isBroken()) {
	  ts.generateMissingPoints();
	  
	  FileWriter out  = new FileWriter(file.getAbsoluteFile());
	  out.write(ts.toString());
	  out.close();
	}
  }

}