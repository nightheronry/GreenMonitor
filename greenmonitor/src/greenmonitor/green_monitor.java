package greenmonitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import greenmonitor.batterydetector;

import org.jfree.data.category.DefaultCategoryDataset;


public class green_monitor extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static DefaultCategoryDataset staticdefaultcategorydataset = new DefaultCategoryDataset();
	//public batterydetector mybatterydetector = new batterydetector();
	Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
	static boolean[] setswitch;
	double oldwatt=0;
	
	static green_monitor w = new green_monitor();
	static OutputThread thOutput;
	static java.util.Timer timer = new java.util.Timer();
	static greenmonitor.CPUMeter mycpumeter = new greenmonitor.CPUMeter();

	private JPanel panel;
	private JLabel Label,CPUload,Showcpu,Formula,Comwatt,Showcomwatt,Realwatt,Showrealwatt,lampswitch, hairdryerswitch, fanswitch, airfreshenerswitch, Computerswitch;
	private final int ww=880,wh=670;
	static DecimalFormat format=new DecimalFormat("#0.0");
	double cpuload=0.0,c=0.0;
	
	public green_monitor() {
		setTitle("CPU Meter");
		setSize(ww,wh);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildPanel();
		add(panel);
		setVisible(true);
        
    }
     
    public void turnon(JLabel appswitch){
       appswitch.setVisible(false);
   	   appswitch.repaint();
    }
    
    public void turnoff(JLabel appswitch){
	   appswitch.setVisible(true);
	   appswitch.repaint();
    }
  
    
    public class DateTask extends TimerTask
   	{
   		@ Override
   		public void run()
   		{
   			
   			String watt = thOutput.get_watt();
   			String amps = thOutput.get_amps();
   			System.out.println("讀取數值：");
   			System.out.println(watt+" W  "+amps+" mA\n");
   			System.out.println(thOutput.get_log());
   			double x=0.0, y=0.0, x1=0.0,y1=0.0, x50=0.0, y50=0.0;
   			FileReader reader;
   	    	String result="";
   			try {
   				reader = new FileReader("result.txt");
   				BufferedReader bufReader = new BufferedReader(reader);
   				try {
   					result = bufReader.readLine();
   					//System.out.println(result);
   				} catch (IOException e1) {
   					e1.printStackTrace();
   				}
   			} catch (FileNotFoundException e1) {
   				e1.printStackTrace();
   			}
   			String[] readresult = result.split(" ");
   			x1=Double.parseDouble(readresult[0]);
   			y1=Double.parseDouble(readresult[1]);
   			x50=Double.parseDouble(readresult[2]);
   			y50=Double.parseDouble(readresult[3]);
   			
   			//取得並判斷cpuload，以便帶入x, y
   	    	cpuload=Double.parseDouble(mycpumeter.getCPUload());
   	    	   	
   	    	System.out.println("CPUload:"+cpuload+" "+x+" "+y+" "+x50+ " "+y50);
   	    	if(cpuload>50.0){                                     
   	    	   x=x50;
   	    	   y=y50;
   	    	}else {
   	    		x=x1;
   	    	    y=y1;
   	    	}
   	    	//計算電腦瓦數並從總瓦數上扣除
   	    	c=cpuload*x+y;
   	    	
   	    	
   	    	w.Formula.setText("Formula :      "+format.format(cpuload) + "  *  " +format.format(x) + "  +  " + format.format(y));
   	    	w.Showrealwatt.setText(String.valueOf(watt));   	
   	    	w.Showcpu.setText(format.format(cpuload) +" %");
   	    	w.Showcomwatt.setText(format.format(c));
   	    	Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
   	    	if(format.format(batteryStatus)=="Online")
   	    		w.turnon(w.Computerswitch);
   	    	
   			if(Math.abs(Double.parseDouble(watt)-oldwatt) >= 5){
   				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
   				watt = thOutput.get_watt();
	   			setswitch=new boolean[5]; //lamp hairdryer airfreshener fan ETS-500
	   			for(int i=0;i<5;i++)
	   				setswitch[i]=false;
	   			decide(Double.parseDouble(watt));
	   			oldwatt=Double.parseDouble(watt);
	   			long startTime;
	   			startTime = System.currentTimeMillis();
				Long spentTime = System.currentTimeMillis() - startTime;
				Long seconds = (spentTime/1000) % 60;
				
				addchartpoint(Double.parseDouble(watt), Double.parseDouble(amps), seconds.toString());
	   			this.scheduledExecutionTime();
   			}
   			//thOutput.stopThread();
   		}
   	}
    void addchartpoint(Double watt, Double amps, String second){
    	//if(second=="50.0") 
    	staticdefaultcategorydataset.addValue(watt, "First", second); //加值 ( watt , type, second) 
    	staticdefaultcategorydataset.addValue(amps, "Second", second);//加值 ( amps , type, second)
    }
    public  void decide(double watt){
    	
    	//double cpuload=0.0;
    	double l=18.2,h=1120.0,a=108.3,f=54.0,e=9.4,pere=10;//電器穩態watt及參數
    	
    	Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
	    if(format.format(batteryStatus)=="Offline"){
    	}
    	else{
    		watt-=c;
    		setswitch[4]=true;
    	}
    	
    	System.out.println("修正後watt:"+watt);
    	if(watt>h-pere){
    		setswitch[1]=true;
    		watt-=h;
    	}
    	
    	if(watt>a-pere){
    		setswitch[2]=true;
    		watt-=a;
    	}
    	
    	if(watt>f-pere){
    		setswitch[3]=true;
    		watt-=f;
    	}
    	
    	if(watt>l-pere){
    		setswitch[0]=true;
    		watt-=l;
    	}
    	/*
    	if(watt>e-pere){
    		setswitch[4]=true;
    		watt-=e;
    	}*/
    	
    	
    	if(setswitch[0]==true)
    		w.turnon(w.lampswitch);
    	else
    		w.turnoff(w.lampswitch);
    	
    	if(setswitch[1]==true)
    		w.turnon(w.hairdryerswitch);
    	else
    		w.turnoff(w.hairdryerswitch);
    	
    	if(setswitch[2]==true)
    		w.turnon(w.airfreshenerswitch);
    	else
    		w.turnoff(w.airfreshenerswitch);
    	
    	if(setswitch[3]==true)
    		w.turnon(w.fanswitch);
    	else
    		w.turnoff(w.fanswitch);
    	
    	if(setswitch[4]==true)
    		w.turnon(w.Computerswitch);
    	else
    		w.turnoff(w.Computerswitch);
    	
    }
    
    
    
    public static void main(String[] args) {
        	
    	 SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                //w.turnon(w.hairdryerswitch);
                //w.turnoff(w.hairdryerswitch);
                //AWTUtilities.setWindowOpacity(w, 0.9f);
                
            }    		
        });
    	 
    	 
        byte[] buffer = ("#L,W,3,E,0,1;").getBytes();
		// 連接 COM Port 
        
		try{
			//CommPortIdentifier com = CommPortIdentifier.getPortIdentifier( args[0] );
			CommPortIdentifier com = CommPortIdentifier.getPortIdentifier( "COM8" );  // 電表固定
			
			if( com.getPortType() == CommPortIdentifier.PORT_SERIAL )
			{
				try{
					SerialPort thePort = (SerialPort) com.open( "GreenProject", 10 ); // 
					thePort.setSerialPortParams( 115200, 8, 1, 0); // baud, dataBits, stopBits, parity
					
					thePort.getOutputStream().write(buffer);//下Command
					
					thOutput = new OutputThread( thePort.getInputStream(), System.out); // 收Reply, 顯示在console端
					thOutput.start();
					//thOutput.stopThread();
				}
				catch( PortInUseException e ) {}
			}
		}
		catch(Exception e ){
			System.out.println( e );
			System.exit(0);
		}
		
		//while(true){
			w.ScheduleDelay();
		//}
		
    }
    
    void ScheduleDelay(){
    	
           Timer timer = new Timer();
           timer.schedule(new DateTask(), 3000 , 1000);
    	         
         try {
                 Thread.sleep(3000);
            }
              catch(InterruptedException e) {
           }
       }
       


    public void buildPanel()
	{
		Label=new JLabel(new ImageIcon("src/background.png"));
		CPUload=new JLabel();
		Showcpu=new JLabel();
		Formula=new JLabel();
		Comwatt=new JLabel();
		Showcomwatt=new JLabel();
		Realwatt=new JLabel();
		Showrealwatt=new JLabel();
		
		Computerswitch=new JLabel(new ImageIcon("src/off.png"));
		lampswitch=new JLabel(new ImageIcon("src/off.png"));
		fanswitch=new JLabel(new ImageIcon("src/off.png"));
		airfreshenerswitch=new JLabel(new ImageIcon("src/off.png"));
		hairdryerswitch=new JLabel(new ImageIcon("src/off.png"));
	
		panel = new JPanel();
	
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((d.width-this.getWidth())/2,(d.height-this.getHeight())/2);
	
		panel.setLayout(null);
		panel.setBackground(new Color(255,255,255));
	
		Label.setBounds(0,0,ww-15,wh-40);
	
		CPUload.setFont(new Font("Calibri", Font.BOLD, 44));
		CPUload.setBounds(675,190,600,100);
		CPUload.setText("CPU Load");
		CPUload.setForeground(Color.BLACK);
	
		Showcpu.setFont(new Font("Calibri", Font.BOLD, 52));
		Showcpu.setBounds(705,260,600,100);
		Showcpu.setForeground(Color.BLACK);
	
		Formula.setFont(new Font("Calibri", Font.BOLD, 30));
		Formula.setBounds(250,405,600,100);
		Formula.setForeground(Color.WHITE);
		
		Comwatt.setFont(new Font("Calibri", Font.BOLD, 48));
		Comwatt.setBounds(400,230,600,100);
		Comwatt.setText("Com Watt" );
		Comwatt.setForeground(Color.BLACK);
	
		Showcomwatt.setFont(new Font("Calibri", Font.BOLD, 60));
		Showcomwatt.setBounds(425,320,600,100);
		Showcomwatt.setForeground(Color.BLACK);

		Realwatt.setFont(new Font("Calibri", Font.BOLD, 70));
		Realwatt.setBounds(50,160,600,100);
		Realwatt.setForeground(Color.BLACK);
		Realwatt.setText("Real Watt");
		
		Showrealwatt.setFont(new Font("Calibri", Font.BOLD, 80));
		Showrealwatt.setBounds(120,280,600,100);
		Showrealwatt.setForeground(Color.BLACK);
		
		Computerswitch.setBounds(10,472,160,160);
		lampswitch.setBounds(190,472,160,160);
		fanswitch.setBounds(370,472,160,160);
		airfreshenerswitch.setBounds(540,472,160,160);
		hairdryerswitch.setBounds(700,472,160,160);
	
		panel.add(CPUload);
		panel.add(Showcpu);
		panel.add(Formula);
		panel.add(Comwatt);
		panel.add(Showcomwatt);
		panel.add(Realwatt);
		panel.add(Showrealwatt);
		panel.add(Computerswitch);
		panel.add(lampswitch);
		panel.add(fanswitch);
		panel.add(airfreshenerswitch);
		panel.add(hairdryerswitch);
	
		panel.add(Label);
	}

}



class OutputThread extends Thread{
	
	InputStream theInput ;
	OutputStream theOutput;

	String[] read_in_data;
	double read = -1;
	String watt="";
	String amps="";
	String strLog = "";
	byte[] buffer = new byte[256];
	private boolean isRunning = true;
	
	OutputThread ( InputStream in, OutputStream out )//建構子
	{
		theInput = in;
		theOutput= out;
	}
	public void stopThread() 
    {
        this.isRunning = false;
    }
	public void restart() 
    {
        this.isRunning = true;
    }
	public String get_watt()
	{
		return watt;
	}
	public String get_amps()
	{
		return amps;
	}
	public String get_log()
	{
		return strLog;
	}
	
	public void run()
	{		
		try
		{
			while( isRunning )
			{
				int bytesRead = theInput.read(buffer);
				if( bytesRead == -1 )
				{
					break;
				}
				//從電表上讀出來的Log
				strLog = new String(buffer,0,bytesRead ) ;
	
				
				if(strLog.length()>=3)
				{						
					if(strLog.substring(0,2).equals("#d"))//如果讀出來的Log是電器的資訊，進行字串切割
					{
						String[] read_in_data = strLog.split(",");
						
						if(read_in_data.length >= 6)//確保有讀到瓦特數
						{
							/*
							if((Double.parseDouble(read_in_data[3]) < 30))//讀進來的不是瓦特數
								continue;
								*/
							//印出瓦特數
							//System.out.println("test Watt:"+read_in_data[3]);
							if(read_in_data[3]!="_")
								watt = (Double.parseDouble(read_in_data[3])/10)+"";
							
							if(read_in_data[5]!="_")
								amps = read_in_data[5];
						}
					}
				}	
				
			}
		}
		catch( IOException e )
		{
			System.err.println( e + strLog);
		}
	}
}

