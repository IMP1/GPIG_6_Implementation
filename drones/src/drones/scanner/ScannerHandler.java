package drones.scanner;

/**
 * Asynchronous scanning handler.
 * Placed in a separate thread as raw data from sensors will likely
 * need processing, though it is given as pre-processed information in
 * this prototype.
 * @author Anthony Williams
 */
public class ScannerHandler implements Runnable {
	
	@Override
	public void run() {
		//TODO: Asynchronous scanner handling and giving data to transmit.
	}
}
