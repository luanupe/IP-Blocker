package world.laf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DDoSClient {

	private List<Integer> ports;
	private String address;
	private int limit;
	
	public DDoSClient(String address, int limit) {
		this.ports = new ArrayList<Integer>();
		this.address = address;
		this.limit = limit;
	}
	
	protected void run(int port) {
		this.ports.add(port);
	}
	
	public String getBanFirewall(int port) {
		return "Address " + this.getAddress() + " on Port " + port;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public int getTotal() {
		return this.ports.size();
	}
	
	public boolean isOK() {
		return (this.ports.size() <= this.limit);
	}
	
	public String toString() {
		String status = (this.isOK()) ? "OK" : "KO";
		return "[" + status + "]\t" + this.getAddress() + " = " + Arrays.toString(this.ports.toArray());
	}

}
