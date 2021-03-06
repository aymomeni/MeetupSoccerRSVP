package com.momeni.meetuprsvp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

@SuppressWarnings("unused")
public class MeetupRestService extends TimerTask {

	private static List<SoccerEvent> _allEvents;
	private static List<SoccerEvent> _allEventsToRsvpToToday;
	private static List<String> _MeetupKeys;
	private static Timer timer;
	private static String _defaultApiKeyForRequests;
	
	// TimerTask implementation from http://www.javapractices.com/topic/TopicAction.do?Id=54
	private final static long fONCE_PER_DAY = 1000 * 60 * 60 * 24; // milliseconds
	
	private final static int fONE_DAY = 1;
	private final static int fSEVEN_AM = 7;
	private final static int fZERO_MINUTES = 0;
	private final static int _minMinutesDelayToRsvp = 1; // for weekday games
	private final static int _maxMinutesDelayToRsvp = 9;
	private final static int _minMilliSecondsDelayToRsvpSaturdays = 100; // for Saturday games
	private final static int _maxMilliSecondsDelayToRsvpSaturdays = 400;
	
	public static void main(String[] args) {
		
		_MeetupKeys = new ArrayList<String>();
		// Meetup API Keys need to be added to the _MeetupKeys Collection

		
		_defaultApiKeyForRequests = _MeetupKeys.get(0);
		
		System.out.println("Starting up... fetching all events");
		getAllUpCommingEvents(_defaultApiKeyForRequests);
		printAllEventsCollection();
		fillAllEventsToRsvpToToday();
		initialCheckForRsvpToday();
		
		//perform the task once a day at 7 a.m., starting tomorrow morning
		Timer timer = new Timer();
		TimerTask CheckEventsTask = new MeetupRestService();
		timer.scheduleAtFixedRate(CheckEventsTask, getTomorrowMorning7am(), fONCE_PER_DAY);
		System.out.println("TimerTask scheduled to run 7am every morning...");
	}

	private static void initialCheckForRsvpToday() {

		if(_allEventsToRsvpToToday.size() > 0) {		
			// grab the lowest event time (First game of day if there is multiple games) TODO: maybe some other way is better
			long lowest = Long.MAX_VALUE;
			SoccerEvent toRsvpToEvent = null;
			
			System.out.println(_allEventsToRsvpToToday.size() + " Events to Rsvp to Today: ");
			for(SoccerEvent sE : _allEventsToRsvpToToday){
				System.out.println(sE.getName() + " " + sE.getId());
				if(sE.getTime() < lowest){
					lowest = sE.getTime();
					toRsvpToEvent = sE;
				}
			}		
			try {
				if(toRsvpToEvent != null) {
					System.out.println("Wainting for Rsvp to open today in " + toRsvpToEvent.getTimeMilliSecondsLeftUntilRSVPOpen() + " milliseconds eventId: " + toRsvpToEvent.getId());
					Thread.sleep(toRsvpToEvent.getTimeMilliSecondsLeftUntilRSVPOpen()+1000); // 1 seconds after the rsvp has opened
				}				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(int i = 0; i < _MeetupKeys.size(); i++) {
				int tries = 0;
				boolean rsvped = false;
				
				// Checking if today is Saturday and if so brute force rsvps else have a more gentle random delay of 1-2 min before an rsvp
				boolean itsASaturdayGame = gameDayIsASaturday(toRsvpToEvent.getLocal_date());
				
				if(!itsASaturdayGame)	{
					int randomDelayInMinutes = (int)(Math.random() * _maxMinutesDelayToRsvp + _minMinutesDelayToRsvp)*1000*60;
					try {
						Thread.sleep((long)randomDelayInMinutes);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				} else {
					int randomDelayMilliSecondsForSaturday = (int)(Math.random() * _maxMilliSecondsDelayToRsvpSaturdays + _minMilliSecondsDelayToRsvpSaturdays);
					try {
						Thread.sleep((long)randomDelayMilliSecondsForSaturday);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				do { 
					rsvped = rsvpToEvent(_MeetupKeys.get(i), toRsvpToEvent.getId()); 
					tries++;
				}
				while(rsvped == false && tries < 3);	
			}
			
		} else { 		
			System.out.println("Nothing to Rsvp to today.");
		}
	}

	@Override
	public void run() {

		getAllUpCommingEvents(_defaultApiKeyForRequests);
		printAllEventsCollection();
		fillAllEventsToRsvpToToday();	
		
		if(_allEventsToRsvpToToday.size() > 0) {		
			
			shuffleMeetupApiKeys();
			
			System.out.println(_allEventsToRsvpToToday.size() + " Events to Rsvp to Today: ");
			
			// grab the lowest event time (First game of day if there is multiple games) TODO: maybe some other way is better
			long lowest = Long.MAX_VALUE;
			SoccerEvent toRsvpToEvent = null;
			for(SoccerEvent sE : _allEventsToRsvpToToday) {
				System.out.println(sE.getName() + " " + sE.getId());
				if(sE.getTime() < lowest) {
					lowest = sE.getTime();
					toRsvpToEvent = sE;
				}
			}		
			try {
				if(toRsvpToEvent != null) {
					System.out.println("Waiting for Rsvp to open today in " + toRsvpToEvent.getTimeMilliSecondsLeftUntilRSVPOpen() + " milliseconds eventId: " + toRsvpToEvent.getId());
					Thread.sleep(toRsvpToEvent.getTimeMilliSecondsLeftUntilRSVPOpen()+1000); // 2 seconds after the rsvp has opened
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
			// Checking if today is Saturday and if so brute force rsvps else have a more gentle random delay of 1-2 min before an rsvp
			boolean itsASaturdayGame = gameDayIsASaturday(toRsvpToEvent.getLocal_date());
				
			for(int i = 0; i < _MeetupKeys.size(); i++) {
				
				int tries = 0;
				boolean rsvped = false;
				
				if(!itsASaturdayGame)	{
					int randomDelayInMinutes = (int)(Math.random() * _maxMinutesDelayToRsvp + _minMinutesDelayToRsvp)*1000*60;
					try {
						Thread.sleep((long)randomDelayInMinutes);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				} else {
					int randomDelayMilliSecondsForSaturday = (int)(Math.random() * _maxMilliSecondsDelayToRsvpSaturdays + _minMilliSecondsDelayToRsvpSaturdays);
					try {
						Thread.sleep((long)randomDelayMilliSecondsForSaturday);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				do {
					rsvped = rsvpToEvent(_MeetupKeys.get(i), toRsvpToEvent.getId()); 
					tries++;
				} while(rsvped == false && tries < 3);
					
				if(rsvped == false && tries == 3) { 
					System.out.println("Rsvp list was full... adding to waitlist."); 
					rsvpToEventWaitlist(_MeetupKeys.get(i), toRsvpToEvent.getId()); 
				}
			}
					
		} else {
			// else do nothing and wait for next day for another check
			System.out.println("No events to Rsvp to today...");		
		}
		System.out.println("TimerTask scheduled to run 7am tomorrow...");
		return;
	}
	
	private void shuffleMeetupApiKeys() {
		Collections.shuffle(_MeetupKeys);
	}

	private static boolean gameDayIsASaturday(String argDate){
		
		// argDate format is expected to be yyyy-MM-dd		
		if(argDate == null || argDate.isEmpty()){
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        Date date;
		
        try {
			date = sdf.parse(argDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		} 
		
        Calendar cal = Calendar.getInstance(); 
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        
        if(day == Calendar.SATURDAY) {
		    return true;
	    }
        
		return false;
	}

	private static void fillAllEventsToRsvpToToday() {		
		_allEventsToRsvpToToday = new LinkedList<SoccerEvent>();
		if(!_allEvents.isEmpty()) {
			for(SoccerEvent sE : _allEvents) {	
				if(sE.getTimeMilliSecondsLeftUntilRSVPOpen() < fONCE_PER_DAY && sE.getTimeMilliSecondsLeftUntilRSVPOpen() > 0) {
					_allEventsToRsvpToToday.add(sE);
				}
			}
		}	
		return;
	}

	private static Date getTomorrowMorning7am() {
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
	
	private static void getAllUpCommingEvents(String apiKey) {
		
		_allEvents = new LinkedList<SoccerEvent>();
		
		try {		
			System.out.println("getAllUpCommingEvents() => Connecting to server...");
			URL url = new URL("https://api.meetup.com/Cottonwood-Co-Ed-Adult-Soccer/events?key=" + apiKey);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if(conn.getResponseCode() != 200) {			
				throw new RuntimeException("Failed : Http error code : " 
					+ conn.getResponseCode());
			}
			
			System.out.println("getAllUpCommingEvents() => Server responded with 200 OK");

			BufferedReader br = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));

			StringBuilder sB = new StringBuilder();
			String output = "";		
			
			while((output = br.readLine()) != null) {
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
			System.out.println("getAllUpCommingEvents() => Successfully processed Server response (incl. filling of internal objects)");
			
		} catch(Exception e) {
			System.out.println("Something failed in getAllUpCommingEvents()");
			e.printStackTrace();
		}
	}
	
	private static SoccerEvent fillSoccerEventObjectFromJSONObject(JSONObject event) {
		
		SoccerEvent soccEvent = null;
		try{
			
			if(event.has("id") && event.has("name") && event.has("time") && event.has("local_date") && event.has("local_time") && event.has("rsvp_open_offset")) {
				soccEvent = new SoccerEvent();
				soccEvent.setId(event.getString("id"));
				soccEvent.setTime(event.getLong("time"));
				soccEvent.setName(event.getString("name"));
				soccEvent.setLocal_date(event.getString("local_date"));
				soccEvent.setLocal_time(event.getString("local_time"));
				soccEvent.setRsvp_open_offset(event.getString("rsvp_open_offset"));
				soccEvent.setTimeMilliSecondsLeftUntilRSVPOpen();
				if(soccEvent.getTimeMilliSecondsLeftUntilRSVPOpen() == -1){ return null; } // in case anything goes wrong with rsvp_open_offset parsing
			} else {
				return null;
			}
		} catch(JSONException e) {
			System.out.println("Error in filling the soccEvent Object");
			e.printStackTrace();
		}
		return soccEvent;
	}
	
	private static boolean rsvpToEvent(String key, String eventId) {
		
		try {	
			System.out.println("rsvpToEvent() => Connecting to server...");
			URL url = new URL("https://api.meetup.com/2/rsvp/?event_id="+eventId+"&rsvp=yes&key="+key);
			System.out.println(url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			OutputStream os = conn.getOutputStream();
			os.flush();
			
			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {	// 3 recursive tries
				System.out.println("Failed : Http error code " + conn.getResponseCode());
				conn.disconnect();
				return false;
			}
			
			System.out.println("rsvpToEvent() => Server responded with 201 Created => eventId " + eventId);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server ....");
			
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			System.out.println("");
			conn.disconnect();
			return true;

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		} catch (Exception e) {			
			e.printStackTrace();
			return false;
		}
	}
	
	private static void printAllEventsCollection() {
		
		if(_allEvents.isEmpty()) {
			return;
		}		
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		for(SoccerEvent s : _allEvents) {
			System.out.println(s.getName() + "  " + s.getLocal_date() + " "+ s.getId() + " " + s.getRsvp_open_offset() + " " + s.getTime() + " " + s.getMilliSecondsFromRsvpOffset() + " Time left till RSVP: " + s.getTimeMilliSecondsLeftUntilRSVPOpen());
		}
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
		System.out.println("================================================================================================================================");
	}
	
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
			System.out.println("Output from Server ....");
			
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			System.out.println("");
			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	}
}