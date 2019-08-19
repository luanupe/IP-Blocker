package world.laf;

public class NetStatProtocol {
	
	private String protocol, status;
	private String localAddress, remoteAddress;
	private int localPort, remotePort;
	
	public NetStatProtocol(String protocol, String status, String localAddress, int localPort, String remoteAddress, int remotePort) {
		this.protocol = protocol;
		this.status = status;
		this.localAddress = localAddress;
		this.localPort = localPort;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public String getStatus() {
		return this.status;
	}
	
	public String getLocalAddress() {
		return this.localAddress;
	}
	
	public int getLocalPort() {
		return this.localPort;
	}
	
	public String getRemoteAddress() {
		return this.remoteAddress;
	}
	
	public int getRemotePort() {
		return this.remotePort;
	}
	
	public String getLocalAddressPort() {
		return this.getLocalAddress() + ":" + this.getLocalPort();
	}
	
	public String getRemoteAddressPort() {
		return this.getRemoteAddress() + ":" + this.getRemotePort();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getProtocol()).append("\t");
		builder.append(this.getLocalAddressPort()).append("\t");
		builder.append(this.getRemoteAddressPort()).append("\t");
		builder.append(this.getStatus()).append("\t");
		return builder.toString();
	}

}
