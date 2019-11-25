package ua.unit.tbujalo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MessengerServer{
	protected static List<ClientServer> clients = new LinkedList<>();
	protected static boolean run = true;
	private static Map<String, String> colors = new HashMap<>();
	static {
		colors.put("RED", "\u001B[31m");
		colors.put("GREEN", "\u001B[32m");
		colors.put("YELLOW", "\u001B[33m");
		colors.put("BLUE", "\u001B[34m");
		colors.put("PURPLE", "\u001B[35m");
		colors.put("CYAN", "\u001B[36m");
	}
	private static Map<String, String> availableCommands = new HashMap<>();
	static {
		availableCommands.put("server -help", "Show available commands");
		availableCommands.put("server/users -start_writing /<user_name>", "'server -start_write' is starting Write messages to file <to_day_date.txt>" +
				"or if users to <user_name_to_day_date.txt>");
		availableCommands.put("server/users -stop_writing /<user_name>", "'server -stop_write' is stopping the writing messages to file <to_day_date.txt>" +
				"or if users to <user_name_to_day_date.txt>");
		availableCommands.put("server/users -exit", "'server -exit' is close server, 'users -exit <user_name>' disconnect user from server.");
		availableCommands.put("server -list_users", "Show all users");
	}
	static final String ANSI_RESET = "\u001B[0m";
	private int port;
	private ServerSocket server;
	protected static boolean writeToFile = false;
	protected static File file = null;
	public MessengerServer() {
		this.port = 0;
	}

	private void runServer(){
		try{
			System.out.println(server);
			Iterator<Map.Entry<String, String>> iterator = colors.entrySet().iterator();
			while (run){
				System.out.println("Waiting for client");
				if (!iterator.hasNext())
					iterator = colors.entrySet().iterator();
				clients.add(new ClientServer(server.accept(), iterator.next().getValue()));
			}
		}catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}finally {
			closeSocket();
		}
	}

	private void closeSocket(){
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				System.out.println("Can't close server");
				System.exit(1);
			}
		}
		System.out.println("Server exit");
		System.exit(0);
	}

	private void doServerCommand(String cmd){
		switch (cmd){
			case "-help":{
				availableCommands.forEach((k, v) ->{
					System.out.println("-----------------------------------------------------------------------------------");
					String key = colors.get("YELLOW")+k+ANSI_RESET;
					String val = colors.get("CYAN")+v+ANSI_RESET;
					System.out.printf("| %-55s | %s\n", key, val);
						});
				break;
			}
			case "-exit":{
				closeSocket();
				break;
			}
			case "-start_writing": {
				writeToFile = true;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
				String date = sdf.format(new Date());
				file = new File(date + ".txt");
				if (!file.exists()) {
					try {
						if (file.createNewFile())
							System.out.println(date + ".txt is create. Writing to file is started.");
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Cant create file");
					}
				}
				break;
			}
			case "-stop_writing":{
				System.out.println("Writing is stopped ");
				writeToFile = false;
				file = null;
				break;
			}
			case "-list_users":{
				clients.forEach((k) -> System.out.println(colors.get("YELLOW")+k.getUserName()+ANSI_RESET));
				break;
			}
			default:{
				System.out.println("Unknown command to serve: "+cmd);
				break;
			}
		}
	}


	private void doSomethingWithUser(String userName, BiConsumer<List<ClientServer>, ClientServer> biConsumer){
		ListIterator<ClientServer> iterator = clients.listIterator();
		while (iterator.hasNext()) {
			ClientServer sc = iterator.next();
			if (sc.getUserName().equals(userName)){
				biConsumer.accept(clients, sc);
				return;
			}
		}
		System.out.println("Can't find user '"+userName+"'");
	}

	private void doUsersCommand(String cmd, String args){
		switch (cmd){
			case "-exit":{
				doSomethingWithUser(args, (list, elem) -> {
					boolean del;
					String name = elem.getUserName();
					elem.setRun(false);
					try {
						Thread.sleep(1000);
					}catch (InterruptedException e){

					}
					del = list.remove(elem);
					if (del)
					{
						System.out.println(name+" successfully disconnected");
						clients.forEach((clients-> clients.sendMessage(name+" was disconnected")));
					}
					else
						System.out.println("error disconnecting "+name);
				});
				break;
			}
			case "-start_writing":{
				doSomethingWithUser(args, (l, e) ->{
					e.setWrite(true);
					SimpleDateFormat date = new SimpleDateFormat("HH:mm:ss");
					String d = date.format(new Date())+"_"+e.getUserName()+".txt";
					File f = new File(d);
					if (!f.exists()){
						try {
							if (f.createNewFile()){
								e.setFile(f);
								System.out.println("File "+d+" successfully created");
							}
							else
								throw new IOException();
						}catch (IOException err){
							System.out.println("Cant create file: "+d);
						}
					}
				});
				break;
			}
			case "-stop_writing":{
				doSomethingWithUser(args, (l, e) -> {
					e.setWrite(false);
					e.setFile(null);
					System.out.println("Writing "+e.getUserName()+" messages  to file is stopped");
				});
				break;
			}
			default:{
				System.out.println("Unknown command to serve: "+cmd);
				break;
			}
		}
	}

	private void guessCommand(String []args){
		if (args.length == 2){
			if (args[0].equals("server")) {
				doServerCommand(args[1]);
				return;
			}
		}else if (args.length == 3){
			if (args[0].equals("users")) {
				doUsersCommand(args[1], args[2]);
				return;
			}
		}
		System.out.println("Unknown command");
	}

	protected void startServer(){
		new Thread(() -> {
			try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))){
				String line;
				while(run){
					System.out.println("Enter number of the port you wish");
					line = stdIn.readLine();
					if (line != null && line.matches("\\d+")){
						port = Integer.parseInt(line);
						if (port < 1024)
							System.out.println("Enter port greater than 1024");
						else{
							server = new ServerSocket(port);
							while (run){
								line = stdIn.readLine();
								guessCommand(line.split(" "));
							}
						}
					}
				}

			}catch (IOException e){
				e.printStackTrace();
				System.out.println("Can't read from console");
				run = false;
			}
		}).start();
		while (port < 1024){
			try {
				Thread.sleep(1000);
			}catch (InterruptedException e){

			}
		}
		System.out.println("-----------> DONE <---------\n\nTo See all available commands type 'server -help'.\n");
		runServer();

	}
}
