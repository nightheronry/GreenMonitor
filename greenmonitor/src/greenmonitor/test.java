package greenmonitor;



public class test{
	
	public static void main(String[] args) {
		
		Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
		Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);

		System.out.println(batteryStatus); // Shows result of toString() method.
	}
}