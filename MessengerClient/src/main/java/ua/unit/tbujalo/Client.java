package ua.unit.tbujalo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private String          name;
    private Socket          socket = null;
    private BufferedReader  in = null;
    private PrintWriter     out = null;
    private BufferedReader  stdIn = new BufferedReader(new InputStreamReader(System.in));

    public Client() {
        runClient();
    }

    private void runClient(){
        int	        port = 1024;
        String      host = "";
        String      line;

        System.out.println("Enter the server Host (in local machine - localhost)");
        try {
            host = stdIn.readLine();
            while (true) {
                System.out.println("Enter the server Port");
                line = stdIn.readLine();
                if (line.matches("\\d+")) {
                    port = Integer.parseInt(line);
                    if (port < 1024)
                        System.out.println("Enter port greater than 1024");
                    else {
                        break;
                    }
                }
            }
        }catch (IOException e){
            System.out.println("Cant read from command line");
        }
        System.out.println( "Loadig" );
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            System.err.println("Socket failed, try again");
        }
        if (socket == null){
            System.out.println("Can't connenct to "+host+":"+port);
            while (true){
                System.out.println("Enter --abort to exit, or --arain to try one more");
                String res = "";
                try {
                    res = stdIn.readLine();
                }catch (IOException ex){
                    System.out.println("Can't read from command line");
                    closeAll();
                }
                if ("--abort".equals(res)){
                    closeAll();
                    return;
                }else if ("--again".equals(res)){
                    runClient();
                }
            }
        }
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter your name: ");
            name = stdIn.readLine();
            out.println(name);
            System.out.println("You are successfully connected");
            System.out.println("Type '-exit' to close application");
            new WriteToServer().start();
            new ReadFromServer().start();
        }catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void closeAll(){
        if (socket != null)
            try {
                socket.close();
            }catch (IOException e){
                System.out.println("Cant close connection with server");
            }
        if (in != null)
            try {
                in.close();
            }catch (IOException e){
                System.out.println("Cant close reading from server");
            }
        if (out != null)
            out.close();
        try {
            stdIn.close();
        }catch (IOException e){
            System.out.println("Cants close STDIN");
        }
		System.exit(0);
    }

    public class ReadFromServer extends Thread {
        @Override
        public void run() {
            String messages;
            while (true) {
                try {
                    messages = in.readLine();
                    if (messages == null || messages.equals(name + "goodFromServer")) {
                        System.out.println("Good by");
                        closeAll();
                        break;
                    }
                    System.out.println(messages);
                } catch (IOException e) {
                    System.out.println("cant read from server");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
    public class WriteToServer extends Thread {
        @Override
        public void run() {
            String message;
            while (true){
                try {
                    message = stdIn.readLine();
                    out.println(message);
                    if (message.equals("-exit")){
                        break;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    System.out.println("cant write to server");
                    System.exit(1);
                }
            }
        }
    }
}
