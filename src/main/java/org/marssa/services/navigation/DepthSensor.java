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
package org.marssa.services.navigation;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

import org.marssa.footprint.datatypes.decimal.distance.ADistance;
import org.marssa.footprint.datatypes.decimal.distance.Metres;
import org.marssa.footprint.datatypes.decimal.temperature.ATemperature;
import org.marssa.footprint.datatypes.decimal.temperature.DegreesCelcius;
import org.marssa.footprint.exceptions.OutOfRange;
import org.marssa.footprint.interfaces.navigation.IDepthSensor;
import org.marssa.footprint.logger.MMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Warren Zahra
 * 
 */
public class DepthSensor implements IDepthSensor, SentenceListener {
	private static final Logger logger = LoggerFactory
			.getLogger(DepthSensor.class.getName());

	// TODO check if reader variable is really required
	private final SentenceReader reader;

	Metres depthMeters = null;
	DegreesCelcius temperatureDegrees = null;

	public DepthSensor(SentenceReader reader) {
		this.reader = reader;
		reader.addSentenceListener(this, "MTW");
		reader.addSentenceListener(this, SentenceId.DBT);
		reader.addSentenceListener(this, SentenceId.DPT);
		String[] sentenceIDs = { "MTW", "DBT", "DPT" };
		logger.info(
				"A depth sensor with the following Sentence id is instantiated",
				sentenceIDs);
	}

	@Override
	public ADistance getDepthMeters() throws OutOfRange {
		logger.trace(MMarker.GETTER, "Returning Depth in metres {} .",
				depthMeters);
		return depthMeters;
	}

	@Override
	public ATemperature getTemperatureDegrees() {
		logger.trace(MMarker.GETTER,
				"Returning Temperature in degreesCelsius {} .",
				temperatureDegrees);
		return temperatureDegrees;
	}

	@Override
	public void readingPaused() {
	}

	@Override
	public void readingStarted() {
	}

	@Override
	public void readingStopped() {
	}

	@Override
	public void sentenceRead(SentenceEvent event) {

		String sid = event.getSentence().getSentenceId().toString();
		logger.debug("Sentence received is of Id type", sid);
		try {
			if (sid.equals("MTW")) {
				MTWSentence mtw = (MTWSentence) event.getSentence();
				temperatureDegrees = new DegreesCelcius(mtw.getTemperature());

			} else if (sid.equals("DBT")) {
				DBTSentence dbt = (DBTSentence) event.getSentence();
				depthMeters = new Metres(dbt.getDepth());

			} else if (sid.equals("DPT")) {
				DPTSentence dpt = (DPTSentence) event.getSentence();
				depthMeters = new Metres(dpt.getDepth());

			}
		} catch (OutOfRange e) {
			logger.debug("Value is out of range", new OutOfRange());
		}
	}
}
