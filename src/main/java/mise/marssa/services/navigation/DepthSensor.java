/**
 * 
 */
package mise.marssa.services.navigation;

import mise.marssa.footprint.datatypes.decimal.distance.ADistance;
import mise.marssa.footprint.datatypes.decimal.distance.Metres;
import mise.marssa.footprint.datatypes.decimal.temperature.ATemperature;
import mise.marssa.footprint.datatypes.decimal.temperature.DegreesCelcius;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation.IDepthSensor;
import mise.marssa.footprint.logger.MMarker;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import mise.marssa.footprint.datatypes.decimal.distance.ADistance;
import mise.marssa.footprint.datatypes.decimal.distance.Metres;
import mise.marssa.footprint.datatypes.decimal.temperature.ATemperature;
import mise.marssa.footprint.datatypes.decimal.temperature.DegreesCelcius;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation.IDepthSensor;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Warren Zahra
 * 
 */
public class DepthSensor implements IDepthSensor, SentenceListener {
	static Logger depthSensorLogger = (Logger) LoggerFactory.getLogger(DepthSensor.class);
	SentenceReader reader;

	Metres depthMetres = null;
	DegreesCelcius temperatureDegrees = null;

	public DepthSensor(SentenceReader reader) {
		this.reader = reader;
		reader.addSentenceListener(this, "MTW");
		reader.addSentenceListener(this, SentenceId.DBT);
		reader.addSentenceListener(this, SentenceId.DPT);
        String[] sentenceIDs = {"MTW","DBT","DPT"};
        depthSensorLogger.info("A depth sensor with the following Sentence id is instantiated",sentenceIDs);
	}

	public ADistance getDepthMetres() throws OutOfRange {
		depthSensorLogger.debug(MMarker.GETTER,"Returning Depth in metres {} .",depthMetres.getValue());
		return depthMetres;
	}

	public ATemperature getTemperatureDegrees() {
		depthSensorLogger.debug(MMarker.GETTER,"Returning Temperature in degreesCelsius {} .",temperatureDegrees.getValue());
		return temperatureDegrees;
	}

	public void readingPaused() {
	}

	public void readingStarted() {
	}

	public void readingStopped() {
	}

	public void sentenceRead(SentenceEvent event) {

		String sid = event.getSentence().getSentenceId().toString();
		depthSensorLogger.debug("Sentence received is of Id type",sid);
		try {
			if (sid.equals("MTW")) {
				MTWSentence mtw = (MTWSentence) event.getSentence();
				temperatureDegrees = new DegreesCelcius(
						(float) mtw.getTemperature());

			} else if (sid.equals("DBT")) {
				DBTSentence dbt = (DBTSentence) event.getSentence();
				depthMetres = new Metres((float) dbt.getDepth());

			} else if (sid.equals("DPT")) {
				DPTSentence dpt = (DPTSentence) event.getSentence();
				depthMetres = new Metres((float) dpt.getDepth());

			}
		} catch (OutOfRange e) {
			depthSensorLogger.error("Value is out of range",new OutOfRange());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
