package world.laf;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Main {
	
	public static boolean RUNNING = true;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.initAndRun();
	}
	
	private Map<Integer, NetStatAnalyser> analysers;
	private NetStatListener listener;
	
	private Main() {
		this.analysers = new HashMap<Integer, NetStatAnalyser>();
		this.listener = new NetStatListener();
	}
	
	private void initAndRun() {
		try {
			JSONObject config = this.readConfig();
			JSONArray listeners = config.getJSONArray("listeners");
			Iterator<JSONObject> iterator = listeners.iterator();
			
			while (iterator.hasNext()) {
				JSONObject portConfig = iterator.next();
				if ((!this.analysers.containsKey(portConfig.getInt("portToCheck")))) {
					NetStatAnalyser analyser = new NetStatAnalyser(this.listener, portConfig);
					this.analysers.put(analyser.getPortToCheck(), analyser);
					analyser.start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Type ? for help\n > ");
			Scanner scanner = new Scanner(System.in);
			String input = null;
			while ((input = scanner.nextLine()) != null) {
				this.handleInput(input.split(" "));
				System.out.print(" > ");
			}
		}
	}
	
	private void handleInput(String[] input) {
		if ((input.length > 0)) {
			if ((input[0].equalsIgnoreCase("exit"))) {
				Main.RUNNING = false;
				System.out.println("Good bye!");
				System.exit(0);
			} else if ((input[0].equals("?"))) {
				this.showHelpList();
			} else if ((input[0].equalsIgnoreCase("banList"))) {
				this.showBanList();
			} else {
				System.err.println("\tCommand not found! Type ? for help");
			}
		} else {
			System.err.println("\tInput is empty! Type ? for help");
		}
	}
	
	private void showHelpList() {
		StringBuilder builder = new StringBuilder();
		builder.append("\t").append("?").append("\t\t").append("Show the help;").append("\n");
		builder.append("\t").append("exit").append("\t\t").append("Stops this application;").append("\n");
		builder.append("\t").append("banList").append("\t\t").append("Print out all the IP address banned;").append("\n");
		System.out.println(builder.toString());
	}
	
	private void showBanList() {
		StringBuilder builder = new StringBuilder();
		for (Entry<Integer, NetStatAnalyser> analyser : this.analysers.entrySet()) {
			builder.append("--- [ Ban List for " + analyser.getKey() + " ] -----------------------").append("\n");
			String banList = analyser.getValue().getController().toString().trim();
			builder.append(banList.isEmpty() ? "\t- Ban list is Empty\n\n" : banList);
		}
		System.out.println(builder.toString());
	}
	
	private JSONObject readConfig() throws Exception {
		FileInputStream input = new FileInputStream(new File("res\\config.json"));
		Scanner reader = new Scanner(input, "UTF-8");
		String content = reader.useDelimiter("\\A").next();
		reader.close();
		return JSONObject.fromObject(content);
	}

}
