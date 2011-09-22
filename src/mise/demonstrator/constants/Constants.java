/**
 * 
 */
package mise.demonstrator.constants;

import mise.marssa.data_types.MString;
import mise.marssa.data_types.integer_datatypes.MInteger;

/**
 * @author Clayton Tabone
 *
 */
public class Constants {
	
	/**
	 * General Constants
	 * @author Clayton Tabone
	 */
	public final static class GENERAL {
		public final static MInteger RETRY_AMOUNT = new MInteger(5);
	}
	
	/**
	 * LabJack Constants
	 * @author Clayton Tabone
	 */
	public final static class LABJACK {
		public final static MString HOST = new MString("192.168.2.14");
		public final static MInteger PORT = new MInteger(5021);
	}
	
	/**
	 * Web Services Constants
	 * @author Clayton Tabone
	 */
	public final static class WEB_SERVICES {
		public final static MInteger PORT = new MInteger(8182);
	}
	
	/**
	 * Ramping Constants
	 * @author Clayton Tabone
	 */
	public final static class RAMPING {
		public final static MInteger RETRY_INTERVAL = new MInteger(5);
	}
	
	/**
	 * Rudder Constants
	 * @author Clayton Tabone
	 */
	public final static class RUDDER {
		// TODO What exactly should RUDDER_DELAY represent?
		public final static MInteger RUDDER_DELAY = new MInteger(2000);
	}
	
	/**
	 * GPS Constants
	 * @author Clayton Tabone
	 */
	public final static class GPS {
		public final static MString HOST = new MString("192.168.1.1");
		public final static MInteger PORT = new MInteger(2947);
	}
}