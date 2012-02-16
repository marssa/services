/**
 * 
 */
package mise.marssa.services.navigation;

import mise.marssa.footprint.datatypes.decimal.DegreesFloat;
import mise.marssa.footprint.datatypes.decimal.distance.ADistance;
import mise.marssa.footprint.datatypes.decimal.distance.Metres;
import mise.marssa.footprint.datatypes.decimal.speed.ASpeed;
import mise.marssa.footprint.datatypes.decimal.speed.Knots;
import mise.marssa.footprint.datatypes.decimal.temperature.ATemperature;
import mise.marssa.footprint.datatypes.decimal.temperature.DegreesCelcius;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation.ISpeedSensor;
import mise.marssa.footprint.logger.MMarker;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VHWSentence;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Warren Zahra
 *
 */
public class SpeedSensor implements ISpeedSensor, SentenceListener {

	private static Logger SpeedSensor = (Logger) LoggerFactory.getLogger("SpeedSensor");
	SentenceReader reader;
	Knots speedKnots = null;
	Metres depthMetres = null;
	DegreesFloat degreesTrue = null;
	DegreesFloat degreesMagnetic = null;
	DegreesCelcius temperatureDegrees = null;
	
	public SpeedSensor(SentenceReader reader){
		this.reader = reader;
        reader.addSentenceListener(this, "MTW");
        reader.addSentenceListener(this, "VHW");
        reader.addSentenceListener(this, SentenceId.DBT);
        reader.addSentenceListener(this, SentenceId.DPT);
        String[] sentenceIDs = {"MTW","VHW","DBT","DPT"};
        SpeedSensor.info("A speed sensor with the following Sentence id is instantiated",sentenceIDs);
	}
	
	public ASpeed getSpeedKnots() throws OutOfRange{
		SpeedSensor.trace(MMarker.GETTER,"Returning speed in knots {} .",speedKnots.getValue());
		return speedKnots;
	}
		
	public DegreesFloat getDegreesTrue() throws OutOfRange{
		SpeedSensor.trace(MMarker.GETTER,"Returning Degrees in degreesTrue {} .",degreesTrue.getValue());
		return degreesTrue;
	}

	public DegreesFloat getDegreesMagnetic() throws OutOfRange{
		SpeedSensor.trace(MMarker.GETTER,"Returning Degrees in degreesMagnetic {} .",degreesMagnetic.getValue());
		return degreesMagnetic;
	}	
	
	public ADistance getDepthMetres() throws OutOfRange{
		SpeedSensor.trace(MMarker.GETTER,"Returning Depth in metres {} .",depthMetres.getValue());
		return depthMetres;
	}	
	
	public ATemperature getTemperature(){
		SpeedSensor.trace(MMarker.GETTER,"Returning Temperature in degreesCelsius {} .",temperatureDegrees.getValue());
		return temperatureDegrees;
	}

	public void readingPaused() {}
	
	public void readingStarted() {}
	
	public void readingStopped() {}
	
	public void sentenceRead(SentenceEvent event) {
		String sid = event.getSentence().getSentenceId().toString();
		SpeedSensor.debug("Sentence received is of Id type",sid);
		try {
	        if (sid.equals("MTW")) {
	        	MTWSentence mtw = (MTWSentence) event.getSentence();
	        	temperatureDegrees = new DegreesCelcius((float) mtw.getTemperature());
	        	
	        } else if(sid.equals("DBT")) {
	        	DBTSentence dbt = (DBTSentence) event.getSentence();
				depthMetres = new Metres((float) dbt.getDepth());
	        	
	        } else if(sid.equals("DPT")) {
	        	DPTSentence dpt = (DPTSentence) event.getSentence();
	        	depthMetres = new Metres((float) dpt.getDepth());
	        	
	        }else if(sid.equals("VHW")) {
	        	VHWSentence vhw = (VHWSentence) event.getSentence();
	        	speedKnots = new Knots((float) vhw.getSpeedKnots());
	        	degreesMagnetic = new DegreesFloat((float) vhw.getDegreesMagnetic());
	        	degreesTrue = new DegreesFloat((float) vhw.getDegreesTrue());
	      		}
			}
	        catch (OutOfRange e) {
	        	SpeedSensor.debug("Value is out of range",new OutOfRange());
	        	// TODO Auto-generated catch block
	        	//e.printStackTrace();
		}
	}

}
