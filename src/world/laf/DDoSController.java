package world.laf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import net.sf.json.JSONObject;

// netsh advfirewall firewall add rule name="LAF - IP Block 192.198.10.100" dir=in interface=any action=block remoteip=192.198.10.100
// netsh advfirewall firewall delete rule name="LAF - IP Block 192.198.10.100"
public class DDoSController {
	
	private Map<String, DDoSClient> clients;
	private int port, connections, timeout;
	private JSONObject banList;
	
	protected DDoSController(int port, int connections, int timeout) throws Exception {
		this.clients = new HashMap<String, DDoSClient>();
		this.port = port;
		this.connections = connections;
		this.timeout = timeout;
		this.init();
	}
	
	private void init() throws Exception {
		File file = new File("res\\block\\" + this.port + ".json");
		if ((file.exists())) {
			FileInputStream input = new FileInputStream(file);
			Scanner reader = new Scanner(input, "UTF-8");
			String content = reader.useDelimiter("\\A").next();
			reader.close();
			this.banList = JSONObject.fromObject(content);
		} else {
			this.banList = new JSONObject();
		}
	}
	
	protected void run(NetStatProtocol stat) {
		DDoSClient client = this.getClient(stat.getRemoteAddress());
		client.run(stat.getRemotePort());
	}
	
	protected void finish() {
		this.unban();
		this.ban();
		this.saveBanList();
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
				System.out.println("\t[" + this.port + "] " + client.getTotal() + " connections... Adding IP to block list: " + client.getAddress());
				
				// Roda o processo pra ADICIONAR a regra no firewall
				Process p = Runtime.getRuntime().exec("cmd /c netsh advfirewall firewall add rule name=\"" + client.getBanFirewall() + "\" dir=in interface=any action=block remoteip=" + client.getAddress()); 
	            p.waitFor();
	            
				ban = new JSONObject();
				ban.put("name", client.getBanFirewall());
				ban.put("connections", client.getTotal());
				ban.put("address", client.getAddress());
				
				// Atualiza o tempo do banimento (se já estiver banido simplesmente recomeça a contagem)
				ban.put("timestamp", System.currentTimeMillis());
				this.banList.put(ban.getString("address"), ban);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void unban() {
		Iterator<String> keys = this.banList.keys();
		while(keys.hasNext()) {
			JSONObject ban = this.banList.getJSONObject(keys.next());
			long banExpires = ban.getLong("timestamp") + this.timeout;
			
			try {
				if ((System.currentTimeMillis() >= banExpires)) {
					System.out.println("\t[" + this.port + "] Removing IP from block list: " + ban.getString("address"));
					
					// Roda o processo pra REMOVER a regra do firewall
					Process p = Runtime.getRuntime().exec("cmd /c netsh advfirewall firewall delete rule name=\"" + ban.getString("name") + "\""); 
		            p.waitFor();
					
					// Remove da lista de banimentos
					this.banList.remove(ban.getString("address"));
				}
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
		builder.append("--- [ Port Stats " + this.port + " ] ----------------------------------").append("\n");
		Iterator<String> keys = this.banList.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			builder.append("IP Address banned: ").append(key).append("\n");
		}
		/*for (Map.Entry<String, DDoSClient> client : this.clients.entrySet()) {
			builder.append(client.getValue().toString()).append("\n");
		}*/
		return builder.append("\n").toString();
	}

}
