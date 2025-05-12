package main;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import GameEngine.GameEngine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main {
	public static void main(String[] args) throws Exception {
		String webappCodeBase = "./war";
		File warFile = new File(webappCodeBase);
		Launcher launcher = new Launcher();
		
		// Try to start server on port 8081, with fallback to other ports if needed
		System.out.println("CREATING: web server on port 8081");
		Server server = null;
		int port = 8081;
		int maxAttempts = 10;
		int attempts = 0;

		while (server == null && attempts < maxAttempts) {
			try {
				server = launcher.launch(true, port, warFile.getAbsolutePath(), "/");
				System.out.println("STARTING: web server on port " + port);
			} catch (java.net.BindException e) {
				attempts++;
				port++;
				System.out.println("Port " + (port-1) + " already in use, trying port " + port);
			}
		}

		if (server == null) {
			throw new Exception("Failed to find an available port after " + maxAttempts + " attempts");
		}
		
		// Start the server
		server.start();
		
		// dump the console output - this will produce a lot of red text - no worries, that is normal
		server.dumpStdErr();
		
		// Inform user that server is running
		System.out.println("RUNNING: web server on port " + port);
		
        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
		server.join();
	}
}
