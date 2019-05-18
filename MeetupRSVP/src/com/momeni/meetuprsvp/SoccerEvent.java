package com.momeni.meetuprsvp;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoccerEvent {
	
	private String id;
	private String name;
	private long time; // time in milliseconds epoch
	private String local_date = null;
	private String local_time = null;
	private String rsvp_open_offset = null; // Format e.g. PT151H30M -> Prior to event by 151 hours and 30 min
	private long timeMilliSecondsLeftUntilRsvpOpen = -1;
	
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
	public String getLocal_date() {
		return local_date;
	}
	public void setLocal_date(String local_date) {
		this.local_date = local_date;
	}
	public String getLocal_time() {
		return local_time;
	}
	public void setLocal_time(String local_time) {
		this.local_time = local_time;
	}
	public String getRsvp_open_offset() {
		return this.rsvp_open_offset;
	}
	public void setRsvp_open_offset(String rsvp_open_offset) {
		this.rsvp_open_offset = rsvp_open_offset;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public long getTimeMilliSecondsLeftUntilRSVPOpen(){
		return this.timeMilliSecondsLeftUntilRsvpOpen;
	}
	public void setTimeMilliSecondsLeftUntilRSVPOpen(){
		
		Date date = new Date();
		long currentTimeEpochMilliSeconds = date.getTime();
		
		if(this.getMilliSecondsFromRsvpOffset() == -1){
			return;
		}

		this.timeMilliSecondsLeftUntilRsvpOpen = this.getTime() - this.getMilliSecondsFromRsvpOffset() - currentTimeEpochMilliSeconds;	
	}
	
	// if anything goes wrong in the parsing of rsvp_open_offset returns -1
	public long getMilliSecondsFromRsvpOffset(){
		
		if(this.rsvp_open_offset == null){
			return -1;
		}
		
		String pattern = "PT(?<hours>\\d+)H(?<minutes>\\d+)*M?";
		String hours = null;
		String minutes = null;
		
		// Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(rsvp_open_offset);
	      if (m.find( )) {
	    	 hours = m.group("hours");
	    	 minutes = m.group("minutes"); 	  
	      } else {
	         System.out.println("NO MATCH!");
	      }
	      
	      if(hours != null && !hours.isEmpty() && minutes != null && !minutes.isEmpty()){
	    	  return (Long.parseLong(hours)*60*60 + Long.parseLong(minutes)*60)*1000;
	      } else if(hours != null && !hours.isEmpty() && minutes == null){
	    	  return (Long.parseLong(hours)*60*60)*1000;
	      } else if(hours != null && !hours.isEmpty() && minutes.isEmpty()){
	    	  return (Long.parseLong(hours)*60*60)*1000;
	      }
	      
	      return -1;
	
	}
}
