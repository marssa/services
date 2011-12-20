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
import mise.marssa.footprint.data_types.float_datatypes.distance.ADistance;
import mise.marssa.footprint.data_types.float_datatypes.distance.Metres;
import mise.marssa.footprint.data_types.float_datatypes.temperature.ATemperature;
import mise.marssa.footprint.data_types.float_datatypes.temperature.DegreesCelcius;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation_equipment.IDepthSensor;

/**
 * @author Warren Zahra
 *
 */
public class DepthSensor implements IDepthSensor, SentenceListener {
	SentenceReader reader;
	
	Metres depthMetres = null;
	DegreesCelcius temperatureDegrees = null;
	
	public DepthSensor(SentenceReader reader){
		this.reader = reader;
        reader.addSentenceListener(this, "MTW");
        reader.addSentenceListener(this, SentenceId.DBT);
        reader.addSentenceListener(this, SentenceId.DPT);
	}
	
	public ADistance getDepthMetres() throws OutOfRange{
		return depthMetres;
	}
	
	public ATemperature getTemperatureDegrees(){
		return temperatureDegrees;
	}
	
	public void readingPaused() {}
	
	public void readingStarted() {}
	
	public void readingStopped() {}
	
	
	public void sentenceRead(SentenceEvent event) {
		
		String sid = event.getSentence().getSentenceId().toString();
		System.out.println("Received sentence with sid = " + sid);
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
	        	
	        }
		} catch (OutOfRange e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
