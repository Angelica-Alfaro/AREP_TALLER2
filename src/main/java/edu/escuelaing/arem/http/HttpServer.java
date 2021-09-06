package edu.escuelaing.arem.http;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;

public class HttpServer {
	private static final HttpServer _instance = new HttpServer();

	private HttpServer() {
	}

	public static HttpServer getInstance() {
		return _instance;
	}

	public void start(String[] args, int port) throws IOException, URISyntaxException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port);
			System.exit(1);
		}

		boolean running = true;
		while (running) {
			Socket clientSocket = null;

			try {
				System.out.println("Listo para recibir ...");
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}
			serverConnection(clientSocket);
		}
		serverSocket.close();
	}

	public void serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		String inputLine, outputLine;
		StringBuilder stringBuilderRequest = new StringBuilder();
		while ((inputLine = in.readLine()) != null) {
			// System.out.println("Received: " + inputLine);
			stringBuilderRequest.append(inputLine);
			if (!in.ready()) {
				break;
			}
		}

		String stringRequest = stringBuilderRequest.toString();
		if ((stringRequest != null) && (stringRequest.length() != 0)) {
			String[] request = stringRequest.split(" ");
			String uriStr = request[1];
			//System.out.println("uriStr:" + uriStr);
			URI resourceURI = new URI(uriStr);
			outputLine = getResource(resourceURI);
			out.println(outputLine);
		}
		out.close();
		in.close();
		clientSocket.close();
	}

	public String getResource(URI resourceURI) throws IOException {
		Charset charset = Charset.forName("UTF-8");
		Path file = Paths.get("target/classes/public" + resourceURI.getPath());
		String output;
		
		try (BufferedReader in = Files.newBufferedReader(file, charset)) {
			String str, mimeType = null;

			mimeType = contentType(resourceURI.getPath());
			output = "HTTP/1.1 200 OK\r\n" + "Content-Type: " + mimeType + "\r\n\r\n";
			while ((str = in.readLine()) != null) {
				//System.out.println(str);
				output += str + "\n";
			}

		} catch (IOException e) {
			System.err.println("Access denied.");
			output = defaultResponse();
		}
		//System.out.println(output);
		return output;
	}

	public String defaultResponse() {
		String outputLine = "HTTP/1.1 200 OK\r\n" 
							+ "Content-Type: text/html\r\n"
							+ "\r\n" 
							+ "<!DOCTYPE html>\n"
							+ "  <html>\n"
							+ "    <head>\n" 
							+ "      <meta charset=\"UTF-8\">\n"
							+ "      <title>Home page</title>\n" 
							+ "    </head>\n" 
							+ "    <body>\n"
							+ "      <img src=\"http://agro-sky.com/construccion5.jpg\"> "
							+ "    </body>\n" 
							+ "  </html>\n";
		return outputLine;
	}
	
	public String contentType(String path) {
		String mimeType = null;
		
		if (path.contains(".html")) {
			mimeType = "text/html";
		}
		else if (path.contains(".css")) {
			mimeType = "text/css";
		} 
		else if (path.contains(".js")) {
			mimeType = "text/javascript";
		}
		return mimeType;
	}
}
