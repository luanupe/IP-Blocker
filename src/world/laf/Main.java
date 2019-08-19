package world.laf;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Scanner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Main {
	
	public static void main(String[] args) throws Exception {
		JSONObject config = Main.readConfig();
		JSONArray listeners = config.getJSONArray("listeners");
		
		NetStatListener listener = new NetStatListener();
		Iterator<JSONObject> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			NetStatAnalyser analyser = new NetStatAnalyser(listener, iterator.next());
			analyser.start();
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
