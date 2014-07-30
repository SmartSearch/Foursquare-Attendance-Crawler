package eu.smartfp7.foursquare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.smartfp7.geo.GeoUtil;

/**
 * A (FourSquare) venue as defined by the FourSquare venue. 
 * 
 * 
 * 
 * 
 * @author Dyaa Albakour
 */
/**
 * @author dyaa
 *
 */
/**
 * @author dyaa
 *
 */
public class Venue{

	
	/*
	 * 
	 * This is an example of a venue in the JSON format
 * {"id":"4ad08b28f964a52064d820e3",
"name":"Westfield London",
"contact":{"phone":"+442033712300","formattedPhone":"+44 20 3371 2300","twitter":"westfieldlondon"},
"location":{"address":"Ariel Way","lat":51.50721994598464,"lng":-0.2215290069580078,"distance":1943,"postalCode":"W12 7GF","city":"Shepherd's Bush","state":"Greater London","country":"United Kingdom","cc":"GB"},
"canonicalUrl":"https://foursquare.com/v/westfield-london/4ad08b28f964a52064d820e3",
"categories":[{"id":"4bf58dd8d48988d1fd941735","name":"Mall","pluralName":"Malls","shortName":"Mall","icon":{"prefix":"https://foursquare.com/img/categories_v2/shops/mall_","suffix":".png"},"primary":true}],
"verified":true,
"restricted":true,
"stats":{"checkinsCount":46003,"usersCount":19916,"tipCount":235},
"url":"http://uk.westfield.com/london/","likes":{"count":0,"groups":[]},
"hereNow":{"count":11,"groups":[{"type":"others","name":"Other people here","count":11,"items":[]}]},
"venuePage":{"id":"33156056"}}
	 */
	
	private String id;
	private String name;
	private String phone;
	private String twitter;
	private String postalCode;
	private String address;
	private String city;
	private String state;
	private String country;
	private String canonicalUrl;
	private String url;
	private String foursquareJson;
	
	private Double lat;
	private Double lon;
	private Double rating=0.0;
	
	private String[] categoriesIds;
	private String[] categoriesNames;
	private Map<String,String> categories;
	
	private Collection<String> photos;
	private Collection<String> icons;
	private Map<String,String> category_icons;
	
	private int venuePageId;
	private int checkincount=-1;
	private int hereNow=0;
	private int likes=0;
	
	
	public Venue() {
		super();
		
	}
	
	public Venue(String jsonObjString){
	  	setFoursquareJson(jsonObjString);
	  
		JsonParser parser = new JsonParser();
		
		JsonObject jsonObj= parser.parse(jsonObjString).getAsJsonObject();
		setName(jsonObj.get("name").getAsString());
		setId(jsonObj.get("id").getAsString());
		
		
		JsonArray jsonArray = jsonObj.get("categories").getAsJsonArray();
		int s  =jsonArray.size();
		/*
		 * TODO: remove this part if everything is working fine with the new code.
		 * 
		String[] catIds = new String[s];
		for(int i=0;i<s;i++){
			catIds[i]= jsonArray.get(i).getAsJsonObject().get("id").getAsString();
		}
		setCategoriesIds(catIds);
		
		String[] catNames = new String[s];
		for(int i=0;i<s;i++){
			catNames[i]= jsonArray.get(i).getAsJsonObject().get("name").getAsString();
		}
		setCategoriesNames(catNames);
		*/
		
		this.categories = new HashMap<String, String>();
		this.category_icons = new HashMap<String,String>();
		
		for(int i=0;i<s;i++){
		  this.categories.put(jsonArray.get(i).getAsJsonObject().get("id").getAsString(), jsonArray.get(i).getAsJsonObject().get("name").getAsString());
		  JsonObject iconObject = jsonArray.get(i).getAsJsonObject().get("icon").getAsJsonObject();
		  this.category_icons.put(jsonArray.get(i).getAsJsonObject().get("name").getAsString(),iconObject.get("prefix").getAsString()+"32"+iconObject.get("suffix").getAsString());
		}
		
		this.photos = new ArrayList<String>();
		this.icons = new ArrayList<String>();
		
		if(jsonObj.has("url"))
			setUrl(jsonObj.get("url").getAsString().replaceAll("â.*", ""));
		if(jsonObj.has("canonicalUrl"))
			setCanonicalUrl(jsonObj.get("canonicalUrl").getAsString());
		if(jsonObj.has("location")){
			// e.g. location":{"address":"Ariel Way","lat":51.50721994598464,"lng":-0.2215290069580078,
			//		"distance":1943,"postalCode":"W12 7GF","city":"Shepherd's Bush","state":"Greater London",
			//		"country":"United Kingdom","cc":"GB"}
			JsonObject locObj = jsonObj.get("location").getAsJsonObject();
			if(locObj.has("address"))
				setAddress(locObj.get("address").getAsString());
			if(locObj.has("city"))
				setCity(locObj.get("city").getAsString());
			if(locObj.has("state"))
				setState(locObj.get("state").getAsString());
			if(locObj.has("country"))
				setCountry(locObj.get("country").getAsString());
			if(locObj.has("postalCode"))
				setPostalCode(locObj.get("postalCode").getAsString());
			if(locObj.has("lat"))
				setLat(locObj.get("lat").getAsDouble());
			if(locObj.has("lng"))
				setLon(locObj.get("lng").getAsDouble());
			
		}
		if(jsonObj.has("stats")){
			JsonObject statObject = jsonObj.get("stats").getAsJsonObject();
			if(statObject.has("checkinsCount"))
				setCheckincount(statObject.get("checkinsCount").getAsInt());
		}
		if(jsonObj.has("hereNow")) {
		  JsonObject statObject = jsonObj.get("hereNow").getAsJsonObject();
		  if(statObject.has("count"))
			setHereNow(statObject.get("count").getAsInt());
		}
		if(jsonObj.has("likes")) {
		  JsonObject statObject = jsonObj.get("likes").getAsJsonObject();
		  if(statObject.has("count"))
			setLikes(statObject.get("count").getAsInt());
		}
		if(jsonObj.has("rating"))
		  setRating(jsonObj.get("rating").getAsDouble());
		if(jsonObj.has("photos")) {
			for(JsonElement e: jsonObj.get("photos").getAsJsonObject().get("groups").getAsJsonArray()) {
			  if(e.getAsJsonObject().get("type").getAsString().equals("venue")) {
				for(JsonElement e2: e.getAsJsonObject().get("items").getAsJsonArray()) {
				  this.photos.add(e2.getAsJsonObject().get("prefix").getAsString().toString()+"300x100"+e2.getAsJsonObject().get("suffix").getAsString().toString());
				  this.icons.add(e2.getAsJsonObject().get("prefix").getAsString().toString()+"36x36"+e2.getAsJsonObject().get("suffix").getAsString().toString());
				}
			  }
			}
		}
		
	}
	
	public Map<String,String> getCategoryIcons() {
	  return category_icons;
	}
	
	public Collection<String> getIcons() {
	  return icons;
	}
	
	public Collection<String> getPhotos() {
	  return photos;
	}

	public void setPhotos(Collection<String> photos) {
	  this.photos = photos;
	}

	public String getGeoHash(int precision) {
	  return (this.lat != null && this.lon != null) ? GeoUtil.geoHash(this.lon, this.lat, precision): null ;
	}
	
	public void addCategory(String id, String name) {
	  this.categories.put(id, name);
	}
	
	public Map<String,String> getCategories() {
	  return this.categories;
	}
	
	public String getFoursquareJson() {
	  return foursquareJson;
	}

	public void setFoursquareJson(String foursquareJson) {
	  this.foursquareJson = foursquareJson;
	}

	public int getHereNow() {
	  return hereNow;
	}

	public void setHereNow(int hereNow) {
	  this.hereNow = hereNow;
	}

	public int getCheckincount() {
		return checkincount;
	}

	public void setCheckincount(int checkincount) {
		this.checkincount = checkincount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getTwitter() {
		return twitter;
	}
	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public int getVenuePageId() {
		return venuePageId;
	}
	public void setVenuePageId(int venuePageId) {
		this.venuePageId = venuePageId;
	}
	public String getCanonicalUrl() {
		return canonicalUrl;
	}
	public void setCanonicalUrl(String canonicalUrl) {
		this.canonicalUrl = canonicalUrl;
	}
	public String getUrl() {
		return url == null ? this.getCanonicalUrl() : url ;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getState() {
	  	return state;
	}
	public void setState(String state){
	  	this.state = state;
	}
	public String getCountry() {
	  	return country;
	}
	public void setCountry(String country){
	  	this.country = country;
	}
	
	
	@Deprecated
	public String[] getCategoriesIds() {
		return categoriesIds;
	}

	@Deprecated
	public void setCategoriesIds(String[] categoriesIds) {
		this.categoriesIds = categoriesIds;
	}

	@Deprecated
	public String[] getCategoriesNames() {
		return categoriesNames;
	}

	@Deprecated
	public void setCategoriesNames(String[] categoriesNames) {
		this.categoriesNames = categoriesNames;
	}




	private List<TimeSeriesItem<Integer>> hereNowTimeseries = new ArrayList<TimeSeriesItem<Integer>>();

	/**
	 * 
	 * @return the raw timeseries data available for the venue
	 */
	public List<TimeSeriesItem<Integer>> getHereNowTimeseries() {
		return hereNowTimeseries;
	}
	
	
	/**
	 * Convert the <i>HereNow</i> raw time series into an array of integers representing 
	 * 	a time series on a regular interval specified 
	 * 
	 * @param starttime
	 * @param endtime
	 * @param interval 
	 */
	public Double[] getNormalisedHereNowTimeSeries(long starttime, 
			long endtime, long interval) throws Exception{
		if(starttime>= endtime)
			throw new Exception("Invalid parameters");
		
		if(this.getHereNowTimeseries().size()==0)
			return null;
		
		long first = this.getHereNowTimeseries().get(0).getTimeInMilis(); 
		
		List<Double> list= new ArrayList<Double>();
		
		// Number of zeros to be added until there is data 
		// Each point of time correspond to the interval that preceded it.
		//		Note that the first point considered corresponds to the zero index
		/*
		int tofill = (int) ((first - starttime)/(int)interval) +1;
		
		
		for (int i=0;i<tofill;i++)	list.add(0.0);		
		
		long currentPoint = starttime;
		if (tofill>1)
			currentPoint =	starttime + (tofill-1)* interval;
		int accumulator =0; // accumulator of the observations found
		int observations = 0;
		boolean exceedEndTime = false;
		bigloop: for(TimeSeriesItem<Integer> tsi: this.getHereNowTimeseries()){
			long ct = tsi.getTimeInMilis();
			if( ct < currentPoint+interval  ){
				int value = tsi.getValue();				
				accumulator += value;
				observations++;
			}else{
				
				double v = (double)accumulator/(double)observations;
				list.add(v);
				accumulator = 0;
				observations =0;				
				currentPoint = currentPoint+ interval;
				while(ct -currentPoint>interval){
					list.add(0.0);
					currentPoint += interval;
				}
				int value = tsi.getValue();				
				accumulator += value;
				observations++;
				
			}
			
			
			if(currentPoint+interval> endtime){
				while(currentPoint+interval> endtime){
					currentPoint-=interval;
				
					list.remove(list.size()-1);
				}
				exceedEndTime = true;
				break bigloop;
			}
			
		}
		
		
		if(!exceedEndTime){
			
			while(! (currentPoint+interval> endtime)){
				currentPoint+=interval;
					
				list.add(0.0);
			}
			
		}*/
		long current= starttime;
		int pointer = 0;
		for(;current<endtime+interval; current += interval){
			/*
			int accumulator =0; // accumulator of the observations found
			int observations = 0;
			long ct =this.getHereNowTimeseries().get(pointer).getTimeInMilis();			 
			while(ct<current+interval){
				TimeSeriesItem<Integer> tsi = this.getHereNowTimeseries().get(pointer);
				ct = tsi.getTimeInMilis();
				int v = tsi.getValue();
				accumulator+=v;
				observations++;
				pointer++;
			}*/
			List<Integer> indices = timeseriesIndeicsBetween(current, current+interval);
			int accumulate=0;
			int observations =0;
			for(int x:indices){
				accumulate+= this.getHereNowTimeseries().get(x).getValue();
				observations++;
			}
			if(observations>0)
				list.add((double)accumulate/(double)observations);
			else
				list.add(0.0);
			
		}
		
		
	
		return list.toArray(new Double[list.size()]);
	}


	public class HereNow{
		int count;
		
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Venue)
		{
			Venue v = (Venue) obj;
			return v.getId().equals(this.getId());
		}
		return false;
	}

	
	
	private List<Integer> timeseriesIndeicsBetween(long start, long end){
		List<Integer> list = new ArrayList<Integer>();
		for(int i=0;i<this.getHereNowTimeseries().size();i++){
			long t = getHereNowTimeseries().get(i).getTimeInMilis();
			 
			if(t>=start && t< end){
				list.add(i);
			}
			if(t> end) break;
			
		}
		return list;
		
	}

	public int getLikes() {
	  return likes;
	}

	public void setLikes(int likes) {
	  this.likes = likes;
	}

	public Double getRating() {
	  return rating;
	}

	public void setRating(Double rating) {
	  this.rating = rating;
	}

	
}
