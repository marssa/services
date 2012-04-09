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
package mise.marssa.services.communication;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mise.marssa.footprint.logger.MMarker;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;


/**
 * @author Warren Zahra
 *
 */
public class SerialCommunication {
	/**
	 * 
	 */
	private static Logger serialCommunicationLogger = (Logger) LoggerFactory.getLogger(SerialCommunication.class);
		InputStream in;
		OutputStream out;
		
	    public SerialCommunication()
	    {
	        super();
	    }
	    
	        
	    public void connect ( String portName, int baudrate, int dataBits,int stopBits,int parity) throws Exception
	    {
	    	serialCommunicationLogger.info(MMarker.CONNECTION,"Connected with port {}.", portName);
	    	Object[] serialConnection = {baudrate, dataBits,stopBits,parity};
	    	serialCommunicationLogger.info(MMarker.CONSTRUCTOR,"Parameters for SerialPort are BaudRate{} . DataBits{} . StopBits {} . Parity {} .",serialConnection);
	    	
	        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	        	serialCommunicationLogger.error("Port {}. is currently in use by another Device", portName);
	           // System.out.println("Error: Port is currently in use");
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	               // serialPort.setSerialPortParams(baudrate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	                serialPort.setSerialPortParams(baudrate,dataBits,stopBits,parity);
	                
	                in = serialPort.getInputStream();
	                out = serialPort.getOutputStream();	               
	            }
	            else
	            {
	            	serialCommunicationLogger.error("Only serial ports are handled by this example");
	               // System.out.println("Error: Only serial ports are handled by this example.");
	            }
	        }     
	    }
	    
	    /** */
	    public InputStream getInputStream(){
	    	serialCommunicationLogger.trace(MMarker.GETTER,"Returning InputStream  {} .", in);
	    	return in;
	    }
	    
	    public void read(InputStream in) throws IOException{
	    	 {//TODO read should we show the string in the logger ????
	             byte[] buffer = new byte[1024];
	             int len = -1;
	             try
	             {
	                 while ( ( len = this.in.read(buffer)) > -1 )
	                 {
	                	 serialCommunicationLogger.trace("Input stream is" +new String(buffer,0,len));
	                     System.out.print(new String(buffer,0,len));
	                 }
	             }
	             catch ( IOException e )
	             {
	                 e.printStackTrace();
	             }            
	         }
	    }

	        
	    public void write(String outputSerialSentence){

	        {
	            try
	            {                
	                out.write(outputSerialSentence.getBytes()); 
	                serialCommunicationLogger.trace("Sending {} . data to serial",outputSerialSentence);
	               // System.out.println("Data send" + outputSerialSentence);
	                out.flush();
	            }
	            catch ( IOException e )
	            {//TODO exception handling 
	               // e.printStackTrace();
	                serialCommunicationLogger.debug("IOException handling",new IOException());
	            }            
	        }
	    }
	   
	    
	    
	}


