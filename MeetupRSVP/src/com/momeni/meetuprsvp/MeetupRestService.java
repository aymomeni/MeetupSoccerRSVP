package com.momeni.meetuprsvp;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.*;

public class MeetupRestService {

	private static List<SoccerEvent> _allEvents;
	private static String _KEY = "MEETUPKEYS";
	private static Timer _dailyTimer;
	private static int _milliSecondsInADay;
	
	public MeetupRestService() {
		
	}	
	
	public static void main(String[] args) {

		readKeyFromFile();
		//setUpDailyTaskTimer();
		startUp();
		
		for(SoccerEvent s : _allEvents){
			System.out.println(s.getName() + "  " + s.getLocal_date() + " "+ s.getId() + " " + s.getRsvp_open_offset() + " " + s.getTime() + " " + s.getSecondsPriorToGameTime());
		}
		//rsvpToEvent(_KEY, "eventId");
		//rsvpToEventWaitlist(_KEY, "bbxnrqyzfbmb");
		
		//new MeetupRestService();
		//System.out.println("TaskScheduled");
		
	}
	
	private static void setUpDailyTaskTimer() {
		_dailyTimer = new Timer();
		_dailyTimer.schedule(new CheckEventsTask(), _milliSecondsInADay); // second argument in milliseconds
	}
	
	static class CheckEventsTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Do what needs to be done daily
			printSummaryOfNextEightEvents(_KEY);
			_dailyTimer.cancel();
			_dailyTimer = new Timer();
			_dailyTimer.schedule(new CheckEventsTask(), _milliSecondsInADay);
		}
	}

	private static void readKeyFromFile() {
		
		try {
			BufferedReader buffFileReader;
			URL path = MeetupRestService.class.getResource("keys.txt");
			File file = new File(path.getFile()); 
			buffFileReader = new BufferedReader(new FileReader(file));
			String st; 
			  try {
				while ((st = buffFileReader.readLine()) != null) 
				  {
					  if(st.isEmpty() || st == null){
						  System.out.println("Key file corrupt");
						  System.exit(-1);
					  }
					  _KEY = st; 
				  }
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} 		 
		  
		_KEY = _KEY.trim();
	}

	public static void startUp(){
		
		try{
			_allEvents = new LinkedList<SoccerEvent>();
			_milliSecondsInADay = 24*60*60*1000;
			
			System.out.println("Startup: Connecting to server...");
			URL url = new URL("https://api.meetup.com/Cottonwood-Co-Ed-Adult-Soccer/events?key=" + _KEY);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if(conn.getResponseCode() != 200){
				throw new RuntimeException("Failed : Http error code : " 
					+ conn.getResponseCode());
			}
			System.out.println("Startup: Connecting Successful...");

			BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));

			StringBuilder sB = new StringBuilder();
			String output = "";		
			
			while((output = br.readLine()) != null){
				sB.append(output);
			}

			conn.disconnect();
			
			JSONArray arr = new JSONArray(sB.toString());
						
			for(int i = 0; i < arr.length(); i++){
				
				JSONObject event = arr.getJSONObject(i);
				
				SoccerEvent sE = fillSoccerBeanFromJSON(event);
				_allEvents.add(sE);
			}

		} catch(IOException e){

			e.printStackTrace();
		}
		
	}
	
	private static SoccerEvent fillSoccerBeanFromJSON(JSONObject event){
		
		SoccerEvent soccEvent = new SoccerEvent();
		
		soccEvent.setId(event.getString("id"));
		soccEvent.setLocal_date(event.getString("local_date"));
		soccEvent.setLocal_time(event.getString("local_time"));
		soccEvent.setTime(event.getLong("time"));
		soccEvent.setName(event.getString("name"));
		try{
			soccEvent.setRsvp_open_offset(event.getString("rsvp_open_offset"));
		}catch(JSONException e){
			soccEvent.setRsvp_open_offset(null);
		}
		
		return soccEvent;
	}
	
	private static void rsvpToEventWaitlist(String key, String eventId) {
		
		try {	
			URL url = new URL("https://api.meetup.com/2/rsvp/?event_id="+eventId+"&rsvp=waitlist&key="+key);
			System.out.println(url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

//			String input = "{\"event_id\"="+eventId+",\"rsvp\"=\"yes\"}";
//			OutputStream os = conn.getOutputStream();
//			os.write(input.getBytes());
//			os.flush();
			
			OutputStream os = conn.getOutputStream();
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	  
	}
	
	private static void rsvpToEvent(String key, String eventId) {
		
		try {	
		
			URL url = new URL("https://api.meetup.com/2/rsvp/?event_id="+eventId+"&rsvp=yes&key="+key);
			System.out.println(url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	  
	}

	
	// Prints a short summary of upcoming events
	public static void printSummaryOfNextEightEvents(String key){
		
		try{

			System.out.println("Client side request to meetup... ");
			URL url = new URL("https://api.meetup.com/Cottonwood-Co-Ed-Adult-Soccer/events?key=" + key);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if(conn.getResponseCode() != 200){
				throw new RuntimeException("Failed : Http error code : " 
					+ conn.getResponseCode());
			}

			System.out.println("Response successfully received");

			BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));

			StringBuilder sB = new StringBuilder();
			String output = "";
			System.out.println("Response from Server .... \n");
			
			
			while((output = br.readLine()) != null){
				sB.append(output);
			}

			conn.disconnect();
			
			JSONArray arr = new JSONArray(sB.toString());
			
			for(int i = 0; i < 8; i++){
				JSONObject event = arr.getJSONObject(i);
				JSONObject venue = event.getJSONObject("venue");
				System.out.println("ID: " + event.getString("id") + " Date: " + event.getString("local_date") + " Time: " + event.getString("local_time") + " VenueName: " + venue.getString("name") 
						+ " RSVPOpenOffset: " + event.getString("rsvp_open_offset"));
				
				System.out.println();
			}

		} catch(IOException e){

			e.printStackTrace();
		}
	}
}