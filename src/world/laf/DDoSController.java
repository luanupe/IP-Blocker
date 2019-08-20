package world.laf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import net.sf.json.JSONObject;

public class DDoSController {
	
	private static final Map<Integer, JSONObject> BAN_LIST = new HashMap<Integer, JSONObject>();
	private JSONObject banList;
	private Map<String, DDoSClient> clients;
	private int port, connections, timeout;
	
	protected DDoSController(int port, int connections, int timeout) throws Exception {
		this.clients = new HashMap<String, DDoSClient>();
		this.port = port;
		this.connections = connections;
		this.timeout = timeout;
		this.init();
	}
	
	private void init() throws Exception {
		synchronized (DDoSController.BAN_LIST) {
			JSONObject banList = DDoSController.BAN_LIST.get(this.port);
			if ((banList == null) || (banList.isNullObject())) {
				File file = new File("res\\block\\" + this.port + ".json");
				if ((file.exists())) {
					FileInputStream input = new FileInputStream(file);
					Scanner reader = new Scanner(input, "UTF-8");
					String content = reader.useDelimiter("\\A").next();
					reader.close();
					banList = JSONObject.fromObject(content);
				} else {
					banList = new JSONObject();
				}
			}
			
			DDoSController.BAN_LIST.put(this.port, banList);
			this.banList = banList;
		}
	}
	
	protected void run(NetStatProtocol stat) {
		DDoSClient client = this.getClient(stat.getRemoteAddress());
		client.run(stat.getRemotePort());
	}
	
	protected void finish() {
		this.unban();
		this.ban();
		
		this.clients.clear();
		this.clients = null;
	}
	
	private void ban() {
		for (DDoSClient client : this.clients.values()) {
			if ((!client.isOK())) this.ban(client);
		}
	}
	
	private void ban(DDoSClient client) {
		try {
			// Adiciona a lista de banimentos se não já estiver banido
			JSONObject ban = this.banList.getJSONObject(client.getAddress());
			if ((ban == null) || (ban.isNullObject())) {
				String name = client.getBanFirewall(this.port);
				this.log(client.getTotal() + " connections... Black listing " + name);
				
				// Roda o cmd pra ADICIONAR a regra no firewall
				Process p = Runtime.getRuntime().exec("cmd /c netsh advfirewall firewall add rule name=\"" + name + "\" dir=in protocol=any interface=any action=block remoteip=" + client.getAddress()); 
	            p.waitFor();
	            
	            // Cria o histórico pra salvar no arquivo
				ban = new JSONObject();
				ban.put("name", name);
				ban.put("connections", client.getTotal());
				ban.put("address", client.getAddress());
				ban.put("port", this.port);
				
				// Atualiza o tempo do banimento (se já estiver banido simplesmente recomeça a contagem)
				ban.put("timestamp", System.currentTimeMillis());
				this.banList.put(ban.getString("address"), ban);
				this.saveBanList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void unban() {
		Map<String, JSONObject> results = new HashMap<String, JSONObject>();
		Iterator<String> keys = this.banList.keys();
		
		// Precisei duplicar a lista pra evitar Exception de acesso simultâneo
		while(keys.hasNext()) {
			String key = keys.next();
			JSONObject ban = this.banList.getJSONObject(key);
			long banExpires = ban.getLong("timestamp") + (this.timeout * ban.getInt("connections"));
			if ((System.currentTimeMillis() >= banExpires)) results.put(key, ban);
		}
		
		// Remover a regras do firewall e IPs da banList
		for (Map.Entry<String, JSONObject> result : results.entrySet()) {
			try {
				JSONObject ban = this.banList.getJSONObject(result.getKey());
				this.log("Removing address from black list: " + result.getKey());
				
				// Roda o cmd pra REMOVER a regra do firewall
				Process p = Runtime.getRuntime().exec("cmd /c netsh advfirewall firewall delete rule name=\"" + ban.getString("name") + "\""); 
	            p.waitFor();
				
				// Remove da lista de banimentos
				this.banList.remove(result.getKey());
				this.saveBanList();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveBanList() {
		try {
			PrintWriter out = new PrintWriter("res//block//" + this.port + ".json");
			out.print(this.banList.toString());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			synchronized (DDoSController.BAN_LIST) {
				DDoSController.BAN_LIST.put(this.port, this.banList);
			}
		}
	}
	
	private DDoSClient getClient(String address) {
		synchronized (this.clients) {
			DDoSClient client = this.clients.get(address);
			if ((client == null)) {
				client = new DDoSClient(address, this.connections);
				this.clients.put(client.getAddress(), client);
			}
			return client;
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<String> keys = this.banList.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject ban = this.banList.getJSONObject(key);
			Date date = new Date(ban.getLong("timestamp"));
			builder.append("\t").append("Address ").append(key).append(" black listed until ").append(date.toGMTString()).append("\n");
		}
		return builder.append("\n").toString();
	}
	
	// Logging
	
	public void log(String message) {
		System.out.println("\t[" + this.port + "] " + message);
	}

}
