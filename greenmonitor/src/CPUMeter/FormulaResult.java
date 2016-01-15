package CPUMeter;
/**
 * A least-squares regression line function.
 */
import java.util.*;
import java.math.BigDecimal;


//雙變數：y = a + b1*x1 
///多變數：y = a + b1*x1 + b2*x2

public class FormulaResult {
	// 單變數之迴歸
	private double sumY = 0;
	private double sumX1 = 0;
	private double sumX1X1 = 0;
	private double sumX1Y = 0;
	private double sumYY = 0;
	
	private double sumY50 = 0;
	private double sumX50 = 0;
	private double sumX50X50 = 0;
	private double sumX50Y50 = 0;
	private double sumY50Y50 = 0;
	
	private int YMin = 0;
	private int YMax = 0;
	private int X1Min = 0;
	private int X1Max = 0;
	
	private int Y50Min = 0;
	private int Y50Max = 0;
	private int X50Min = 0;
	private int X50Max = 0;
	
	private int numPoint;		/** number of data points */
	private int numPoint50;		/** number of data points */
	private boolean coefsValid;	/** true if coefficients valid */
	
	private ArrayList<String> listX;	// CPU
	private ArrayList<String> listY;	// Memmory
	private ArrayList<String> listZ;	// Power
	
	private ArrayList<String> listX50;	// CPU
	private ArrayList<String> listZ50;	// Power
	
	private float a;			/** line coefficient a **/
	private float b1;			/** line coefficient b1 **/
	private String[] xyz;
	
	private float a50;			/** line coefficient a **/
	private float b50;			/** line coefficient b1 **/
	private String[] xyz50;
	
	/**
	 * Constructor.
	 * @param data the array of data points
	 */
	public FormulaResult( DataPoint data[] ) { 
		numPoint = 0;
		numPoint50 = 0;
		xyz = new String[3];
		xyz50 = new String[3];
		listX = new ArrayList<String>();
		listY = new ArrayList<String>();
		listZ = new ArrayList<String>();
		listX50 = new ArrayList<String>();
		listZ50 = new ArrayList<String>();
		
		if ( data.length == 2 ) {
			for ( int i = 0; i < data.length; ++i ) {
				addDataPoint2(data[i]);
			}
		}		
	}
	
	public FormulaResult() {
		X1Max = 0;
		YMax = 0;
		numPoint = 0;
		xyz = new String[3];
		listX = new ArrayList<String>();
		listY = new ArrayList<String>();
		listZ = new ArrayList<String>();
		
		X50Max = 0;
		Y50Max = 0;
		numPoint50 = 0;
		xyz50 = new String[3];
		listX50 = new ArrayList<String>();
		listZ50 = new ArrayList<String>();
	}
	
	/**
	 * Add a new data point: Update the sums.
	 * @param dataPoint the new data point
	 */
	public void addDataPoint2( DataPoint dataPoint ) {
		if ( dataPoint.x < 50.0 ){
			sumX1  += dataPoint.x;
			sumX1X1 += dataPoint.x*dataPoint.x;
			sumX1Y += dataPoint.x*dataPoint.z;
			sumY  += dataPoint.z;
			sumYY += dataPoint.z*dataPoint.z;

			if ( dataPoint.x > X1Max ) {
				X1Max = (int)dataPoint.x;
			}
			if ( dataPoint.z > YMax ) {
				YMax = (int)dataPoint.z;
			}

			// 把每個點的具體座標存入 ArrayList中，備用。
			xyz[0] = (int)dataPoint.x+ "";
			xyz[2] = (int)dataPoint.z+ "";
			if ( dataPoint.x !=0 && dataPoint.z != 0 ) {
				try {
					listX.add( numPoint, xyz[0] );
					listZ.add( numPoint, xyz[2] );
				} 
				catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		
			++numPoint;
		}
		else {
			  sumX50  += dataPoint.x;
		  	  sumX50X50 += dataPoint.x*dataPoint.x;
			  sumX50Y50 += dataPoint.x*dataPoint.z;
			  sumY50  += dataPoint.z;
			  sumY50Y50 += dataPoint.z*dataPoint.z;

			  if ( dataPoint.x > X50Max ) {
				  X50Max = (int)dataPoint.x;
			  }
			  if ( dataPoint.z > Y50Max ) {
				  Y50Max = (int)dataPoint.z;
			  }

			  // 把每個點的具體座標存入 ArrayList中，備用。
			  xyz50[0] = (int)dataPoint.x+ "";
			  xyz50[2] = (int)dataPoint.z+ "";
			  if ( dataPoint.x !=0 && dataPoint.z != 0 ) {
				  try {
					  listX50.add( numPoint50, xyz50[0] );
					  listZ50.add( numPoint50, xyz50[2] );
				  } 
				  catch ( Exception e ) {
					  e.printStackTrace();
				  }
			  }
			
			  ++numPoint50;
		}
		coefsValid = false;
	}
	
	// 計算迴歸方程式係數
	/**
	 * Validate the coefficients.
	 * 計算雙變數一次迴歸方程式係數 y = a + b1*x1 中的 a b1
	 */
	private void validate2Coefficients() {
		if ( coefsValid ) return;

		if ( numPoint >= 2 ) {
			float yBar  = (float) sumY  / numPoint;
			float x1Bar = (float) sumX1 / numPoint;
			
			b1 = (float) ( ( numPoint * sumX1Y - sumX1 * sumY ) / ( numPoint * sumX1X1 - sumX1 * sumX1 ) );
			a  = (float) ( yBar - b1 * x1Bar );
		} 
		else {
			a = b1 = Float.NaN;
		}
		
		if ( numPoint50 >= 2 ) {
			float yBar50  = (float) sumY50  / numPoint50;
			float x1Bar50 = (float) sumX50 / numPoint50;
			
			b50 = (float) ( ( numPoint50 * sumX50Y50 - sumX50 * sumY50 ) / ( numPoint50 * sumX50X50 - sumX50 * sumX50 ) );
			a50  = (float) ( yBar50 - b50 * x1Bar50 );
		} 
		else {
			a50 = b50 = Float.NaN;
		}
		coefsValid = true;
	}
	
	// 取得迴歸方程式係數
	/**
	 * Return the equation y = a + b1*x1 of coefficient a.
	 * @return the value of a
	 */
	public float get2VarA() {
		validate2Coefficients();
		return a;
	}
	
	public float get2VarA50() {
		validate2Coefficients();
		return a50;
	}

	/**
	 * Return the equation y = a + b1*x1 of coefficient b1.
	 * @return the value of b1
	 */
	public float get2VarB1() {
		validate2Coefficients();
		return b1;
	}
	
	public float get2VarB50() {
		validate2Coefficients();
		return b50;
	}
	
	// 取得計算過程變數
	/** Return the current number of data points.**/
	public int getDataPointCount() { 
		return numPoint; 
	}
	
	public int getDataPointCount50() { 
		return numPoint50; 
	}
	
	/**Return the sum of the y values.**/
	public double getSumY() { 
		return sumY; 
	}
	
	/**Return the sum of the x1 values.**/
	public double getSumX() { 
		return sumX1; 
	}

	/**Return the sum of the x1*x1 values.**/
	public double getSumXX() { 
		return sumX1X1; 
	}

	/**Return the sum of the x1*y values.**/
	public double getSumXY() { 
		return sumX1Y; 
	}

	/**Return the sum of the y*y values.**/
	public double getSumYY() { 
		return sumYY; 
	}

	public int getYMin() {
		return YMin;
	}

	public int getYMax() {
		return YMax;
	}
	
	public int getX1Min() {
		return X1Min;
	}

	public int getX1Max() {
		return X1Max;
	}
	
	/*// 實現更精確的四捨五入
	public float round( float v, int scale ) {

		if ( scale < 0 ) {
			throw new IllegalArgumentException( "The scale must be a positive integer or zero" );
		}

		BigDecimal b = new BigDecimal( Double.toString(v) );
		BigDecimal one = new BigDecimal( "1" );
		return b.divide( one, scale, BigDecimal.ROUND_HALF_UP ).floatValue();
	}*/
	
	// Reset
	public void reset() {
		numPoint = 0;
		numPoint50 = 0;
		sumX1 = sumX1X1 = sumX1Y = sumY = sumYY = 0;
		sumX50 = sumX50X50 = sumX50Y50 = sumY50 = sumY50Y50 = 0;
		coefsValid = false;
	}
	
}

