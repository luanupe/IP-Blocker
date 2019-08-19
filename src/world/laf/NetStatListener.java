package world.laf;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class NetStatListener {
	
	public ArrayList<NetStatProtocol> netStat() throws Exception {
		ArrayList<NetStatProtocol> netstat = new ArrayList<>();
		ProcessBuilder builder = new ProcessBuilder( "netstat", "-n" );
		Process p = builder.start();
		try (Scanner scanner = new Scanner(p.getInputStream())) {
			Pattern pattern = Pattern.compile( "(TCP|UDP)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)" );
			while (scanner.findWithinHorizon(pattern, 0) != null) {
				NetStatProtocol protocol = this.getProtocol(scanner.match().group(1), scanner.match().group(2), scanner.match().group(3), scanner.match().group(4));
				if ((protocol != null)) netstat.add(protocol);
			}
		}
		return netstat;
	}
	
	private NetStatProtocol getProtocol(String protocol, String localAddress, String remoteAddress, String status) {
		String[] local = localAddress.trim().split(":");
		String[] remote = remoteAddress.trim().split(":");
		
		if ((!local[1].isEmpty()) && (!remote[1].isEmpty())) {
			return new NetStatProtocol(protocol, status, local[0], Integer.parseInt(local[1]), remote[0], Integer.parseInt(remote[1]));
		}
		return null;
	}

}
