package com.momeni.meetuprsvp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoccerEvent {
	
	private String id;
	private String name;
	private long created;
	private long time; // time in milliseconds epoch
	private String status;
	private String local_date;
	private String local_time;
	private String rsvp_open_offset = null; // Format e.g. PT151H30M -> Prior to event by 151 hours and 30 min
	private boolean rsvp_boolean; // true if rsvp is yes, false otherwise
	private boolean waitlisted; // if waitlisted true else false
	
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
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
		return rsvp_open_offset;
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
	public boolean isRsvp_boolean() {
		return rsvp_boolean;
	}
	public void setRsvp_boolean(boolean rsvp_boolean) {
		this.rsvp_boolean = rsvp_boolean;
	}
	public boolean isWaitlisted() {
		return waitlisted;
	}
	public void setWaitlisted(boolean waitlisted) {
		this.waitlisted = waitlisted;
	}
	
	public long getSecondsPriorToGameTime(){
		
		return this.regexMatcherForPT();
		
	}
	
	public long regexMatcherForPT(){
		
		if(this.rsvp_open_offset == null){
			return -1;
		}
		
		long seconds;
		
		String pattern = "PT(?<hours>\\d+)H(?<minutes>\\d+)*M?";
		String hours = null;
		String minutes = null;
		int Ihours = 0;
		int Iminutes = 0;
		
		// Create a Pattern object
	      Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
	      Matcher m = r.matcher(rsvp_open_offset);
	      if (m.find( )) {

	    	 hours = m.group("hours");
	    	 minutes = m.group("minutes"); 	  

	    	  	    	  
	      } else {
	         System.out.println("NO MATCH");
	      }
	      
	      if(hours != null && minutes != null){
	    	  return Long.parseLong(hours)*60*60 + Long.parseLong(minutes)*60;
	      } else if(hours != null && minutes == null){
	    	  return Long.parseLong(hours)*60*60;
	      }
	      
	      return -1;
	
	}
	
	
}
