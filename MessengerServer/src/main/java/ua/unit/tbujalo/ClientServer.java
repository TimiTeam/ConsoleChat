package ua.unit.tbujalo;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientServer extends Thread{
    private Socket socket;
    private String userName;
    private BufferedReader in;
    private PrintWriter out;
    private String color;
    private boolean run;
    protected boolean write;
    protected File file;

    public ClientServer(Socket socket, String color){
        this.socket = socket;
        this.color = color;
        this.run = true;
        this.write = false;
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        }catch (IOException e){
            e.printStackTrace();
        }
        this.start();
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public String getUserName() {
        return userName;
    }


    public void sendMessage(String message){
        out.println(message);
    }

    private void closeAll(){
        try {
            if (socket != null)
                socket.close();
            if (in != null)
                in.close();
            if (out != null)
                out.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void sendMessageToAll(String mes){
        MessengerServer.clients.forEach((client)-> {
            if (!client.equals(this))
                    client.sendMessage(mes);
        });
    }

    public void writeToFile(String filePth, String content){
        try {
            Files.write(Paths.get(filePth), content.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e){
            System.out.println("Can't write to file: "+filePth);
        }
    }

    @Override
    public void run() {
        try {
            String line;
            this.userName = in.readLine();
            if (userName == null){
                System.out.println("can't get message from user");
                return;
            }
            sendMessageToAll("New user connected "+userName);
            System.out.println(userName+" connected");
            while (run){
                line = in.readLine();

                SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss");
                String date = dt.format(new Date());
                String str = date+" ["+userName+"]: "+line+"\n";

                if (MessengerServer.writeToFile && MessengerServer.file != null)
                    writeToFile(MessengerServer.file.getAbsolutePath(), str);
                if (write && file != null)
                    writeToFile(file.getAbsolutePath(), str);

                if (!MessengerServer.run || !run || line == null || line.equals("exit")){
                    sendMessage(userName+"goodFromServer");
                    sendMessageToAll(userName+" is leave dialog");
                    this.closeAll();
                    MessengerServer.clients.remove(this);
                    break;
                }
                sendMessageToAll(date+"["+this.color+userName+MessengerServer.ANSI_RESET+"]: "+line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
