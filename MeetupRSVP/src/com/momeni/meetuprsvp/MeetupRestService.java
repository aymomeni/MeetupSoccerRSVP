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
	private static List<SoccerEvent> _allEventsToRSVPToday;
	private static List<String> _MeetupKeys;
	private static Timer timer;
	
	
	// TimerTask implementation from http://www.javapractices.com/topic/TopicAction.do?Id=54
	private final static long fONCE_PER_DAY = 1000 * 60 * 60 * 24; // milliseconds
	
	private final static int fONE_DAY = 1;
	private final static int fSEVEN_AM = 7;
	private final static int fZERO_MINUTES = 0;
	
	public static void main(String[] args) {
		
		_MeetupKeys = new ArrayList<String>();
		_MeetupKeys.add("7e454df4306b31417030a7e5f582b");
		
		getAllUpCommingEvents();
		printAllEventsCollection();
		
		//TimerTask CheckEventsTask = new MeetupRestService(); //TODO UNCOMMENT
		
		//perform the task once a day at 7 a.m., starting tomorrow morning
	    //(other styles are possible as well)
		
		//Timer timer = new Timer(); //TODO UNCOMMENT
		//timer.scheduleAtFixedRate(CheckEventsTask, getTomorrowMorning7am(), fONCE_PER_DAY); //TODO UNCOMMENT
	}


	@SuppressWarnings("unused")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		getAllUpCommingEvents();
		printAllEventsCollection();
		boolean eventsToRSVPTo = checkIfAnythingToRSVPToday();
		
		if(eventsToRSVPTo){		
			// grab the lowest event time (First game of day if there is multiple games) TODO: maybe some other way is better
			long lowest = Long.MAX_VALUE;
			SoccerEvent toRSVPToEvent = null;
			for(SoccerEvent sE : _allEventsToRSVPToday){
				if(sE.getTime() < lowest){
					lowest = sE.getTime();
					toRSVPToEvent = sE;
				}
			}		
			try {
				if(toRSVPToEvent != null){
					Thread.sleep(toRSVPToEvent.getTimeMilliSecondsLeftUntilRSVPOpen()+5000); // 5 seconds after the rsvp has opened
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int i = 0; i < _MeetupKeys.size(); i++){
				rsvpToEvent(_MeetupKeys.get(i), toRSVPToEvent.getId());	
			}
			
		} 
		// else do nothing and wait for next day for another check
	}
	
// Run method to roughly test this class	
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		getAllUpCommingEvents();
//		printAllEventsCollection();
//		boolean eventsToRSVPTo = true; //checkIfAnythingToRSVPToday();
//		
//		if(eventsToRSVPTo){
//			
//			// grab the lowest event time TODO: maybe some other way is better
//			long lowest = Long.MAX_VALUE;
//			SoccerEvent toRSVPTOEvent = new SoccerEvent();
//			toRSVPTOEvent.setId("bbxnrqyzfbmb");
//			toRSVPTOEvent.setTime(1552147200000l);
//			_allEventsToRSVPToday = new LinkedList<SoccerEvent>();
//			_allEventsToRSVPToday.add(toRSVPTOEvent);
//			for(SoccerEvent sE : _allEventsToRSVPToday){
//				if(sE.getTime() < lowest){
//					lowest = sE.getTime();
//				}
//			}		
//			try {
//				if(toRSVPTOEvent != null){
//					Thread.sleep(5000);
//					System.out.println("Waited");
//				}				
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			rsvpToEventWaitlist(_KEY, toRSVPTOEvent.getId());	
//		}
//		// else do nothing
//	}
		
	private boolean checkIfAnythingToRSVPToday() {
		
		Date date = new Date();
		long currentTimeEpochMilliSeconds = date.getTime();
		_allEventsToRSVPToday = new LinkedList<SoccerEvent>();
		
		for(SoccerEvent sE : _allEvents){
			long timeMilliSecondsLeftUntilRsvpOpen = (sE.getTime() - sE.getMilliSecondsPTGameTimeOpening()) - currentTimeEpochMilliSeconds;			
			if(timeMilliSecondsLeftUntilRsvpOpen < fONCE_PER_DAY){
				sE.setTimeMilliSecondsLeftUntilRSVPOpen(timeMilliSecondsLeftUntilRsvpOpen);
				_allEventsToRSVPToday.add(sE);
			}
		}
	
		return !_allEventsToRSVPToday.isEmpty();
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

//	private static void readKeyFromFile() {
//		
//		try {
//			BufferedReader buffFileReader;
//			URL path = MeetupRestService.class.getResource("com/momeni/meetuprsvp/keys.txt");
//			File file = new File(path.getFile()); 
//			buffFileReader = new BufferedReader(new FileReader(file));
//			String st; 
//			  try {
//				while ((st = buffFileReader.readLine()) != null) 
//				  {
//					  if(st.isEmpty() || st == null){
//						  System.out.println("Key file corrupt");
//						  System.exit(-1);
//					  }
//					  _KEY = st; 
//				  }
//			} catch (IOException e) {
//				e.printStackTrace();
//			} 
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} 		 
//		  
//		_KEY = _KEY.trim();
//	}

	
	private static void getAllUpCommingEvents(){
		
		_allEvents = new LinkedList<SoccerEvent>();
		
		try{		
			System.out.println("getAllUpCommingEvents() => Connecting to server...");
			URL url = new URL("https://api.meetup.com/Cottonwood-Co-Ed-Adult-Soccer/events?key=" + _MeetupKeys.get(0));

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
			System.out.println("rsvpToEventWaitlist() => Sending Post...");
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
			System.out.println("rsvpToEventWaitlist() => 201 Created");
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
			System.out.println(s.getName() + "  " + s.getLocal_date() + " "+ s.getId() + " " + s.getRsvp_open_offset() + " " + s.getTime() + " " + s.getMilliSecondsPTGameTimeOpening());
		}
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
	}
}