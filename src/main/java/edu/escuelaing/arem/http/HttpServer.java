package edu.escuelaing.arem.http;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class HttpServer {
	private static final HttpServer _instance = new HttpServer();
	
	public static HttpServer getInstance() {
		return _instance;
	}
	
	private HttpServer() {}
	
	public void start(String[] args) throws IOException, URISyntaxException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(35000);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 35000.");
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
			serveConnection(clientSocket);
		}
		serverSocket.close();
	}
	
	public void serveConnection(Socket clientSocket) throws IOException, URISyntaxException {
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		String inputLine, outputLine;
		ArrayList<String> request = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			System.out.println("Received: " + inputLine);
			request.add(inputLine);
			if (!in.ready()) {
				break;
			}
		}
		
		String uriStr = request.get(0).split(" ")[1];
		URI resourceURI = new URI(uriStr);
		outputLine = getResource(resourceURI);
		out.println(outputLine);
		
		out.close();
		in.close();
		clientSocket.close();
	}
	
	public String getResource(URI resourceURI) throws IOException {
		System.out.println("Received URI: " + resourceURI);
		//return computeDefaultResponse();
		return  getRequestDisc();
	}
	//Hacer que el servidor ya reciba js, css, imagenes y no solo html.
	public String getRequestDisc() throws IOException{
		//File archivo = new File("src/main/resources/public_html/index.html");
		File archivo = new File("target/classes/public_html/index.html");//Meterlo en el root del proyecto, relativo al folder del proyecto
		BufferedReader in = new BufferedReader(new FileReader(archivo));
		String str;
		String output = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n\r\n";
		while ((str = in.readLine()) != null) {
			System.out.println(str);
			output+= str+"\n";
		}
		System.out.println(output);
		return output;
	}
	
	public String computeDefaultResponse() {
		String outputLine = 
				"HTTP/1.1 200 OK\r\n" 
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
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		HttpServer.getInstance().start(args);
	}
}
