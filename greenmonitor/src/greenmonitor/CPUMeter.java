package greenmonitor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import javax.swing.*;


public class CPUMeter {
	DecimalFormat f=new DecimalFormat("#0.0");
	
	/**
	 * @param args
	 */

	public String getCPUload(){
		return f.format(Double.parseDouble(printUsage())*100);
	}
	private static String printUsage() {
		String str ="";
		  OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		  for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
		    method.setAccessible(true);
		    if (method.getName().startsWith("get") 
		        && Modifier.isPublic(method.getModifiers())) {
		            Object value;
		        try {
		            value = method.invoke(operatingSystemMXBean);
		        } catch (Exception e) {
		            value = e;
		        } 
		        if (method.getName().equals("getSystemCpuLoad"))
		        	str = value +"";

		    } 
		  } 
		  return str;
		}	
}