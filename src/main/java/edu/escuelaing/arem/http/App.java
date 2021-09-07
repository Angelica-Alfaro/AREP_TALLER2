package edu.escuelaing.arem.http;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Communication for Heroku using java network management libraries
 * @author Angélica
 *
 */
public class App {
	
	/**
	 * Start the web server
	 * @param args - server connection
	 */
	public static void main(String[] args) {
		try {
			HttpServer.getInstance().start(args, getPort());
		} 
		catch (IOException e) {
			e.printStackTrace();	
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method reads the default port as specified by the PORT variable in the
	 * environment.
	 *
	 * Heroku provides the port automatically so you need this to run the project on
	 * Heroku.
	 */
	static int getPort() {
		if (System.getenv("PORT") != null) {
			return Integer.parseInt(System.getenv("PORT"));
		}
		return 35000; // returns default port if heroku-port isn't set (i.e. on localhost)
	}
}
