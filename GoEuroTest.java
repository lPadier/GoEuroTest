import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.lang.RuntimeException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;


public class GoEuroTest {
	public static JSONObject getJSON(String addr) {
		// String s = "https://api.goeuro.com/api/v1/suggest/position/en/name/";
		String s = "http://192.168.0.16/";
		try {
			s += URLEncoder.encode(addr, "UTF-8");
			System.out.println(s);
			URL url = new URL(s);
			Scanner scan = new Scanner(url.openStream());
  

			String str = new String();
			while (scan.hasNext()) {
				str += scan.nextLine();
			}
			scan.close();
			JSONParser parser = new JSONParser();
			Object obj=parser.parse(str);
			return (JSONObject) obj;
		}
		catch (ParseException e)				{System.out.println("PE " + e);}
		catch (UnsupportedEncodingException e)	{System.out.println("UEE");}
		catch (MalformedURLException e) 		{System.out.println("MUE");}
		catch (IOException e) 					{System.out.println("IOE" + e);}
		throw new RuntimeException();
	}
	
	public static void writeCSV (JSONObject jsonObject) {
		if (jsonObject.get("results") instanceof JSONArray) {
			
			JSONArray results=(JSONArray) jsonObject.get("results");
			String path = "./output.csv";
			try {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
				writer.write("_type , _id , name , type , latitude , longitude");
				for (Object resultObject:results) {
					if (resultObject instanceof JSONObject) {
						writer.newLine();
						JSONObject resultJsonObject = (JSONObject) resultObject;
						String _type, id, name, type, latitude, longitude;
						
						_type = (resultJsonObject.get("_type") instanceof String) ? "\""+(String) resultJsonObject.get("_type")+"\"" : "null";
						id = (resultJsonObject.get("_id") instanceof Number) ? String.valueOf( resultJsonObject.get("_id")) : "null";
						name = (resultJsonObject.get("name") instanceof String) ? "\""+(String) resultJsonObject.get("name")+"\"" : "null";
						type = (resultJsonObject.get("type") instanceof String) ? "\""+(String) resultJsonObject.get("type")+"\"" : "null";

						if (resultJsonObject.get("geo_position") instanceof JSONObject) {
							JSONObject geoPosition = (JSONObject) resultJsonObject.get("geo_position");
							if ( geoPosition.get("latitude") instanceof Number) {
								latitude = String.valueOf( geoPosition.get("latitude"));
								longitude = String.valueOf( geoPosition.get("longitude"));
							}
							else {
								latitude="null";
								longitude="null";
							}
						}else {
								latitude="null";
								longitude="null";
							}
					
						writer.write(_type + " , " + id + " , " + name + " , " + type + " , " + latitude + " , " + longitude);
					}
				}
				writer.close();
			}
			catch (FileNotFoundException e) 		{System.out.println("FNFE" + e);}
			catch (UnsupportedEncodingException e)	{System.out.println("UEE");}
			catch (MalformedURLException e) 		{System.out.println("MUE");}
			catch (IOException e) 					{System.out.println("IOE" + e);}
		}
		else { //results is empty
			System.out.println("There are no results");
		}
	}

	public static void main(String[] args) {
		if (args.length==1) {
			try {
				JSONObject jsonObject=getJSON(args[0]);
				writeCSV(jsonObject);
			}
			catch (RuntimeException e) {
				System.out.println("Error while getting JSON." + e);
				return;
			}
		} else {
			System.out.println("this program takes one and only one argument.");
		}
	}
}