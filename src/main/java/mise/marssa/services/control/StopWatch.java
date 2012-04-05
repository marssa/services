/**
 * Copyright 2012 MARSEC-XL Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

