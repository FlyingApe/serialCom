package nl.bastiaansierd.SerialComTester;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.*;
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
        private String lastReadJSONString;

        public SerialReader ( InputStream in)
        {
            this.in = in;
            src=new BufferedReader(new InputStreamReader(in));
        }

        public String getLastReadJSONString() {
            return lastReadJSONString;
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
                        System.out.println("JSON:"+json);
                        json="";
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }/*
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                String inputString;
                //int tagCounter = 0;

                StringBuilder bufferBuilder = new StringBuilder();
                StringBuilder jsonBuilder = new StringBuilder();

                bufferReader:
                while ((len = this.in.read(buffer)) > -1) {
                    String bufferedString = new String(buffer, 0, len);
                    //System.out.println("bS: " + bufferedString);

                    bufferBuilder.append(bufferedString);
                    inputString = bufferBuilder.toString();

                    //boolean write = false;
                    Scanner bufferScanner = new Scanner(inputString);

                    boolean loopedOnce = false;
                    int curlyBracketCounter = 0;
                    int minimumCurlyBracketCount = 1024;

                    while (bufferScanner.hasNextLine()) {
                        String bufferedLine = bufferScanner.nextLine();

                        char[] bufferedChars = bufferedLine.toCharArray();
                        for(int i = 0; i < bufferedChars.length; i++){
                            //find first full JSON string
                            if(bufferedChars[i] == '{'){
                                if(loopedOnce && curlyBracketCounter == minimumCurlyBracketCount){
                                }
                                curlyBracketCounter++;
                                if(curlyBracketCounter > minimumCurlyBracketCount){
                                    loopedOnce = true;

                                }
                            } else if(bufferedChars[i] == '}'){
                                curlyBracketCounter--;
                                if(curlyBracketCounter < minimumCurlyBracketCount){
                                    minimumCurlyBracketCount = curlyBracketCounter;
                                    loopedOnce = false;
                                    jsonBuilder = new StringBuilder();
                                }
                            }

                            if(loopedOnce){
                                jsonBuilder.append(bufferedChars[i]);
                                if(bufferedChars[i] == '}' && curlyBracketCounter == minimumCurlyBracketCount){
                                    String preTestLastReadJSONString = jsonBuilder.toString();

                                    System.out.println("JSON: " + preTestLastReadJSONString);
                                    try {
                                        JsonObject jsonTestObject = (JsonObject) Jsoner.deserialize(preTestLastReadJSONString);
                                        lastReadJSONString = preTestLastReadJSONString;
                                    } catch (JsonException e) {
                                        e.printStackTrace();
                                    }

                                    jsonBuilder = new StringBuilder();
                                    bufferBuilder = new StringBuilder();
                                    continue bufferReader;
                                }
                            }

                        }
                    }

                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }*/
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
