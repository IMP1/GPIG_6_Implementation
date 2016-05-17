package network;

import java.time.LocalDateTime;

public final class ScanAcknowledgement extends Acknowledgement {

	public ScanAcknowledgement(String id, LocalDateTime timestamp) {
		super(id, timestamp);
	}
	
	public ScanAcknowledgement(String message) {
		super(message);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(SUFFIX);
		return sb.toString();
	}
	
}
