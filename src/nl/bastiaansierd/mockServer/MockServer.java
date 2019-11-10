package nl.bastiaansierd.mockServer;

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
                OutputStream out = socket.getOutputStream();

                Scanner scanner = new Scanner(in);
                PrintWriter writer = new PrintWriter(out);

                if(scanner.hasNextLine()){
                    System.out.println("line: " + scanner.nextLine());
                    Byte b = 0;
                    writer.print(b);
                    writer.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

