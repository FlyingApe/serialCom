package nl.bastiaansierd.mockServer;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MockServer {
    public static void main(String[] args) throws IOException {
        final int SBAP_PORT = 8888;
        ServerSocket server = new ServerSocket(SBAP_PORT);
        System.out.println("Waiting for clients to connect...");
        while(true) {
            Socket s = server.accept();
            System.out.println("Client connected.");
            TestService testService = new TestService(s);
            Thread t = new Thread(testService);
            t.start();
        }
    }

    public static class TestService implements Runnable{
        private Socket socket;

        public TestService(Socket s) {
            socket = s;
        }

        public void run(){
            String json="";

            try {
                InputStream in = socket.getInputStream();
                Scanner scanner = new Scanner(in);

                while (scanner.hasNextLine()) {
                    System.out.println("line: " + scanner.nextLine());
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

