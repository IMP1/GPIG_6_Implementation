package network;

import java.time.LocalDateTime;

public class ScanAcknowledgement extends Acknowledgement {

	public ScanAcknowledgement(String id, LocalDateTime timestamp) {
		super(id, timestamp);
	}
	
	public ScanAcknowledgement(String message) {
		super(message);
	}
	
}
