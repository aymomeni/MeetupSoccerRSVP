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

public class MeetupRestService extends TimerTask {

	private static List<SoccerEvent> _allEvents;
	private static String _KEY = "MEETUPKEYS";
	private static Timer timer;
	
	
	// TimerTask implementation from http://www.javapractices.com/topic/TopicAction.do?Id=54
	// milliseconds
	private final static long fONCE_PER_DAY = 1000 * 60 * 60 * 24;
	
	private final static int fONE_DAY = 1;
	private final static int fSEVEN_AM = 7;
	private final static int fZERO_MINUTES = 0;
	
	public static void main(String[] args) {

		readKeyFromFile();
		
		TimerTask CheckEventsTask = new MeetupRestService();
		//perform the task once a day at 7 a.m., starting tomorrow morning
	    //(other styles are possible as well)
		timer.scheduleAtFixedRate(CheckEventsTask, getTomorrowMorning7am(), fONCE_PER_DAY);
		//rsvpToEvent(_KEY, "eventId");
		//rsvpToEventWaitlist(_KEY, "bbxnrqyzfbmb");		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		getAllUpCommingEvents();
		printAllEventsCollection();
		checkIfAnythingToRSVPToday();
		// find current time in epoch
		// check if there is any events who's rsvp is open today
		// if yes wait a given amount of time and then rsvp to the event
		// if no don't do anything
	}
		
	private boolean checkIfAnythingToRSVPToday() {
		
		Date date = new Date();
		long currentTimeEpochMilli = date.getTime();
		
		
		
		
		return false;
	}

	private static Date getTomorrowMorning7am(){
		Calendar tomorrow = new GregorianCalendar();
		tomorrow.add(Calendar.DATE, fONE_DAY);
		Calendar result = new GregorianCalendar(
				tomorrow.get(Calendar.YEAR),
				tomorrow.get(Calendar.MONTH),
				tomorrow.get(Calendar.DATE),
				fSEVEN_AM,
				fZERO_MINUTES
				
		);		
		return result.getTime();	
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

	
	private static void getAllUpCommingEvents(){
		
		_allEvents = new LinkedList<SoccerEvent>();
		
		try{
			
			System.out.println("getAllUpCommingEvents() => Connecting to server...");
			URL url = new URL("https://api.meetup.com/Cottonwood-Co-Ed-Adult-Soccer/events?key=" + _KEY);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if(conn.getResponseCode() != 200){
				throw new RuntimeException("Failed : Http error code : " 
					+ conn.getResponseCode());
			}
			
			System.out.println("getAllUpCommingEvents() => Server responded with 200 OK");

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
				SoccerEvent sE = fillSoccerEventObjectFromJSONObject(event);				
				if(sE == null){
					continue;
				} else{
					_allEvents.add(sE);
				}				
			}			
			System.out.println("getAllUpCommingEvents() => Successfully processed Server response");
			
		} catch(Exception e){
			System.out.println("Something failed in getAllUpCommingEvents()");
			e.printStackTrace();
		}
	}
	

	private static SoccerEvent fillSoccerEventObjectFromJSONObject(JSONObject event){
		
		SoccerEvent soccEvent = null;
		try{
		
			if(event.has("id") && event.has("name") && event.has("time") && event.has("local_date") && event.has("local_time") && event.has("rsvp_open_offset")){
				soccEvent = new SoccerEvent();
				soccEvent.setId(event.getString("id"));
				soccEvent.setTime(event.getLong("time"));
				soccEvent.setName(event.getString("name"));
				soccEvent.setLocal_date(event.getString("local_date"));
				soccEvent.setLocal_time(event.getString("local_time"));
				soccEvent.setRsvp_open_offset(event.getString("rsvp_open_offset"));
			} else{
				return null;
			}
		} catch(JSONException e){
			System.out.println("Error in filling the soccEvent Object");
			e.printStackTrace();
		}
		return soccEvent;
	}
	
	@SuppressWarnings("unused")
	private static void rsvpToEventWaitlist(String key, String eventId) {
		
		try {	
			URL url = new URL("https://api.meetup.com/2/rsvp/?event_id="+eventId+"&rsvp=waitlist&key="+key);
			System.out.println(url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			
//	CODE BELOW IS WHEN POST INCLUDES A JSON PAYLOAD
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
	
	@SuppressWarnings("unused")
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
	
	private static void printAllEventsCollection(){
		
		if(_allEvents.isEmpty()){
			return;
		}		
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		for(SoccerEvent s : _allEvents){
			System.out.println(s.getName() + "  " + s.getLocal_date() + " "+ s.getId() + " " + s.getRsvp_open_offset() + " " + s.getTime() + " " + s.getSecondsPriorToGameTime());
		}
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
	}
}