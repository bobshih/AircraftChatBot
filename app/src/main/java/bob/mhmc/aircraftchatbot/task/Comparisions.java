package bob.mhmc.aircraftchatbot.task;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import bob.mhmc.aircraftchatbot.Flight;

/**
 * Created by Bob on 2017/6/1.
 */

public class Comparisions {
	public static Comparator<Flight> cheapComparision = new Comparator<Flight>() {
		@Override
		public int compare(Flight o1, Flight o2) {
			if(o1.getPrice() > o2.getPrice()){
				return 1;
			} else if(o1.getPrice() < o2.getPrice()){
				return -1;
			}
			return 0;
		}
	};

	public static Comparator<Flight> expensiveComparision = new Comparator<Flight>() {
		@Override
		public int compare(Flight o1, Flight o2) {
			if(o1.getPrice() < o2.getPrice()){
				return 1;
			} else if(o1.getPrice() > o2.getPrice()){
				return -1;
			}
			return 0;
		}
	};

	public static Comparator<Flight> shortComparision = new Comparator<Flight>() {
		@Override
		public int compare(Flight o1, Flight o2) {
			if(o1.getDurationSecond() > o2.getDurationSecond()){
				return 1;
			} else if(o1.getDurationSecond() < o2.getDurationSecond()){
				return -1;
			}
			return 0;
		}
	};

	public static Comparator<Flight> lateComparision = new Comparator<Flight>() {
		@Override
		public int compare(Flight o1, Flight o2) {
			try {
				Date date1 = o1.getDepartTime();
				Date date2 = o2.getDepartTime();

				if(date1.before(date2)){
					return 1;
				} else if(date2.before(date1)){
					return -1;
				} else {
					return 0;
				}
			} catch (Exception e){
				e.printStackTrace();
				return 0;
			}
		}
	};

	public static Comparator<Flight> earlyComparision = new Comparator<Flight>() {
		@Override
		public int compare(Flight o1, Flight o2) {
			try {
				Date date1 = o1.getDepartTime();
				Date date2 = o2.getDepartTime();

				if(date1.before(date2)){
					return -1;
				} else if(date2.before(date1)){
					return 1;
				} else {
					return 0;
				}
			} catch (Exception e){
				e.printStackTrace();
				return 0;
			}
		}
	};
}
