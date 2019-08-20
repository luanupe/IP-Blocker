package world.laf;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Main {
	
	public static void main(String[] args) throws Exception {
		JSONObject config = Main.readConfig();
		JSONArray listeners = config.getJSONArray("listeners");

		Map<Integer, NetStatAnalyser> analysers = new HashMap<Integer, NetStatAnalyser>();
		NetStatListener listener = new NetStatListener();
		Iterator<JSONObject> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			JSONObject portConfig = iterator.next();
			if ((!analysers.containsKey(portConfig.getInt("portToCheck")))) {
				NetStatAnalyser analyser = new NetStatAnalyser(listener, portConfig);
				analyser.start();
			}
		}
	}
	
	private static JSONObject readConfig() throws Exception {
		FileInputStream input = new FileInputStream(new File("res\\config.json"));
		Scanner reader = new Scanner(input, "UTF-8");
		String content = reader.useDelimiter("\\A").next();
		reader.close();
		return JSONObject.fromObject(content);
	}

}
