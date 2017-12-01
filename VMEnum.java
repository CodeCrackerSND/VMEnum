/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vmenum;
// It is a java console program, execute it by java -jar VMEnum.jar
// will dispaly java stack trace of select process and its system properties
// It require a jar reference (java path) to C:\Program Files (x86)\Java\jdk1.7.0_25\lib\tools.jar
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 *
 * @author Mihai
 */
public class VMEnum {

    private static final String CONNECTOR_ADDRESS =
          "com.sun.management.jmxremote.localConnectorAddress";
    
            private static JMXServiceURL getURLForPid(String pid) throws Exception {
            
            // attach to the target application
            final VirtualMachine vm = VirtualMachine.attach(pid);
            
            // get the connector address
            String connectorAddress =
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            
            // no connector address, so we start the JMX agent
            if (connectorAddress == null) {
                String agent = vm.getSystemProperties().getProperty("java.home") +
                        File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent);
                
                // agent is started, get the connector address
                connectorAddress =
                        vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                assert connectorAddress != null;
            }
            return new JMXServiceURL(connectorAddress);
        }
            

    private static int GetThisProcessID()
    {
try
{
java.lang.management.RuntimeMXBean runtime = 
    java.lang.management.ManagementFactory.getRuntimeMXBean();
java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
jvm.setAccessible(true);
sun.management.VMManagement mgmt =  
    (sun.management.VMManagement) jvm.get(runtime);
java.lang.reflect.Method pid_method =  
    mgmt.getClass().getDeclaredMethod("getProcessId");
pid_method.setAccessible(true);

int pid = (Integer) pid_method.invoke(mgmt);
return pid;
}
catch(Exception exc)
{
return -1;
}
    }
    
private static String ReadInputString()
{
try
{
// create a BufferedReader using System.in
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
String entered_str = "";
System.out.println("Write :index or process id");
System.out.println("and after that presss Enter key.");
do {
entered_str = br.readLine();
} while (entered_str==null||entered_str.isEmpty());
return entered_str;
}
catch(Exception exc)
{
return "";
}
    }

private static boolean ShouldPrintProperties()
{
try
{
// create a BufferedReader using System.in
BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
String entered_str = "";
System.out.println("Should print System Properties???");
System.out.println("Enter Y or YES to print or any other string to skip it:");
do {
entered_str = br.readLine();
} while (entered_str==null||entered_str.isEmpty());
entered_str = entered_str.toLowerCase();
if (entered_str.equalsIgnoreCase("Y")||entered_str.equalsIgnoreCase("YES"))
return true;
else
return false;
}
catch(Exception exc)
{
return false;
}
    }


public static final String RelativeToolPath = "\\lib\\tools.jar";

public static void AddLibToPath()
{
    try
    {

String jdk_path = System.getProperty("java.home");
int jre_index = jdk_path.lastIndexOf("\\jre");
if (jre_index!=-1&&jdk_path.endsWith("\\jre"))
jdk_path = jdk_path.substring(0,jre_index);

String tools_path = jdk_path+RelativeToolPath;
URL file_URL = new File(tools_path).toURI().toURL();

        URLClassLoader systemClassLoader = null;
        Method addURLMethod = null;
         try
         {
         systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
         addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
         addURLMethod.setAccessible(true);
         }
         catch (Exception e)
         {
          throw new IllegalStateException(e);
         }
        
         if (systemClassLoader==null||addURLMethod==null) return;
         
         addURLMethod.invoke(systemClassLoader, file_URL);
         

URLClassLoader Thread_cl = (URLClassLoader) (ClassLoader.getSystemClassLoader());
Class cls_test = Thread_cl.loadClass("com.sun.tools.attach.VirtualMachine");
if (cls_test!=null)
System.out.println("Load ok!");

    }
    catch(Exception exc)
    {
    exc.printStackTrace();
    }
}




    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

     AddLibToPath();
     MainProgram();
}
    
    public static void MainProgram()
    {
     
     int this_process_id = GetThisProcessID();
     System.out.println("This (vmenum) process id = "+String.valueOf(this_process_id));
     System.out.println("This enum may fail for some processes if so enter the process id manually!");
     System.out.println(":fallowed by process index and process id");
     System.out.println(":index, process id");
     int count = 1;
     List<String> vmlist = new ArrayList();
     for (VirtualMachineDescriptor vd : VirtualMachine.list())
     {
     System.out.println(":"+String.valueOf(count)+"  "+ vd.id());
     vmlist.add(vd.id());
     count++;

     }
     

String input_str = ReadInputString();
input_str = input_str.replaceAll(" ", "");
String newline = System.getProperty("line.separator");
if (newline.isEmpty()) newline = "\n";
input_str = input_str.replaceAll(newline, "");
    

if (input_str==null||input_str.isEmpty())
{
System.out.println("Nothing entered");
return;
}

String process_id = "";
String process_index = "";
if (input_str.startsWith(":"))
process_index = input_str.substring(1);
else if (input_str.length()==1)
process_index = input_str;
else
process_id = input_str;

if (!process_index.isEmpty())
{
try
{
int index = Integer.parseInt(process_index);
index--;
if (index>=0&&index<(count-1))
process_id =  vmlist.get(index);

}
catch(Exception exc)
{
System.out.println("Index="+process_index+" seems to be invalid!");
System.out.println(exc.toString());
}
}

System.out.println();

     try
     {
     try
     {
     VirtualMachine vm = VirtualMachine.attach(process_id);
     if (ShouldPrintProperties())
     {
     Properties props = vm.getSystemProperties();
     Enumeration e = props.propertyNames();
     
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      System.out.println(key + " = " + props.getProperty(key));
    }
    System.out.println();
     }
     }
     catch(Exception exc)
     {
     
     }
     
     JMXServiceURL service_url = getURLForPid(process_id);
     JMXConnector connector = JMXConnectorFactory.connect(service_url);  
     MBeanServerConnection connection = connector.getMBeanServerConnection();

      ThreadMonitor monitor = new ThreadMonitor(connection);
      monitor.threadDump();
      if (!monitor.findDeadlock())
      {
        System.out.println("No deadlock found.");
      }

     // get the connector address
     //String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
     int wtf = 1;
     

     
     }
     catch(Exception exc)
     {
     
     }
    }
    

}