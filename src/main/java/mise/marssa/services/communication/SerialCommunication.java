/**

 * 
 */
package mise.marssa.services.communication;
import gnu.io.CommPort;
	import gnu.io.CommPortIdentifier;
	import gnu.io.SerialPort;

	import java.io.FileDescriptor;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.OutputStream;


/**
 * @author Warren Zahra
 *
 */
public class SerialCommunication {
	/**
	 * 
	 */
		InputStream in;
		OutputStream out;
		
	    public SerialCommunication()
	    {
	        super();
	    }
	    
	        
	    public void connect ( String portName ) throws Exception
	    {
	    	
	        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	        if ( portIdentifier.isCurrentlyOwned() )
	        {
	            System.out.println("Error: Port is currently in use");
	        }
	        else
	        {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            
	            if ( commPort instanceof SerialPort )
	            {
	                SerialPort serialPort = (SerialPort) commPort;
	                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	                
	                in = serialPort.getInputStream();
	                out = serialPort.getOutputStream();
	                
	               
	            }
	            else
	            {
	                System.out.println("Error: Only serial ports are handled by this example.");
	            }
	        }     
	    }
	    
	    /** */
	    public InputStream getInputStream(){
	    	return in;
	    }
	    
	    public void read(InputStream in) throws IOException{
	    	 {
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
	                System.out.println("Data send" + outputSerialSentence);
	                out.flush();
	            }
	            catch ( IOException e )
	            {
	                e.printStackTrace();
	            }            
	        }
	    }
	   
	    
	    
	}


