/**
 * 
 */
package mise.marssa.services.navigation;

import java.io.IOException;
import java.util.List;

import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.TPVObject;
import mise.marssa.services.constants.ServicesConstants;
import mise.marssa.footprint.datatypes.MDate;
import mise.marssa.footprint.datatypes.MString;
import mise.marssa.footprint.datatypes.composite.Coordinate;
import mise.marssa.footprint.datatypes.composite.Latitude;
import mise.marssa.footprint.datatypes.composite.Longitude;
import mise.marssa.footprint.datatypes.decimal.DegreesFloat;
import mise.marssa.footprint.datatypes.decimal.MFloat;
import mise.marssa.footprint.datatypes.decimal.speed.Knots;
import mise.marssa.footprint.datatypes.integer.DegreesInteger;
import mise.marssa.footprint.datatypes.integer.MInteger;
import mise.marssa.footprint.datatypes.time.Hours;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.exceptions.NoValue;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.navigation.IGpsReceiver;
import mise.marssa.footprint.logger.MMarker;
import mise.marssa.services.constants.ServicesConstants;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.TPVObject;

/**
 * @author Clayton Tabone
 *
 */
public class GpsReceiver implements IGpsReceiver {

	private static Logger GPSReceiverLogger = (Logger) LoggerFactory.getLogger(GpsReceiver.class);
	GPSdEndpoint ep;
	private MString host;
	private MInteger port;

	public GpsReceiver(MString host, MInteger port) {
		GPSReceiverLogger.info("Trying to connect with a GPS through GPSD with host: {}  and port: {} .",host.toString(),port.getValue());
		this.host = host;
		this.port = port;
		GPSReceiverLogger.error(MMarker.EXCEPTION,"IOException" , new IOException());
		//ep = new GPSdEndpoint(host.getContents(), port.getValue());
		//ep.start();
		try {
			//System.out.println("gpsd started " + ep.version());
			GPSReceiverLogger.info("GPSD version {} . started", ep.version());
			//System.out.println("Enable watch mode for gpsd " + ep.watch(true, true));
			GPSReceiverLogger.info("Enable watch mode for GPSD {} .", ep.watch(true,true));
		} catch (IOException e) {
			GPSReceiverLogger.error(MMarker.EXCEPTION,"IOException" , new IOException());
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (ParseException e) {
			GPSReceiverLogger.error(MMarker.EXCEPTION,"ParseException", new ParseException());
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		GPSReceiverLogger.debug("Connected with a GPS with host: {} and port: {}",host.toString(),port.getValue());
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
		GPSReceiverLogger.trace("Getting COG from GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		for(int i = 0; i < ServicesConstants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				float cog = (float) ep.poll().getFixes().get(0).getCourse();
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning COG {} .",cog);
				return new DegreesFloat(cog);
			} catch(IOException e) {
				GPSReceiverLogger.error("IOException",new IOException());
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					//TODO should we let host and port in every class be global so that when another gps is instantiated
					// we can refer to that particular GPS
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
					GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.error("The COG is not available from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
					//throw new NoValue("The COG is not available from the GPSReceiver. This is the error message from the gpsd4java library:" + e.getMessage(), e.getCause());
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
		GPSReceiverLogger.trace("Getting Coordinate from GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		//for(int i = 0; i < Constants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				List<TPVObject> tpvList = ep.poll().getFixes();
				if(tpvList.isEmpty()){
					GPSReceiverLogger.error("No TPV object could be read",new NoConnection());
					//throw new NoConnection("No TPV object could be read");
				}
				TPVObject tpv = tpvList.get(0);
				Latitude latitude = new Latitude(new DegreesFloat((float) tpv.getLatitude()));
				Longitude longitude = new Longitude(new DegreesFloat((float) tpv.getLongitude()));
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning Coordinate: {} .",new Coordinate(latitude, longitude).toString());
				return new Coordinate(latitude, longitude);
			} catch(IOException e) {
				//if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
				Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
				GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
				GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
				//}
			} catch(ParseException e) {
				//if(i > Constants.GENERAL.RETRY_AMOUNT.getValue()) {
				GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
				Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
				GPSReceiverLogger.error("The Coordinate is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
				//throw new NoValue(e.getMessage(), e.getCause());
				//}
			}
		//}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful will return a NoConnection Exception
		//return null;
			return null;
	}

	/* (non-Javadoc)
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getDate()
	 */
	public MDate getDate() throws NoConnection, NoValue {
		GPSReceiverLogger.trace("Getting Date from GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		for(int i = 0; i < ServicesConstants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double timestamp = ep.poll().getFixes().get(0).getTimestamp();
				System.out.println(timestamp);
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning Date: {} .",new MDate((long)timestamp).toString());
				return new MDate((long) timestamp);
			} catch(IOException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
					GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.error("The Date is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
					//throw new NoValue("The Date is not available from the GPSReceiver. This is the error message from the gpsd4java library:" + e.getMessage(), e.getCause());
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
		GPSReceiverLogger.trace("Getting Elevation from GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		for(int i = 0; i < ServicesConstants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double altitude = ep.poll().getFixes().get(0).getAltitude();
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning Elevation: {} .",new DegreesFloat((float) altitude).getValue());
				//System.out.println("This altitude is " + altitude);
				return new DegreesFloat((float) altitude);
			} catch(IOException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
					GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
			}
			}catch(ParseException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.error("The Altitude is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
					//throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
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
		GPSReceiverLogger.info("Getting Speed Over Ground from a GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		for (int i = 0; i < ServicesConstants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				double speed = ep.poll().getFixes().get(0).getSpeed();
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning SOG: {} .",new Knots((float) speed).getValue());
				//System.out.println("This altitude is " + altitude);
				return new Knots((float) speed);
				
			} catch(IOException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
					GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
			}
			}catch(ParseException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.error("The SOG is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
					//throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
				}
			} catch (OutOfRange e) {
				// TODO Auto-generated catch block
				GPSReceiverLogger.debug(MMarker.EXCEPTION,"SOG is out of range", new OutOfRange());
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
		GPSReceiverLogger.info("Getting EPT from a GPSReceiver with Host: {} and Port: {}.",host.toString(),port.getValue());
		for(int i = 0; i < ServicesConstants.GENERAL.RETRY_AMOUNT.getValue(); i++) {
			try {
				// Suppose to be EPT, description of an EPT is http://www.devhardware.com/c/a/Mobile-Devices/TomTom-GO-920T-GPS-Review/2/
				double EPT = ep.poll().getFixes().get(0).getCourse();
				GPSReceiverLogger.trace(MMarker.GETTER,"Returning EPT: {} .",new Knots((float) EPT).getValue());
				//System.out.println("This altitude is " + altitude);
				return new Knots((float) EPT);
				
			} catch(IOException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoConnection());
					GPSReceiverLogger.error("Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",hoPo);
					//throw new NoConnection(e.getMessage(), e.getCause());
				}
			} catch(ParseException e) {
				if(i > ServicesConstants.GENERAL.RETRY_AMOUNT.getValue()) {
					GPSReceiverLogger.debug(MMarker.EXCEPTION,"IOException",new NoValue());
					Object[] hoPo = {host.getContents(),port.getValue(),e.getMessage(),e.getCause()};
					GPSReceiverLogger.error("The EPT is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",hoPo);
					//throw new NoValue("The Altitude is not available from the GPSReceiver." + e.getMessage(), e.getCause());
				}
			}
		}
		return null;
		
	}
}
