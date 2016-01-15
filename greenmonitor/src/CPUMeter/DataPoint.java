package CPUMeter;
/**
 * A data point for interpolation and regression.
 */
public class DataPoint {
	public float x;		/** the x represents the CPU load */  
	public float y;		/** the y represents the Memmory load */  
	public float z;		/** the z represents the Power */

	/**
	 * Constructor.
	 * @param x the x value
	 * @param y the y value
	 */
	public DataPoint(float x, float z) {
		this.x = x;
		this.z = z;
	}
	
	/*public DataPoint(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}*/
}