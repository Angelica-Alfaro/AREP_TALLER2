package edu.escuelaing.arem.http;

import java.net.*;
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
		if (clientSocket != null) {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String inputLine, outputLine;
			StringBuilder stringBuilderRequest = new StringBuilder();
			if (in != null && in.ready()) {
				while ((inputLine = in.readLine()) != null) {
					// System.out.println("Received: " + inputLine);
					stringBuilderRequest.append(inputLine);
					if (!in.ready()) {
						break;
					}
				}
			}

			String stringRequest = stringBuilderRequest.toString();
			if ((stringRequest != null) && (stringRequest.length() != 0)) {
				String uriStr;
				String[] request = stringRequest.split(" ");

				if ((request != null)) {
					uriStr = request[1];
					//System.out.println("uriStr:" + uriStr);
					URI resourceURI = new URI(uriStr);
					outputLine = getResource(resourceURI);
					out.println(outputLine);
				}
			}
			out.close();
			in.close();
			clientSocket.close();
		} 
		else {
			throw new IOException("Client socket cannot be null");
		}
	}

	public String getResource(URI resourceURI) throws IOException {
		//System.out.println("Received URI: " + resourceURI);
		// return computeDefaultResponse();
		return getRequestDisc();
	}

	// Hacer que el servidor ya reciba js, css, imagenes y no solo html.
	public String getRequestDisc() throws IOException {
		// File archivo = new File("src/main/resources/public_html/index.html");
		File archivo = new File("target/classes/public/index.html");// Meterlo en el root del proyecto, relativo al
																	// folder del proyecto
		BufferedReader in = new BufferedReader(new FileReader(archivo));
		String str;
		String output = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n\r\n";
		while ((str = in.readLine()) != null) {
			System.out.println(str);
			output += str + "\n";
		}
		System.out.println(output);
		return output;
	}

	public String computeDefaultResponse() {
		String outputLine = "HTTP/1.1 200 OK\r\n" 
							+ "Content-Type: text/html\r\n"
							+ "\r\n" 
							+ "<!DOCTYPE html>\n"
							+ "  <html>\n"
							+ "    <head>\n" 
							+ "      <meta charset=\"UTF-8\">\n"
							+ "      <title>Title of the document</title>\n" 
							+ "    </head>\n" 
							+ "    <body>\n"
							+ "      My Web Site\n"
							+ "      <img src=\"https://razonpublica.com/wp-content/uploads/2014/10/mafalda-mujer-quino-e1597859179573.jpg\"> "
							+ "    </body>\n" 
							+ "  </html>\n";
		return outputLine;
	}
}
