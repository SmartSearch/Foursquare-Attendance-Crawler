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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
  
  public static void saveObject(Object vlo, String fileName) {
	FileOutputStream fos = null;
	ObjectOutputStream oos = null;
	try {
	  fos = new FileOutputStream(fileName);
	  oos = new ObjectOutputStream(fos);
	  oos.writeObject(vlo);
	  oos.flush();
	  oos.close();
	  fos.close();
	} catch(IOException ioe) {
	  ioe.printStackTrace();
	}
  }
  
  public static Object loadObject(String fileName) {
	Object obj = null;
	FileInputStream fis = null;
	ObjectInputStream ois = null;
	try {
	  fis = new FileInputStream(fileName);
	  ois = new ObjectInputStream(fis);
	  obj = ois.readObject();
	  ois.close();
	  fis.close();
	} catch(IOException ioe) {
	  ioe.printStackTrace();
	} catch(ClassNotFoundException cnfe) {
	  cnfe.printStackTrace();
	}
	return obj;
  }
  
  /**
   * The generic function for querying the API.
   * (could be a generic function for making any HTTP calls by the way)
   */
  public static String makeAPICall(String url_str) throws IOException {
	URL url = new URL(url_str);
	HttpURLConnection conn =
		(HttpURLConnection) url.openConnection();

	if (conn.getResponseCode() != 200) {
	  throw new IOException(conn.getResponseMessage());
	}

	// Buffer the result into a string
	BufferedReader rd = new BufferedReader(
		new InputStreamReader(conn.getInputStream()));
	StringBuilder sb = new StringBuilder();
	String line;
	while ((line = rd.readLine()) != null) {
	  sb.append(line);
	}
	rd.close();

	conn.disconnect();
	return sb.toString();
  }
  
  
  
  public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
    return bd.doubleValue();
}

}
