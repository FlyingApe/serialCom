package nl.bastiaansierd.SerialComTester;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ArduinoSerialConnection{
    private static ArduinoSerialConnection instance = null;
    public static ArduinoSerialConnection getInstance(){
        /* singelton initialisatie*/
        if(instance == null){
            instance = new ArduinoSerialConnection();
        }
        return instance;
    }

    private Thread arduinoInputThread;

    public ArduinoSerialConnection() {
        try {
            connect("COM6");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Thread getArduinoInputThread(){ return arduinoInputThread;}

    private void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

            if ( commPort instanceof SerialPort)
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

                InputStream ArduinoInputStream = serialPort.getInputStream();
                OutputStream ArduinoOutputStream = serialPort.getOutputStream();

                arduinoInputThread = new Thread(new SerialReader(ArduinoInputStream));
                arduinoInputThread.start();
                new Thread(new SerialWriter(ArduinoOutputStream)).start();
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }


    public static class SerialReader implements Runnable
    {
        InputStream in;
        BufferedReader src;

        public SerialReader ( InputStream in)
        {
            this.in = in;
            src=new BufferedReader(new InputStreamReader(in));
        }

        public void run ()
        {
            String json="";
            while (true) {
                try {
                    String line=src.readLine();
                    json+=line;
                    if (line.trim().equals("}")) {
                        // process json string
                        try {
                            JsonObject jsonTestObject = (JsonObject) Jsoner.deserialize(json);
                            //jsonTestObject.
                            System.out.println("JSON: " + json);

                            //stuur over socket naar server of stub
                            Socket s = new Socket("localhost", 8888);
                            InputStream serverIn = s.getInputStream();
                            OutputStream serverOut = s.getOutputStream();
                            PrintWriter writer = new PrintWriter(serverOut);
                            writer.println(json);
                            writer.flush();

                            s.close();

                        } catch (JsonException e) {
                            //e.printStackTrace();
                        }
                        json="";
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    private static class SerialWriter implements Runnable
    {
        OutputStream out;

        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }

        public void run ()
        {
            try
            {
                int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
}
