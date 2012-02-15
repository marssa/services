package mise.marssa.services.nmea;

/* 
 * NMEAReader.java
 * Copyright (C) 2011 Kimmo Tuukkanen
 * 
 * This file is part of Java Marine API.
 * <http://sourceforge.net/projects/marineapi/>
 * 
 * Java Marine API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Java Marine API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Marine API. If not, see <http://www.gnu.org/licenses/>.
 */

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.io.SentenceReader;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceValidator;

import mise.marssa.footprint.interfaces.navigation.IDepthSensor;
import mise.marssa.footprint.interfaces.navigation.ISensors;
import mise.marssa.footprint.interfaces.navigation.ISpeedSensor;
import mise.marssa.services.navigation.DepthSensor;
import mise.marssa.services.navigation.SpeedSensor;

// If using Java Communications API, remove gnu.io imports above and
// use corresponding javax.comm classes:
// 
// import javax.comm.CommPortIdentifier;
// import javax.comm.SerialPort;

/**
 * <p>
 * Serial port example using Java Communications API or RXTX libraries.
 * Application scans through all existing COM ports and seeks for NMEA data. If
 * valid data is found, scanning stops and application starts printing out the
 * sentences it reads from the port.
 * <ul>
 * <li><a
 * href="http://www.oracle.com/technetwork/java/index-jsp-141752.html">Java
 * Communications API</a>
 * <li><a href="http://rxtx.qbang.org/">RXTX wiki</a>
 * </ul>
 * 
 * @author Kimmo Tuukkanen
 */
public class NMEAReader implements SentenceListener, ISensors {

	
	SentenceReader sr;
	DepthSensor depthSensor;
	SpeedSensor speedSensor;
	Sentence mtwSentence = null;
	SentenceFactory SF = SentenceFactory.getInstance();
	
    public NMEAReader() {
    	 try {
             SerialPort sp = getSerialPort();

             if (sp != null) {
                 InputStream is = sp.getInputStream();
                 sr = new SentenceReader(is);
                 sr.addSentenceListener(this);
                 sr.start();
             }

         } catch (IOException e) {
             e.printStackTrace();
         }
    }
	
	public IDepthSensor getDepthSensor() {
    	return depthSensor;
    }
    
    public ISpeedSensor getSpeedSensor() {
		
		return speedSensor;
	}
    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingPaused()
     */
    public void readingPaused() {
        System.out.println("-- Paused --");
    }

    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingStarted()
     */
    public void readingStarted() {
        System.out.println("-- Started --");
    }

    /*
     * (non-Javadoc)
     * @see net.sf.marineapi.nmea.event.SentenceListener#readingStopped()
     */
    public void readingStopped() {
        System.out.println("-- Stopped --");
    }

    /*
     * (non-Javadoc)
     * @see
     * net.sf.marineapi.nmea.event.SentenceListener#sentenceRead(net.sf.marineapi
     * .nmea.event.SentenceEvent)
     */
    /**
     * Here we read NMEA data and for each different Talker ID
     * a new instance of that particular sensor is created 
     */
    public void sentenceRead(SentenceEvent event) {
    	
        
		
    	if (event.getSentence().getTalkerId().toString()=="SD")
    	{
    		if (depthSensor == null) {
    			depthSensor = new DepthSensor(sr);
    		}
    	}
    	
    	if (event.getSentence().getTalkerId().toString()=="DM")
    	{
    		if (speedSensor == null) {
    			speedSensor = new SpeedSensor(sr);
    		}
    		
    	}
    	
    	
    }

    /**
     * Scan serial ports for NMEA data.
     * 
     * @return SerialPort from which NMEA data was found, or null if data was
     *         not found in any of the ports.
     */
    private SerialPort getSerialPort() {
        try {
            Enumeration<CommPortIdentifier> e = CommPortIdentifier.getPortIdentifiers();

            while (e.hasMoreElements()) {
                CommPortIdentifier id = (CommPortIdentifier) e.nextElement();

                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {

                    SerialPort sp = (SerialPort) id.open("SerialExample", 30);

                    sp.setSerialPortParams(4800, SerialPort.DATABITS_8,
                            SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                    InputStream is = sp.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader buf = new BufferedReader(isr);

                    System.out.println("Scanning port " + sp.getName());

                    // try each port few times before giving up
                    for (int i = 0; i < 5; i++) {
                    	try {
                        	if(buf.ready()) {
	                        	System.out.println("Buffer ready, reading data ...");
	                            String data = buf.readLine();
	                            System.out.println("First line discarded. Reading next line ...");
	                            data = buf.readLine();
	                            System.out.println("Data is:" + data);
	                            if (SentenceValidator.isValid(data)) {
	                                System.out.println("NMEA data found!");
	                                return sp;
	                            }
                        	} else {
                        		System.out.println("Buffer not ready, inserting delay ...");
                        		Thread.sleep(500);
                        	}
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    is.close();
                    isr.close();
                    buf.close();
                }
            }
            System.out.println("NMEA data was not found..");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }  
}