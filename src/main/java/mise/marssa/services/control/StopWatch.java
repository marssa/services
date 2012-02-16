/**
 * 
 */
package mise.marssa.services.control;

import mise.marssa.footprint.datatypes.time.ATime;
import mise.marssa.footprint.datatypes.time.Seconds;
import mise.marssa.footprint.logger.MMarker;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class StopWatch {
	private static Logger stopWatchLogger = (Logger) LoggerFactory.getLogger(StopWatch.class);
	private long start;
	public void start(){
		stopWatchLogger.trace("Started StopWatch");
		start = System.currentTimeMillis();
	}

	public ATime stop(){
		long now = System.currentTimeMillis();
		 long time = ( (now - start)/1000);
		 stopWatchLogger.trace(MMarker.GETTER,"Returning time in seconds");
		 return new Seconds(time);
	}
}

