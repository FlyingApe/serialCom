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

    public ArduinoSerialConnection() {
        try {
            connect("COM6");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

                Thread Connector = new Thread(new Relay(ArduinoInputStream, ArduinoOutputStream));
                Connector.start();
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }


    public static class Relay implements Runnable
    {
        BufferedReader src;
        OutputStream arduinoOut;

        public Relay(InputStream in, OutputStream out)
        {
            src=new BufferedReader(new InputStreamReader(in));
            arduinoOut = out;

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
                            System.out.println("JSON: " + json);

                            //stuur over socket naar server of stub
                            Socket s = new Socket("localhost", 8888);
                            OutputStream serverOut = s.getOutputStream();
                            PrintWriter writer = new PrintWriter(serverOut);
                            writer.println(json);
                            writer.flush();

                            try {
                                InputStream serverIn = s.getInputStream();
                                Scanner scanner = new Scanner(serverIn);
                                while (scanner.hasNextLine()){
                                    String serverInput = scanner.nextLine();
                                    Byte b = 0;
                                    if(serverInput.equals("1")){
                                        b = 1;
                                    } else if(serverInput.equals("0")){
                                        b = 0;
                                    }

                                    System.out.println("buzzerOn : " + serverInput);
                                    arduinoOut.write(b);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }

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
}
