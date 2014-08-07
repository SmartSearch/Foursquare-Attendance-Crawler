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

package eu.smartfp7.foursquare.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class manages the settings of the attendance crawler.
 * The different options can be changed in the etc/settings.json file.
 * Currently, the options that are supported are:
 * 	- foursquare_api_accounts
 * 	- timezones
 * 	- crawl_folder
 * 
 * See README.md for more information.
 * 
 * This class is a singleton, hence the Settings object must be retrieved by
 * calling the `getInstance()` method.
 *
 */
public class Settings {

  private JsonObject settings_json;
  private static Settings instance = null;
  
  protected Settings() {
	// We load the settings file and parse its JSON only once.
	try {
	  JsonParser parser = new JsonParser();
	  this.settings_json =  parser.parse(StringUtils.join(Files.readAllLines(Paths.get("etc/settings.json"), StandardCharsets.UTF_8)," ")).getAsJsonObject();
	} catch (IOException e) {
	  e.printStackTrace();
	}
  }
  
  public static Settings getInstance() {
	if(instance == null)
	  instance = new Settings();
	
	return instance;
  }

  /**
   * Checks if all the files and directories needed by the crawler already
   * exist, creates them otherwise.
   */
  public void checkFileHierarchy(String city) {
	String folder = getFolder();

	String[] dirs_to_check = {  
		folder + city , 
		folder + city + File.separator + "attendances_crawl",
		folder + city + File.separator + "log",
		folder + city + File.separator + "foursquare_venues",
		folder + city + File.separator + ".exhaustive_crawl",
		folder + city + File.separator + ".deleted",
	};

	for(String dir: dirs_to_check) {
	  File dir_f = new File(dir);
	  if(!dir_f.exists())
		dir_f.mkdirs();
	}
  }

  /** Returns the folder where all the data will be downloaded. */
  public String getFolder() {	  
	String path = this.settings_json.get("crawl_folder").getAsString(); 
	if(path.charAt(path.length()-1) != File.separatorChar)
	  path += File.separator;

	return path;
  }

  /** Returns the Foursquare API credentials for a given city. */
  public Map<String,String> getCityCredentials(String city) {
	Map<String,String> credentials = new HashMap<String,String>();

	JsonObject cred_map = this.settings_json.get("foursquare_api_accounts").getAsJsonObject().get(city).getAsJsonObject();
	credentials.put("client_secret", cred_map.get("client_secret").getAsString());
	credentials.put("client_id", cred_map.get("client_id").getAsString());

	return credentials;
  }

  /** Returns the timezone of a given city. */
  public String getCityTimezone(String city) {
	return this.settings_json.get("timezones").getAsJsonObject().get(city).getAsString();
  }
  
  /** Returns the longitude of the geographical center of a given city. */
  public Double getCityCenterLng(String city) {
	return this.settings_json.get("centers").getAsJsonObject().get(city).getAsJsonObject().get("lng").getAsDouble();
  }
  
  /** Returns the latitude of the geographical center of a given city. */
  public Double getCityCenterLat(String city) {
	return this.settings_json.get("centers").getAsJsonObject().get(city).getAsJsonObject().get("lat").getAsDouble();
  }

}
