package network;

import java.time.LocalDateTime;

public final class MoveAcknowledgement extends Acknowledgement {
	
	public final static String MOVE_ACK_PREFIX = "MOVE";

	public MoveAcknowledgement(String id, LocalDateTime timestamp) {
		super(id, timestamp);
	}
	
	public MoveAcknowledgement(String message) {
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
