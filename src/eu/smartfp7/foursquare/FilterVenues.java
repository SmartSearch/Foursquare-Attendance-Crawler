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
import java.io.IOException;
import java.util.*;

import eu.smartfp7.foursquare.utils.Settings;
import eu.smartfp7.foursquare.utils.Utils;


/**
 * 
 * Since the Foursquare API only allows us to make 5,000 calls per hour, we
 * need to use only a sample of all the venues crawled for a given city.
 * We take the 3,000 venues with the highest number of checkins + a random
 * sample of 1,950 venues from the remaining ones.
 * We keep 50 'empty' API calls as a security margin in case something goes 
 * wrong during the hourly crawl.
 * 
 * This program needs the files from an ExhaustiveTrendsCrawling as input.
 *
 */
public class FilterVenues {

  /**
   * @param args One argument: name of the city.
   */
  public static void main(String[] args) throws IOException {
	String city = args[0];
	String folder = Settings.getInstance().getFolder();

	// We create the output file in the folder specified in the arguments.
	FileWriter outFile = new FileWriter(folder + city + File.separator + "venues.ids");
	
	// Get the information from the crawled venues.
	BufferedReader buffer = new BufferedReader(new FileReader(folder + city + File.separator + ".exhaustive_crawl" + File.separator + "venues.json"));
	String line = null ;
    
	Map<String,Integer> venueCheckins = new HashMap<String,Integer>();
	
    while ((line = buffer.readLine()) != null) {
      // We have a very convenient Venue class which creates an object from
      // a Foursquare JSON line.
      try {
    	Venue venue = new Venue(line);
    	venueCheckins.put(venue.getId(), venue.getCheckincount());
      } catch (Exception e) {
    	continue;
      }
    }
    
    buffer.close();
        
    Collection<String> best_venues  = new ArrayList<String>();
    Collection<String> other_venues = new ArrayList<String>(venueCheckins.keySet());
    
    venueCheckins = Utils.sortMapByValue(venueCheckins);
    
    // We get the top 3,000 venues by their checkins counts.
    int i = 0;
    for(String venue_id: venueCheckins.keySet()) {
      if(i == 3000)
    	break;
      best_venues.add(venue_id);
      i++;
    }
    
    // Then we remove those best venues from the complete set and shuffle
    // the rest...
    other_venues.removeAll(best_venues);
    Collections.shuffle((List<?>) other_venues);
    
    // ... and draw 1,950 venues.
    i = 0;
    for(String venue_id: other_venues) {
      if(i == 1950)
    	break;
      best_venues.add(venue_id);
      i++;
    }
    
    // Writing the venue_ids in the output file.
    for(String venue_id: best_venues)
      outFile.write(venue_id+"\n") ;
    
    outFile.close();
  }

}
