package edu.escuelaing.arem.http;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Class that simulates a web server or HTTP server
 * @author Angélica
 *
 */
public class HttpServer {
	
	/**
	 * Attribute that defines the web server 
	 */ 
	private static final HttpServer _instance = new HttpServer();
	
	private HttpServer() {
	}
	
	/**
     * This method get the necessary feature to web server
     * @return web server instance 
     */
	public static HttpServer getInstance() {
		return _instance;
	}
	
	/**
     * This method start the client-server connection for communication
     * @param args - Request to go to another page or file
     * @param port - The connection port
     * @throws IOException, URISyntaxException
     */
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
	
	/**
     * Client connection with Server
     * @param clientSocket - Client for communication
     * @throws IOException, URISyntaxException
     */
	public void serverConnection(Socket clientSocket) throws IOException, URISyntaxException {
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		String inputLine;
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
			getResource(resourceURI, out, clientSocket.getOutputStream(), uriStr);
		}
		out.close();
		in.close();
		clientSocket.close();
	}
	
	/**
     * This method lets you read a text or image type resource
     * @param resourceURI - Path of the required resource
     * @param out - Text output flow
     * @param outputStream - Write output
     * @param uriStr - URI as a string
     * @throws IOException
     */
	private void getResource(URI resourceURI, PrintWriter out, OutputStream outputStream, String uriStr) throws IOException {
		String outputLine;
		String mimeType = contentType(resourceURI.getPath());
		if (mimeType != null) {
			if (mimeType.contains("image")) {
				String[] extension = uriStr.split("\\.");
				getImageResource(resourceURI, outputStream, extension[1]);
			} 
			else {
				outputLine = getTextResource(resourceURI);
				out.println(outputLine);
			}
		}
		else if (uriStr.equals("/")){
			outputLine = defaultResponse();
			out.println(outputLine);
		}
	}

	/**
     * This method allows reading a resource of type .html, .css and .js
     * @param resourceURI - Path of the required resource
     * @param outputStream - Write output
     * @param extension - Define the file extension
     * @throws IOException
     */
	private void getImageResource(URI resourceURI, OutputStream outputStream, String extension) throws IOException {
		String path = "target/classes/public" + resourceURI.getPath();
		File file = new File(path);
		String mimeType = contentType(resourceURI.getPath());

		if (file.exists()) {
			try {
				BufferedImage image = ImageIO.read(file);
				if (image != null) {
					ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
					DataOutputStream writeimg = new DataOutputStream(outputStream);

					ImageIO.write(image, extension, arrayOutputStream);
					writeimg.writeBytes("HTTP/1.1 200 OK \r\n" + "Content-Type: " + mimeType + "\r\n" + "\r\n");
					writeimg.write(arrayOutputStream.toByteArray());
				}
			} catch (IOException e) {
				throw new IOException("Image resource can not be null");
			}
		} else {
			throw new IOException("Image resource don't exist");
		}
	}
	
	/**
     * This method allows reading a resource of type .jpg, .png, .jpeg, .gif
     * @param resourceURI - Path of the required resource
     * @return The content of the resource
     * @throws IOException, URISyntaxException
     */
	public String getTextResource(URI resourceURI) throws IOException {
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

	/**
	 * Default page when not finding a resource
	 * @return the default html page
	 */
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
							+ "      <img src=\"https://www.lagabogados.com/wp-content/uploads/2020/01/EN-CONSTRUCCION.jpg\"> "
							+ "    </body>\n" 
							+ "  </html>\n";
		return outputLine;
	}
	
	/**
	 * This method defines the MIME type of the resource
	 * @param path - Path of the required resource
	 * @return the MIME type of the resource
	 */
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
		else if (path.contains(".png")) {
			mimeType = "image/png";
		}
		else if (path.contains(".jpeg")) {
			mimeType = "image/jpeg";
		}
		else if (path.contains(".gif")) {
			mimeType = "image/gif";
		}
		else if (path.contains(".jpg")) {
			mimeType = "image/jpg";
		}
		
		return mimeType;
	}
}
