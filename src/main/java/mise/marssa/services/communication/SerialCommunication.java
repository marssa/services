/**

 * 
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
	static Logger serialCommunicationLogger = (Logger) LoggerFactory.getLogger(SerialCommunication.class);
		InputStream in;
		OutputStream out;
		
	    public SerialCommunication()
	    {
	        super();
	    }
	    
	        
	    public void connect ( String portName, int baudrate, int dataBits,int stopBits,int parity) throws Exception
	    {
	    	serialCommunicationLogger.info("Connected with port {}.", portName);
	    	Object[] serialConnection = {baudrate, dataBits,stopBits,parity};
	    	serialCommunicationLogger.info("Parameters for SerialPort are BaudRate{} . DataBits{} . StopBits {} . Parity {} .",serialConnection);
	    	
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
	    	serialCommunicationLogger.debug(MMarker.GETTER,"Returning InputStream  {} .", in);
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
	                serialCommunicationLogger.info("Sending {} . data to serial",outputSerialSentence);
	               // System.out.println("Data send" + outputSerialSentence);
	                out.flush();
	            }
	            catch ( IOException e )
	            {//TODO exception handling 
	                e.printStackTrace();
	            }            
	        }
	    }
	   
	    
	    
	}


