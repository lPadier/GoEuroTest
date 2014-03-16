import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.RuntimeException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class GoEuroTest {
	public static JSONObject getJSON(String addr) {
		String s = "https://api.goeuro.com/api/v1/suggest/position/en/name/";
		try {
			s += URLEncoder.encode(addr, "UTF-8");
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
		catch (ParseException e)				{System.out.println("ParseException : " + e);}
		catch (UnsupportedEncodingException e)	{System.out.println("UnsupportedEncodingException : " + e);}
		catch (MalformedURLException e) 		{System.out.println("MalformedURLException : " + e);}
		catch (IOException e) 					{System.out.println("IOException : " + e);}
		throw new RuntimeException("Error while getting JSON");
	}
	
	public static String jsonGet(String name, JSONObject resultJsonObject, Class<?> cls) {
		Object result=resultJsonObject.get(name);
		if (cls == String.class) {
			return (result instanceof String) ? "\""+(String) result+"\"" : "null";
		} else if (cls == Number.class) {
			return (result instanceof Number) ? String.valueOf(result) : "null";
		} else {
			return null;
		}
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
						String _type, _id, name, type, latitude, longitude;
						
						_type = jsonGet("_type",resultJsonObject,String.class);
						_id   = jsonGet("_id",resultJsonObject,Number.class);
						name  = jsonGet("name",resultJsonObject,String.class);
						type  = jsonGet("type",resultJsonObject,String.class);

						if (resultJsonObject.get("geo_position") instanceof JSONObject) {
							JSONObject geoPosition = (JSONObject) resultJsonObject.get("geo_position");
							latitude  = jsonGet("latitude",geoPosition,Number.class);
							longitude = jsonGet("longitude",geoPosition,Number.class);
						} else {
								latitude="null";
								longitude="null";
							}
						writer.write(_type + " , " + _id + " , " + name + " , " + type + " , " + latitude + " , " + longitude);
					}
				}
				writer.close();
				System.out.println("Contents written to output.csv");
			}
			catch (FileNotFoundException e) 		{System.out.println("FNFE" + e);}
			catch (UnsupportedEncodingException e)	{System.out.println("UEE");}
			catch (MalformedURLException e) 		{System.out.println("MUE");}
			catch (IOException e) 					{System.out.println("IOE" + e);}
		} else {
			if (jsonObject.get("results") == null) {
				// results is emply
				System.out.println("There are no results");
			} else {
				// results is neither emply nor an array
				System.out.println("results is neither empty nor an array");
			}
		}
	}

	public static void main(String[] args) {
		if (args.length==1) {
			try {
				JSONObject jsonObject=getJSON(args[0]);
				writeCSV(jsonObject);
			}
			catch (RuntimeException e) {
				System.out.println(e);
				return;
			}
		} else {
			System.out.println("this program takes one and only one argument.");
		}
	}
}