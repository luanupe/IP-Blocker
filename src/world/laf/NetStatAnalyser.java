package world.laf;

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NetStatAnalyser extends Thread {

	private List<String> whiteList;
	private int portToCheck, delayToCheck;
	private int connBanLimit, connBanTimeout;
	private NetStatListener listener;
	private DDoSController controller;
	
	public NetStatAnalyser(NetStatListener listener, JSONObject config) {
		this.whiteList = new ArrayList<String>();
		this.listener = listener;
		this.portToCheck = config.getInt("portToCheck");
		this.delayToCheck = config.getInt("delayToCheck");
		this.connBanLimit = config.getInt("connBanLimit");
		this.connBanTimeout = config.getInt("connBanTimeout");
		
		JSONArray whileList = config.getJSONArray("whiteList");
		for (int i = 0; i < whileList.size(); ++i) {
			this.whiteList.add(whileList.getString(i).trim());
		}
		
		System.out.println("[" + this.portToCheck + "] Listening connections...");
	}
	
	@Override
	public void run() {
		while (Main.RUNNING) {
			try {
				this.analyze(this.listener.netStat());
				this.controller.finish();
				Thread.sleep(this.delayToCheck);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void analyze(ArrayList<NetStatProtocol> stats) throws Exception {
		this.controller = new DDoSController(this.portToCheck, this.connBanLimit, this.connBanTimeout);
		for (NetStatProtocol stat : stats) {
			if ((stat.getLocalPort() == this.portToCheck)) {
				if ((!this.whiteList.contains(stat.getRemoteAddress()))) this.analyze(stat);
				else System.out.println(stat.getRemoteAddress() + " is white listed!");
			}
		}
	}
	
	private void analyze(NetStatProtocol stat) {
		this.controller.run(stat);
	}
	
	protected int getPortToCheck() {
		return this.portToCheck;
	}
	
	protected DDoSController getController() {
		return this.controller;
	}

}
