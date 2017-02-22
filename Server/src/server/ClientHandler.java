package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Handles each client currently connected to the server. These threads
 * are synchronized with other client and producer threads that are accessing
 * the blocking queue for a resource.
 * @author Richard DeSilvey
 */
public class ClientHandler implements Runnable {

    private Socket client;
    private BlockingQueue<String> buffer;
    
    /**
     * Create a new client handler that will access the resource queue.
     * @param client The client socket
     * @param buffer The reference to the resource buffer
     */
    public ClientHandler(Socket client, BlockingQueue<String> buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    /**
     * Handler thread
     */
    @Override public void run() {
        try {
            readResponse();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtains a response from the client; the only message is "GET" which
     * returns a test resource "Res-A: Thread-B" to the client.
     * @throws IOException
     * @throws InterruptedException 
     */
    private void readResponse() throws IOException, InterruptedException {
        String userInput;
        InputStreamReader reader = new InputStreamReader(client.getInputStream());
        BufferedReader stdIn = new BufferedReader(reader);
        while ((userInput = stdIn.readLine()) != null) {
            if (userInput.equals("GET")) {
                send();
                break;
            }
            System.out.println(userInput);
        }
    }

    /**
     * Sends the next avaliable resource to the client.
     * @throws IOException
     * @throws InterruptedException 
     */
    private void send() throws IOException, InterruptedException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
            writer.write(buffer.poll());
            writer.flush();
        }
    }

}
