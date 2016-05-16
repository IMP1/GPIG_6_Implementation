package network;

import java.time.LocalDateTime;

public class MoveAcknowledgement extends Acknowledgement {

	public MoveAcknowledgement(String id, LocalDateTime timestamp) {
		super(id, timestamp);
	}
	
	public MoveAcknowledgement(String message) {
		super(message);
	}
	
}
