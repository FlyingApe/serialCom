package nl.bastiaansierd.SerialComTester;

public class Main {
    public static void main ( String[] args )
    {
        try
        {
            ArduinoSerialConnection.getInstance();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
