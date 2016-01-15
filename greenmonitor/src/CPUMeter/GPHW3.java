package CPUMeter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;


public class GPHW3 {
	
	// parameters
	public static int numofTrain = 100;		         // number of training data
	public static int numofTest = 50;				 //number of test data
	public static double[] factorA = new double[2];	 // coefficients	a = factorA[0], b1 = factorA[1]
	public static double[] factorB = new double[2];	 // coefficients	a50 = factorB[0], b50 = factorB[1]
	
	// CPU
	public static double cpuload = 0.0;	 // CPU load
	public static double cpuSumload = 0.0;		
	public static double cpuAvgload = 0.0;	 // average CPU load
	
	// memory
	public static double mmload = 0.0;		 // memory load
	public static double mmSumload = 0.0;		
	public static double mmAvgload = 0.0;	 // average memory load
	
	// PC power
	public static double powerMeter = 0.0;		   // power meter pc power consumption
	public static double powerformulaA = 0.0;	   // estimate pc consumption
	public static double powerformulaAAvg = 0.0;
	private static CommPortIdentifier com = null;
	private static SerialPort thePort = null;
	
	//public static ExecutorService executor = Executors.newFixedThreadPool(10);
	public static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MICROSECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadPoolExecutor.AbortPolicy());
		
	public static void main(String[] args) {
					
		try {
			
			try {
				com = CommPortIdentifier.getPortIdentifier( "COM8" );
				if ( com.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
					//try {
						thePort = (SerialPort) com.open( "GPHW3", 10 ); // 
						thePort.setSerialPortParams( 115200, 8, 1, 0 ); // baud, dataBits, stopBits, parity
						
						//System.out.println( "\n...Colletcing test data..." + numofTest );
						// Collects the test data
						//DataCollect( numofTest, numofTest + "_testing_samples.txt" );
						// wait for complete
						//executor.shutdown();
						
						//while ( !executor.awaitTermination(10, TimeUnit.SECONDS) );
						
						// Training Phase
						System.out.println( "\n...Training Phase..." + numofTrain );
						
						// Data Collecting Phase
						DataCollect( numofTrain, numofTrain + "_training_samples.txt");
						
						// wait for complete
						executor.shutdown();
						
						while ( !executor.awaitTermination(10, TimeUnit.SECONDS) );
						
						// formula result
						formulatrain( numofTrain );
				
						// Testing phase
						//System.out.println("\n...Testing Phase..." + numofTest);
												
						// formula result test
						//formulatest( factorA[0], factorA[1] );
						System.out.println("Watte = x*CPUload + y");
						System.out.println("(x1 , y1) ="+"("+factorA[1]+","+factorA[0]+")");
						System.out.println("(x50, y50)="+"("+factorB[1]+","+factorB[0]+")");
						FileWriter fw = new FileWriter("result.txt", false);
						fw.write(factorA[1]+" "+factorA[0]+" "+factorB[1]+" "+factorB[0]);
						fw.close();
				}
			} 
			catch ( PortInUseException e ) {
				System.err.println( e );
			}
		} 
		catch ( Exception e ) {
			System.out.println( e );
		}
		
		System.out.println("\n...Complete...");
		System.exit(0);
	}
	public double[] getfactorA(){
		return factorA;
	}
	public double[] getfactorB(){
		return factorB;
	}
	
	public static void DataCollect( int numofCollect, String filename ) {
		// write log
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream( "./"+ filename );

			// records of CPU load and pc power consumption
			InputThread  thInput  = new InputThread ( System.in, thePort.getOutputStream(), fout ); // 敺onsole銝ommand
			OutputThread thOutput = new OutputThread ( thePort.getInputStream(), System.out, fout, numofCollect ); // �Reply, 憿舐內�console蝡�
			executor.execute( thOutput );
			thInput.start();
			thOutput.start();
			executor.shutdown();

		} 
		catch ( IOException e ) {
			System.err.println( e );
		}
	}
	
	// Training formula
	public static boolean formulatrain( int numofCollect ) {
		int num = 0;
		String[] pairValue = new String[ numofCollect + 5 ];
		FormulaResult twovar = new FormulaResult();		// two variables

		// read log
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( "./" + numofCollect + "_training_samples.txt" ) ) );

			while ( ( pairValue[num] = reader.readLine() ) != null ) {
				
				
				String[][] spiltValue = new String[ numofCollect + 5 ][3];
				spiltValue[num] = pairValue[num].split( "[,\r\n]" );
				
				// two variables
				twovar.addDataPoint2( new DataPoint( Float.parseFloat(spiltValue[num][0]), Float.parseFloat(spiltValue[num][2]) ) );
				
				num++;
			}
			
			// calculate the two variables formula
			factorA[0] = twovar.get2VarA();
			factorA[1] = twovar.get2VarB1();
			factorB[0] = twovar.get2VarA50();
			factorB[1] = twovar.get2VarB50();
			System.out.println("\nTwo variables formula : y = " + factorA[0] + " + " + factorA[1] + " * x1");
			System.out.println("\nTwo variables formula 50 : y = " + factorB[0] + " + " + factorB[1] + " * x1");

		} catch ( IOException e ) {
			System.err.println( e );
		}

		return true;
	}
	
	// test result : formula result (y = a + b1*x1)
	public static void formulatest( double a, double b1 ) {

		// write log
		FileOutputStream fout2 = null;
		String[] pairValue = new String[ numofTest + 5 ];
		int num = 0;
		
		try {
			fout2 = new FileOutputStream( "./" + numofTrain + "_training_" + numofTest + "_testing_twovarLog.txt" );
			fout2.write( ("Formula : y = " + a + " + " + b1 + " * x1\r\n").getBytes() );
			fout2.write( ("=====CPU load (%)======Power Formula (Watt)======Power Meter (Watt)=====\r\n").getBytes() );

			if ( com.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
				
				// read from numofTest
				BufferedReader reader = new BufferedReader(new InputStreamReader( new FileInputStream( "./" + numofTest + "_testing_samples.txt" ) ) );

				while ( (pairValue[num] = reader.readLine()) != null ) {

					
					String[][] spiltValue = new String[numofTrain + 5][3];
					spiltValue[num] = pairValue[num].split("[,\r\n]");
					
					// CPU load
					cpuload = Double.parseDouble( spiltValue[num][0] );
					
					// pc power consumption from power meter
					powerMeter = Double.parseDouble( spiltValue[num][2] );

					num++;

					// estimate pc power consumption from formula
					powerformulaA = a + b1 * cpuload;
					System.out.println( cpuload + "(%)\t--> " + powerformulaAAvg + "(Watts)\t--> " + powerformulaA );
					
					// write log
					fout2.write( Double.toString( cpuload ).getBytes() );
					fout2.write( "\t--> ".getBytes() );
					fout2.write( Double.toString( powerformulaA ).getBytes() );
					fout2.write( "\t--> ".getBytes() );
					fout2.write( Double.toString( powerMeter ).getBytes() );
					fout2.write( "\r\n".getBytes() );
					
					
					cpuSumload += cpuload;
					cpuAvgload = cpuSumload / num;
					powerformulaAAvg = a + b1 * cpuAvgload;
				}
				
				cpuAvgload = cpuSumload / numofTest;
				powerformulaAAvg = a + b1 * cpuAvgload;
				fout2.write( ("==================================================\r\n").getBytes() );
				System.out.println("Final Avg: " + cpuAvgload + "(%)\t--> " + powerformulaAAvg + "(Watts)");
				System.out.println("\nFormula : y = " + a + " + " + b1 + " * x1");
			}
			
			fout2.close();
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

}