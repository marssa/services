/**
 * 
 */
package mise.marssa.services.navigation;

import java.io.IOException;
import java.util.List;

import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.TPVObject;
import mise.marssa.services.constants.Constants;
import mise.marssa.footprint.data_types.MDate;
import mise.marssa.footprint.data_types.MString;
import mise.marssa.footprint.data_types.time.Hours;
import mise.marssa.footprint.data_types.composite_datatypes.Coordinate;
import mise.marssa.footprint.data_types.composite_datatypes.Latitude;
import mise.marssa.footprint.data_types.composite_datatypes.Longitude;
import mise.marssa.footprint.data_types.float_datatypes.DegreesFloat;
import mise.marssa.footprint.data_types.float_datatypes.MFloat;
import mise.marssa.footprint.data_types.float_datatypes.speed.Knots;
import mise.marssa.footprint.data_types.integer_datatypes.DegreesInteger;
import mise.marssa.footprint.data_types.integer_datatypes.MInteger;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.exceptions.NoValue;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation_equipment.IGpsReceiver;

/**
 * @author Clayton Tabone
 *
 */
public class GpsReceiver implements IGpsReceiver {

	GPSdEndpoint ep;

	public GpsReceiver(MString host, MInteger port) {
		ep = new GPSdEndpoint(host.getContents(), port.getValue());
		ep.start();
		try {
			System.out.println("gpsd started " + ep.version());
			System.out.println("Enable watch mode for gpsd " + ep.watch(true, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getAzimuth()
	 */
	public DegreesInteger getAzimuth() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getCOG()
	 */
	public DegreesFloat getCOG() throws NoConnection, NoValue {
		for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				float cog = (float) ep.poll().getFixes().get(0).getCourse();
				return new DegreesFloat(cog);
			} catch(IOException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue("The COG is not available from the GPSReceiver. This is the error message from the gpsd4java library:" + e.getMessage(), e.getCause());
				}
			}
		}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful will return a NoConnection Exception
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getCoordinate()
	 */
	public Coordinate getCoordinate() throws NoConnection, NoValue, OutOfRange {
		//for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				List<TPVObject> tpvList = ep.poll().getFixes();
				if(tpvList.isEmpty())
					throw new NoConnection("No TPV object could be read");
				TPVObject tpv = tpvList.get(0);
				Latitude latitude = new Latitude(new DegreesFloat((float) tpv.getLatitude()));
				Longitude longitude = new Longitude(new DegreesFloat((float) tpv.getLongitude()));
				return new Coordinate(latitude, longitude);
			} catch(IOException e) {
				//if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
				//}
			} catch(ParseException e) {
				//if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue(e.getMessage(), e.getCause());
				//}
			}
		//}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful will return a NoConnection Exception
		//return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getDate()
	 */
	public MDate getDate() throws NoConnection, NoValue {
		for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double timestamp = ep.poll().getFixes().get(0).getTimestamp();
				System.out.println(timestamp);
				return new MDate((long) timestamp);
			} catch(IOException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue("The COG is not available from the GPSReceiver. This is the error message from the gpsd4java library:" + e.getMessage(), e.getCause());
				}
			}
		}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful will return a NoConnection Exception
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getElevation()
	 */
	public DegreesFloat getElevation() throws NoConnection, NoValue {
		for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double altitude = ep.poll().getFixes().get(0).getAltitude();
				//System.out.println("This altitude is " + altitude);
				return new DegreesFloat((float) altitude);
			} catch(IOException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
			}
			}catch(ParseException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getHDOP()
	 */
	public MFloat getHDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getLocalZoneTime()
	 */
	public Hours getLocalZoneTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getPDOP()
	 */
	public MFloat getPDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatelliteID()
	 */
	public MInteger getSatelliteID() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatelliteInView()
	 */
	public MInteger getSatelliteInView() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatellitesInUse()
	 */
	public MInteger getSatellitesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSignalSrength()
	 */
	public MFloat getSignalSrength() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSNR()
	 */
	public MFloat getSNR() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSOG()
	 */
	public Knots getSOG() throws NoConnection, NoValue {
		for (int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double speed = ep.poll().getFixes().get(0).getSpeed();
				//System.out.println("This altitude is " + altitude);
				return new Knots((float) speed);
				
			} catch(IOException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
			}
			}catch(ParseException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
				}
			} catch (OutOfRange e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// TODO Auto-generated method stub
		return null;
	
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getStatus()
	 */
	public MString getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getTime()
	 */
	public Hours getTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getVDOP()
	 */
	public MFloat getVDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	public MFloat getEPT() throws NoConnection, NoValue, OutOfRange {
		for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				// Suppose to be EPT, description of an EPT is http://www.devhardware.com/c/a/Mobile-Devices/TomTom-GO-920T-GPS-Review/2/
				double EPT = ep.poll().getFixes().get(0).getCourse();
				//System.out.println("This altitude is " + altitude);
				return new Knots((float) EPT);
				
			} catch(IOException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
					throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
				}
			}
		}
		return null;
		
	}
}
