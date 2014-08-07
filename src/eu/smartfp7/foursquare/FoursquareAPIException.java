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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is a custom Exception class that can handle different types 
 * of response from the Foursquare API.
 */

public class FoursquareAPIException extends Exception {
  
  private static final long serialVersionUID = 4253745868433880118L;
  
  private String http_code;
  private String error_type;
  private String error_detail;
  
  private JsonObject response;
  
  public FoursquareAPIException(String json) {
	super(json);
	
	JsonParser parser = new JsonParser();
	JsonObject jsonObj= parser.parse(json).getAsJsonObject();
	
	this.http_code    = jsonObj.get("meta").getAsJsonObject().get("code").getAsString();
	this.error_type   = jsonObj.get("meta").getAsJsonObject().get("errorType").getAsString();
	this.error_detail = jsonObj.get("meta").getAsJsonObject().get("errorDetail").getAsString();
	
	this.response     = jsonObj.get("response").getAsJsonObject();
  }

  public String getHttp_code() {
    return http_code;
  }

  public String getError_type() {
    return error_type;
  }

  public String getError_detail() {
    return error_detail;
  }

  public JsonObject getResponse() {
    return response;
  }

}
