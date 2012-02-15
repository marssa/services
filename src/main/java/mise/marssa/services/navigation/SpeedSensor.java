/**
 * 
 */
package mise.marssa.services.navigation;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import mise.marssa.footprint.datatypes.decimal.DegreesFloat;
import mise.marssa.footprint.datatypes.decimal.distance.ADistance;
import mise.marssa.footprint.datatypes.decimal.distance.Metres;
import mise.marssa.footprint.datatypes.decimal.speed.ASpeed;
import mise.marssa.footprint.datatypes.decimal.speed.Knots;
import mise.marssa.footprint.datatypes.decimal.temperature.ATemperature;
import mise.marssa.footprint.datatypes.decimal.temperature.DegreesCelcius;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation.ISpeedSensor;

/**
 * @author Warren Zahra
 *
 */
public class SpeedSensor implements ISpeedSensor, SentenceListener {

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
        
	}
	
	public ASpeed getSpeedKnots() throws OutOfRange{
		
		return speedKnots;
	}
		
	public DegreesFloat getDegreesTrue() throws OutOfRange{
		
		return degreesTrue;
	}

	public DegreesFloat getDegreesMagnetic() throws OutOfRange{
	
		return degreesMagnetic;
	}	
	
	public ADistance getDepthMetres() throws OutOfRange{
		
		return depthMetres;
	}	
	
	public ATemperature getTemperature(){
		return temperatureDegrees;
	}

	public void readingPaused() {}
	
	public void readingStarted() {}
	
	public void readingStopped() {}
	
	public void sentenceRead(SentenceEvent event) {
		String sid = event.getSentence().getSentenceId().toString();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
