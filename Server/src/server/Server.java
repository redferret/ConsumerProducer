package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The server holds the blocking queue of resources and the server socket.
 * The producer threads are spawned here and service the queue keeping it
 * full.
 * @author Richard DeSilvey
 */
public class Server{
    
    private ServerSocket serverSocket;
    private int port, maxWaitTime, maxProducers;
    private static BlockingQueue<String> resources;
    /**
     * The maximum number of resources in the buffer.
     */
    private static int numOfResources = 1;
    
    /**
     * Creates a new Server that spawns a certain number of Producers with
     * a specified sleep time for each producer.
     * @param port The port number of the server
     * @param maxProducers The maximum number of producer threads
     * @param maxWaitTime The maximum amount of time a producer will wait
     * @throws InterruptedException 
     */
    public Server(int port, int maxProducers, int maxWaitTime) throws InterruptedException {
        
        resources = new BlockingQueue<>(numOfResources);
        
        this.maxProducers = maxProducers;
        this.maxWaitTime = maxWaitTime;
        
        this.port = port;
    }
    
    /**
     * Main server thread loop
     * @throws IOException 
     */
    public void start() throws IOException {
        System.out.println("Starting the socket server at port:" + port);
        serverSocket = new ServerSocket(port);
        
        Socket client = null;
        
        spawnProducers();
        
        while(true){
            client = serverSocket.accept();// Blocked until a client connects
            Thread thread = new Thread(new ClientHandler(client, resources));
            thread.start();
        }     
    }
    
    /**
     * Spawns the producer threads
     */
    private void spawnProducers(){
        for (int i = 0; i < maxProducers; i++){
            Thread thread = new Thread(new Producer(maxWaitTime, resources));
            thread.start();
        }
    }
    
    /**
     * Creates a SocketServer object and starts the server. The server will
     * spawn a designated number of resource producers. Each producer will wait
     * after putting a resource into the buffer, this value can be specified.
     *
     * @param args First argument is the port number, the second is the number
     * of producers, the third argument is the maximum wait time a producer will
     * wait for after adding a resource, and the fourth argument is the number
     * of available resources in the buffer.
     *
     */
    public static void main(String[] args) {

        int portNumber = Integer.parseInt(args[0]);
        int numProducers = Integer.parseInt(args[1]);
        int maxWaitTime = Integer.parseInt(args[2]);
        numOfResources = Integer.parseInt(args[3]);
        
        try {  
            Server server = new Server(portNumber, numProducers, maxWaitTime);
            server.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
        }
    }
}