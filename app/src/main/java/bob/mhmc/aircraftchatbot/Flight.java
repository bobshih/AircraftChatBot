package bob.mhmc.aircraftchatbot;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Bob on 2017/6/1.
 */

public class Flight {
	private String airline;
	private int stop;
	private int price;
	private int durationSecond;
	private String departTime;
	private String arriveTime;

	public Flight(String depart, String arrive, String airline, String stop, String price, String duration) {
		this.departTime = depart;
		this.arriveTime = arrive;
		this.airline = airline;
		this.price = Integer.valueOf(price.replace("NT$", "").replace(",", ""));

		if(stop.equals("Nonstop")){
			this.stop = 0;
		}else {
			this.stop = Integer.valueOf(stop.split(" ")[0]);
		}

		String[] temp = duration.split(" ");
		this.durationSecond = 0;
		for (String s:temp){
			if(s.contains("h")){
				int i = Integer.valueOf(s.replace("h",""));
				this.durationSecond += i*60*60;
			} else if (s.contains("m")){
				int i = Integer.valueOf(s.replace("m",""));
				this.durationSecond += i*60;
			}
		}
	}

	public String getAirline() {
		return airline;
	}

	public int getStop() {
		return stop;
	}

	public int getPrice() {
		return price;
	}

	public int getDurationSecond() {
		return durationSecond;
	}

	public String getDepartTimeString(){
		return departTime;
	}
	public Date getDepartTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
		try {
			return sdf.parse(departTime);
		}catch (Exception e){
			e.printStackTrace();
		}
		return sdf.get2DigitYearStart();
	}

	public Date getArriveTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
		try {
			return sdf.parse(arriveTime);
		}catch (Exception e){
			e.printStackTrace();
		}
		return sdf.get2DigitYearStart();
	}

	public String getResponseString(){
		return String.format(Locale.TAIWAN, "the %s flight depart at %s, and the ticket costs NT$ %d", airline, departTime, price);
	}

	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof Flight)){
			return false;
		}
		Flight f = (Flight)obj;
		return (airline.equals(f.getAirline()) & price == f.getPrice() & departTime.equals(f.getDepartTimeString()));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
				// if deriving: appendSuper(super.hashCode()).
						append(airline).
						append(price).
						append(departTime).
						toHashCode();
	}
}
