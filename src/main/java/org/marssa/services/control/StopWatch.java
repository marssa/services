/**
 * Copyright 2012 MARSEC-XL International Limited
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
package org.marssa.services.control;

import org.marssa.footprint.datatypes.time.ATime;
import org.marssa.footprint.datatypes.time.Seconds;
import org.marssa.footprint.logger.MMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopWatch {
	private static Logger stopWatchLogger = LoggerFactory
			.getLogger(StopWatch.class);
	private long start;

	public void start() {
		stopWatchLogger.trace("Started StopWatch");
		start = System.currentTimeMillis();
	}

	public ATime stop() {
		long now = System.currentTimeMillis();
		long time = ((now - start) / 1000);
		stopWatchLogger.trace(MMarker.GETTER, "Returning time in seconds");
		return new Seconds(time);
	}
}
