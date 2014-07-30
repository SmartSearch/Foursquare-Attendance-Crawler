package eu.smartfp7.geo;

public class GeoUtil {
	
	/**
	 * 
	 * Calculate the distance in miles between two points
	 * 
	 * 
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	           Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    return   earthRadius * c;
	}
	
	
	/**
	 * Given a point (A) in the Geo-spatial space, using a Latitude and Longitude value (Point A), 
	 *  calculate another Point B, X meters away bearing \theta radians from point A. Then
	 * 
	 * @param lat
	 * @param lon
	 * @param x
	 * @param theta: measured counterclockwise from due east
	 * @return
	 */
	public static double[] nextPoint(double[] pointA, double x, double theta)
	{
		double lat = pointA[0];
		double lon = pointA[1];
		//double radius = 3958.75*1000.0;
		double dx = x* Math.cos(theta);// theta measured counterclockwise from due east
		double dy = x* Math.sin(theta); //  dx, dy same units as R
		
		double deltaLon = dx/(111320 * Math.cos(lat));
		double deltaLat = dy/110540;
		
		double[] point = new double[]{lat+deltaLat, lon+deltaLon};
		
		return point;
		
	}
	
	
	
	public static void main(String[] args){
		
		
		double distance = GeoUtil.distFrom(51.28,-0.489,51.686,0.236);
		System.out.println("Radius of M25: " + distance + " Miles");
		
		
		
		double point[] =new double[]  {51.507525600000000000, -0.127949599999965360};
		
		double dist  = 100.0;
		
		double[] B=GeoUtil.nextPoint(point, dist, Math.PI);
		
		System.out.println(B[0]);
		System.out.println(B[1]);
		
		
		
	}
	public static String geoHash(double longitude,double latitude,int precision){
		String[] base32 ={"0","1","2","3","4","5","6","7","8","9","b","c","d","e","f","g","h",
				"j","k","m","n","p","q","r","s","t","u","v","w","x","y","z"};
		int[] bits = {16,8,4,2,1};
		double[] long_interval = {-180,180};
		double[] lat_interval = {-90,90};
		int count=0;
		String geocode = new String();
		double latmid =0;
		double longmid =0;
		int bit =0;
		boolean even = true;
		while (precision> geocode.length()){

			if (even){ //if bit is even 
				//System.out.println(long_interval[0]+" "+longmid+" "+long_interval[1] );
				if (longmid < longitude){
					long_interval[0]=longmid;
					count+=bits[bit];

				}
				else{
					long_interval[1]=longmid;

				}
				longmid = (long_interval[0]+long_interval[1])/2;
			}
			else{
				if (latmid < latitude){
					lat_interval[0]=latmid;
					count+=bits[bit];
				}
				else{
					lat_interval[1]=latmid;

				}
				latmid = (lat_interval[0]+lat_interval[1])/2;
			}
			//After 5 bits find the base 32 
			if(bit%4==0 && bit !=0){
				geocode = geocode+base32[count];
				count =0;
				bit =0;
			}
			else{
				bit++;
			}
			even= !even;

		}

		return  geocode;
	}
	
}