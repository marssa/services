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

import java.io.IOException;
import java.math.MathContext;
import java.util.List;

import org.json.JSONException;
import org.marssa.footprint.datatypes.MDate;
import org.marssa.footprint.datatypes.MString;
import org.marssa.footprint.datatypes.composite.Coordinate;
import org.marssa.footprint.datatypes.composite.Latitude;
import org.marssa.footprint.datatypes.composite.Longitude;
import org.marssa.footprint.datatypes.decimal.DegreesDecimal;
import org.marssa.footprint.datatypes.decimal.MDecimal;
import org.marssa.footprint.datatypes.decimal.distance.Metres;
import org.marssa.footprint.datatypes.decimal.speed.Knots;
import org.marssa.footprint.datatypes.integer.DegreesInteger;
import org.marssa.footprint.datatypes.integer.MInteger;
import org.marssa.footprint.datatypes.time.Hours;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.exceptions.NoValue;
import org.marssa.footprint.exceptions.OutOfRange;
import org.marssa.footprint.interfaces.navigation.IGpsReceiver;
import org.marssa.footprint.logger.MMarker;
import org.marssa.services.constants.ServicesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ParseException;
import de.taimos.gpsd4java.types.TPVObject;

/**
 * @author Clayton Tabone
 * 
 */
public class GpsReceiver implements IGpsReceiver {

	private static final Logger logger = LoggerFactory
			.getLogger(GpsReceiver.class);
	GPSdEndpoint ep;
	private final MString host;
	private final MInteger port;

	public GpsReceiver(MString host, MInteger port) throws NoValue,
			NoConnection {
		logger.info(
				"Trying to connect with a GPS through GPSD with host: {}  and port: {} .",
				host, port);
		this.host = host;
		this.port = port;
		try {
			ep = new GPSdEndpoint(host.getContents(), port.intValue(),
					new ResultParser());
			ep.start();
			logger.info("GPSD version {} . started", ep.version());
			logger.info("Enable watch mode for GPSD {} .", ep.watch(true, true));
		} catch (IOException e) {
			logger.error(MMarker.EXCEPTION, "NoConnection", e);
			throw new NoConnection(e.getMessage(), e.getCause());
		} catch (ParseException e) {
			logger.error(MMarker.EXCEPTION, "ParseException", e);
			throw new NoValue(e.getMessage(), e.getCause());
		} catch (JSONException e) {
			logger.error(MMarker.EXCEPTION, "JSONException", e);
			throw new NoValue(e.getMessage(), e.getCause());
		}
		logger.debug("Connected with a GPS with host: {} and port: {}", host,
				port);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getAzimuth()
	 */
	@Override
	public DegreesInteger getAzimuth() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getCOG()
	 */
	@Override
	public DegreesDecimal getCOG() throws NoConnection, NoValue {
		logger.trace(
				"Getting COG from GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 0; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				double cog = ep.poll().getFixes().get(0).getCourse();
				logger.trace(MMarker.GETTER, "Returning COG {} .", cog);
				if (Double.isNaN(cog)) {
					throw new NoValue();
				} else
					return new DegreesDecimal(cog, new MathContext(5));
			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					// TODO should we let host and port in every class be global
					// so that when another gps is instantiated
					// we can refer to that particular GPS
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					NoValue nv = new NoValue(
							"The COG is not available from the GPSReceiver. This is the error message from the gpsd4java library:"
									+ e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoValue", e);
					logger.error(
							"The COG is not available from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",
							hoPo);
					throw nv;
				}
			}
		}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful
		// will return a NoConnection Exception
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getCoordinate()
	 */
	@Override
	public Coordinate getCoordinate() throws NoConnection, NoValue, OutOfRange {
		logger.trace(
				"Getting Coordinate from GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 0; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				List<TPVObject> tpvList = ep.poll().getFixes();
				if (tpvList.isEmpty()) {
					logger.error("No TPV object could be read",
							new NoConnection());
					// throw new NoConnection("No TPV object could be read");
				}
				TPVObject tpv = tpvList.get(0);
				Latitude latitude = new Latitude(new DegreesDecimal(
						tpv.getLatitude()));
				Longitude longitude = new Longitude(new DegreesDecimal(
						tpv.getLongitude()));
				logger.trace(MMarker.GETTER, "Returning Coordinate: {} .",
						new Coordinate(latitude, longitude).toString());
				return new Coordinate(latitude, longitude);
			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					NoValue nv = new NoValue(e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoValue", e);
					logger.error(
							"The Coordinate is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",
							hoPo);
					throw nv;
				}
			}

		}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful
		// will return a NoConnection Exception
		// return null;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getDate()
	 */
	@Override
	public MDate getDate() throws NoConnection, NoValue {
		logger.trace(
				"Getting Date from GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 0; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				double timestamp = ep.poll().getFixes().get(0).getTimestamp();
				System.out.println(timestamp);
				logger.trace(MMarker.GETTER, "Returning Date: {} .", new MDate(
						(long) timestamp).toString());
				return new MDate((long) timestamp);
			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}. The error message is {} and the cause is {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					NoValue nv = new NoValue(
							"The Date is not available from the GPSReceiver. This is the error message from the gpsd4java library:"
									+ e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoValue", e);
					Object[] hoPo = { host, port, e.getMessage(), e.getCause() };
					logger.error(
							"The Date is not availalbe from the GPSReceiver with host: {} and port: {}, The error message is {} and the cause is {} ..",
							hoPo);
					throw nv;
				}
			}
		}
		// This code is unreachable but was added here to satisfy the compiler.
		// The try/catch will loop for the RETRY_AMOUNT and if not successful
		// will return a NoConnection Exception
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getElevation()
	 */
	@Override
	public Metres getAltitude() throws NoConnection, NoValue, OutOfRange {
		logger.trace(
				"Getting Altitude from GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 0; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				Metres altitude = new Metres(ep.poll().getFixes().get(0)
						.getAltitude(), new MathContext(5));
				logger.trace(MMarker.GETTER, "Returning Elevation: {} .",
						altitude);
				if (Double.isNaN(altitude.doubleValue())) {
					throw new NoValue();
				} else
					return altitude;
			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port };
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					NoValue nc = new NoValue(
							"The Altitude is not available from the GPSReceiver."
									+ e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoValue", e);
					Object[] hoPo = { host, port };
					logger.error(
							"The Altitude is not availalbe from the GPSReceiver with host: {} and port: {}",
							hoPo);
					throw nc;
				}
			} catch (OutOfRange e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getHDOP()
	 */
	@Override
	public MDecimal getHDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getLocalZoneTime
	 * ()
	 */
	@Override
	public Hours getLocalZoneTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getPDOP()
	 */
	@Override
	public MDecimal getPDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatelliteID()
	 */
	@Override
	public MInteger getSatelliteID() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatelliteInView
	 * ()
	 */
	@Override
	public MInteger getSatelliteInView() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSatellitesInUse
	 * ()
	 */
	@Override
	public MInteger getSatellitesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSignalSrength
	 * ()
	 */
	@Override
	public MDecimal getSignalSrength() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSNR()
	 */
	@Override
	public MDecimal getSNR() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getSOG()
	 */
	@Override
	public Knots getSOG() throws NoConnection, NoValue {
		logger.info(
				"Getting Speed Over Ground from a GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 0; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				double speed = ep.poll().getFixes().get(0).getSpeed();
				logger.trace(MMarker.GETTER, "Returning SOG: {} .", new Knots(
						speed, new MathContext(5)));
				if (Double.isNaN(speed)) {
					throw new NoValue();
				} else
					return new Knots(speed, new MathContext(6));

			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					Object[] hoPo = { host, port };
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					NoValue nv = new NoValue(
							"The Altitude is not available from the GPSReceiver."
									+ e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "NoValue", e);
					Object[] hoPo = { host, port };
					logger.error(
							"The SOG is not availalbe from the GPSReceiver with host: {} and port: {}",
							hoPo);
					throw nv;
				}
			} catch (OutOfRange e) {
				// TODO Auto-generated catch block
				logger.debug(MMarker.EXCEPTION, "SOG is out of range",
						new OutOfRange());
			}
		}
		// TODO Auto-generated method stub
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getStatus()
	 */
	@Override
	public MString getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getTime()
	 */
	@Override
	public Hours getTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mise.marssa.interfaces.navigation_equipment.IGpsReceiver#getVDOP()
	 */
	@Override
	public MDecimal getVDOP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MDecimal getEPT() throws NoConnection, NoValue, OutOfRange {
		logger.info(
				"Getting EPT from a GPSReceiver with Host: {} and Port: {}.",
				host, port);
		for (int i = 1; i <= ServicesConstants.GENERAL.RETRY_AMOUNT.intValue(); i++) {
			try {
				// Suppose to be EPT, description of an EPT is
				// http://www.devhardware.com/c/a/Mobile-Devices/TomTom-GO-920T-GPS-Review/2/
				double EPT = ep.poll().getFixes().get(0).getCourse();
				logger.trace(MMarker.GETTER, "Returning EPT: {} .", new Knots(
						EPT, new MathContext(5)));
				// System.out.println("This altitude is " + altitude);
				if (Double.isNaN(EPT)) {
					throw new NoValue();
				} else
					return new Knots(EPT);

			} catch (IOException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					NoConnection nc = new NoConnection(e.getMessage(),
							e.getCause());
					Object[] hoPo = { host, port };
					logger.debug(MMarker.EXCEPTION, "NoConnection", e);
					logger.error(
							"Could not connect to the GPS with Host: {} and Port: {}",
							hoPo);
					throw nc;
				}
			} catch (ParseException e) {
				if (i == ServicesConstants.GENERAL.RETRY_AMOUNT.intValue()) {
					NoValue nv = new NoValue(
							"The Altitude is not available from the GPSReceiver."
									+ e.getMessage(), e.getCause());
					logger.debug(MMarker.EXCEPTION, "IOException", e);
					Object[] hoPo = { host, port };
					logger.error(
							"The EPT is not availalbe from the GPSReceiver with host: {} and port: {}",
							hoPo);
					throw nv;
				}
			}
		}
		return null;

	}
}
