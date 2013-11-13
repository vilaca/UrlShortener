package pt.go2.daemon;

import java.util.Date;

public interface WatchDogTask {

	void refresh();
	
	Date lastRun();
	
	long interval();
	
}
