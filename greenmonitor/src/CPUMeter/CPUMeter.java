package CPUMeter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
//import java.lang.management.OperatingSystemMXBean;
import com.sun.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.comm.*;


public class CPUMeter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
      
		
		
      for (int i = 0; i < 1000; i++) {
    	  try 
    	  {
    	  Thread.sleep(1000); // do nothing for 1000 miliseconds (1 second)
    	  } 
    	  catch(InterruptedException e)
    	  {
    	  e.printStackTrace();
    	  }
    	
    	  printUsage();
    	  System.out.println();
    	  
	}
	}

	private static void printUsage() {
		  OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
		    method.setAccessible(true);
		    if (method.getName().startsWith("get") 
		        && Modifier.isPublic(method.getModifiers())) {
		            Object value;
		        try {
		            value = method.invoke(operatingSystemMXBean);
		        } 
		        catch (Exception e) {
		            value = e;
		        } // try
		        System.out.println(method.getName() + " = " + value);
		    } // if
		  } // for
	}
	
	public double getCPUload() {
		  OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
		    method.setAccessible(true);
		    if (method.getName().startsWith("get") 
		        && Modifier.isPublic(method.getModifiers())) {
		            double value = 0.00;
		        try {
		            value = operatingSystemMXBean.getSystemCpuLoad();
		        } 
		        catch (Exception e) {
		        	System.err.println( e );
		        } // try
		        return value;
		    } // if
		  } // for
		  return 0;
	}
	
}



class InputThread extends Thread {
	InputStream theInput ;
	OutputStream theOutput;
	FileOutputStream theFILout ;
	
	InputThread ( InputStream in, OutputStream out, FileOutputStream fout ) {
		theInput = in;
		theOutput= out;
		theFILout= fout;
	}
	
	public void CommLine(String cmd){
		try {
			theOutput.write(cmd.getBytes(), 0, cmd.length());
			String strLog = "[Command] " +  cmd + "\r\n";
			System.out.println( strLog ) ;
		} 
		catch (IOException ex) {
			Logger.getLogger(InputThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void run() {
		CommLine("#L,W,3,E,0,1;");
		String strLog = "";
		
		try{
			byte[] buffer1 = new byte[256];
			while ( true ) {
				int bytesRead = theInput.read(buffer1);
				
				if ( bytesRead == -1 ) {
					break;
				}						
				
				if ( "stop".equals( new String( buffer1, 0, bytesRead -2 ) ) ) {
					String cmd    = (char)24 + "#L,W,3,I,0,1;";   // stop logging
					       buffer1 = cmd.getBytes();
					       strLog = "[Command] " +  cmd + "\r\n";
					       
					System.out.println( strLog ) ;
					
					theOutput.write( buffer1, 0, buffer1.length );				
					theFILout.write( strLog.getBytes() );
				} else {
					strLog = "[Command] " + new String( buffer1, 0, bytesRead ) ;

					System.out.println( strLog );
					
					theOutput.write( buffer1, 0, bytesRead - 2 );
					theFILout.write( strLog.getBytes() );
				}
				
			}
		}
		catch( IOException e ) {
			System.err.println( e );
		}
	}
}

class OutputThread extends Thread {
	int count = 0;
	int endcount = 0;
	boolean logFlag = true;
	
	InputStream theInput ;
	OutputStream theOutput;
	FileOutputStream theFILout ;
	
	OutputThread ( InputStream in, OutputStream out, FileOutputStream fout, int numData ){
		theInput = in;
		theOutput= out;
		theFILout= fout;
		endcount = numData;
	}
	
	public void run() {
		CPUMeter meter = new CPUMeter();
		String strLog = "";
		double cpuload = 0.0;	// CPU load
		double mmload = 0.0;	// memory load
		double pcpower = 0.0;	// pc power consumption
		
		try{
			byte[] buffer = new byte[256];
			

			while ( logFlag ) {
				int bytesRead = theInput.read(buffer);
				
				if ( bytesRead == -1 ) {
					break;
				}

				if ( count <= 3 ) {
					count++;
					continue;
				}
				

				cpuload = meter.getCPUload();
				

				if ( cpuload < 0.0001 ) {
					cpuload = 0.0001;
				}
				System.out.print( ( count - 3 ) + " " + cpuload * 100 + "," );
								

				strLog = "[Reply ] " + new String( buffer, 0, bytesRead ) ;//System.out.print( strLog );
				String[] spiltLog = new String[10];
				spiltLog = strLog.split("[,;]");
		
				if ( spiltLog.length < 4 || spiltLog[3].length() == 0 || spiltLog[3].equals("_") || spiltLog[3].equals("_;") ) {
					continue;
				} 
				else {
					pcpower = Double.parseDouble(spiltLog[3]) / 10;
					System.out.print( pcpower + "\n" );
				}
		
				theFILout.write( Double.toString( cpuload * 100 ).getBytes() );
				theFILout.write( ",".getBytes() );
				theFILout.write( Double.toString( mmload ).getBytes() );
				theFILout.write( ",".getBytes() );
				theFILout.write( Double.toString( pcpower ).getBytes() );
				theFILout.write( "\r\n".getBytes() );
				
				count++;
				if ( count == ( endcount + 4 ) ) {
					logFlag = false;
				}
			}
		} catch ( IOException e ) {
			System.err.println( e );
		}
	}
	
	
}
