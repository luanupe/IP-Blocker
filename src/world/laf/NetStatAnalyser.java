package world.laf;

import java.util.ArrayList;
import java.util.LinkedList;

import net.sf.json.JSONObject;

public class NetStatAnalyser extends Thread {

	private int portToCheck, delayToCheck;
	private int connBanLimit, connBanTimeout;
	private NetStatListener listener;
	private LinkedList<DDoSController> controllers;
	
	public NetStatAnalyser(NetStatListener listener, JSONObject config) {
		this.controllers = new LinkedList<DDoSController>();
		this.listener = listener;
		this.portToCheck = config.getInt("portToCheck");
		this.delayToCheck = config.getInt("delayToCheck");
		this.connBanLimit = config.getInt("connBanLimit");
		this.connBanTimeout = config.getInt("connBanTimeout");
		System.out.println("[" + this.portToCheck + "] Listening...");
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				ArrayList<NetStatProtocol> stats = this.listener.netStat();
				this.analyze(stats);
				this.controllers.getFirst().finish();
				Thread.sleep(this.delayToCheck);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void analyze(ArrayList<NetStatProtocol> stats) throws Exception {
		// Cria o novo controlador e carrega a ban list
		DDoSController controller = new DDoSController(this.portToCheck, this.connBanLimit, this.connBanTimeout);
		
		// Adiciona o novo controlador ao começo da lista
		this.controllers.addFirst(controller);
		
		// Checar cada conexão se for da porta correta
		for (NetStatProtocol stat : stats) {
			if ((stat.getLocalPort() == this.portToCheck)) this.analyze(stat);
		}
	}
	
	private void analyze(NetStatProtocol stat) {
		this.controllers.getFirst().run(stat);
	}

}
