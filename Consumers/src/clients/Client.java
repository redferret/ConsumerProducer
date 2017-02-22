package clients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The client is a single thread that simulates a separate client connecting
 * to the server. Once a client gets their resource they will wait 
 * for a random amount of time before asking for another resource. 
 * @author Richard DeSilvey
 */
public class Client implements Runnable {

    private String hostname, threadName;
    private Socket clientSocket;
    private static Random rand;
    private final static AtomicInteger totalResources;
    
    private int port, maxSleepTime, maxConnectTime, numResources;
    
    static {
        rand = new Random();
        totalResources = new AtomicInteger(0);
    }

    /**
     * Client constructor, creates a new client that will access the server
     *
     * @param hostname The Host
     * @param port The port number
     * @param maxSleepTime The maximum sleep time after consuming a resource
     * @param maxConnectTime The maximum amount of time to interact with the
     * server
     */
    public Client(String hostname, int port, int maxSleepTime, int maxConnectTime){
        this.hostname = hostname;
        this.port = port;
        this.maxSleepTime = maxSleepTime;
        this.maxConnectTime = maxConnectTime;
        numResources = 0;
    }

    /**
     * When a client is created the first step is to connect to the server.
     * 
     * @throws UnknownHostException
     * @throws IOException 
     */
    public void connect() throws UnknownHostException, IOException{
        clientSocket = new Socket(hostname, port);
    }

    /**
     * When the client has requested a resource to be delivered via
     * a buffered reader, the server will handle the request and return
     * the resource here.
     * @throws IOException 
     */
    public void getResource() throws IOException{
        String resource;
        InputStreamReader reader = new InputStreamReader(clientSocket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader);

        while ((resource = bufferedReader.readLine()) != null) {
            safePrintln("'"+resource + "' taken by " 
                    + threadName + " at " 
                    + System.currentTimeMillis());
            numResources++;
        }
    }
    
    /**
     * The client will send a simple message "GET" to the server to request
     * a resource.
     * @throws IOException 
     */
    public void askForResource() throws IOException{
        OutputStreamWriter outReader = new OutputStreamWriter(clientSocket.getOutputStream());
    	BufferedWriter writer = new BufferedWriter(outReader);
        writer.write("GET");
        writer.newLine();
        writer.flush();
    }
    
    /**
     * Since the JVM can have many implementations it is safe to assume
     * that the System.out is not synchronized. This method will synchronize
     * each client when they want to print out information.
     * @param s The string being printed
     */
    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
    
    /**
     * The client thread spins here for a limited amount of time.
     */
    @Override public void run() {
        
        threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        int total;
        
        while(true){
            try {
                safePrintln(threadName + " is connecting for "
                        + "new resource at " + System.currentTimeMillis());
                
                connect();
                askForResource();
                getResource();
                
                if ((System.currentTimeMillis() - startTime) >= maxConnectTime){
                    total = totalResources.getAndAdd(numResources);
                    safePrintln(threadName + " is done and consumed " + numResources
                        + " / " + total + " resources.");
                    break;
                }
                safePrintln(threadName + " is sleeping");
                Thread.sleep(Math.max(100, rand.nextInt(maxSleepTime)));
            } catch (UnknownHostException e) {
                System.err.println("Unknown Host");
                break;
            } catch (IOException e) {
                System.err.println("Cannot establish connection: "+e.getMessage());
                break;
            } catch (InterruptedException ex) {
                
            }
        }
    }
    
    /**
     * Creates clients using the arguments passed into this method. The number
     * of clients can be specified along with the maximum time a client
     * will wait after consuming a resource from the server. Also the
     * maximum amount of time a client will remain asking for resources can
     * be specified.
     *
     * @param arg The first argument is the host name, the second argument is
     * the port number, the third argument is the number of consumers, the
     * fourth argument is the maximum waiting time for a client after consuming
     * a resource, and the fifth argument is the maximum amount of time
     * a single client will continue asking for resources (the minimum time
     * a client will stay running is 5 seconds).
     */
    public static void main(String arg[]){
        
        String hostName = arg[0];
        int port = Integer.parseInt(arg[1]);
        int numConsumers = Integer.parseInt(arg[2]);
        int maxSleepTime = Integer.parseInt(arg[3]);
        int maxConnectTime;
        // Create different clients
        for (int i = 0; i < numConsumers; i++){
            
            maxConnectTime = Math.max(5000, rand.nextInt(Integer.parseInt(arg[4])));
            
            Client client = new Client(hostName, port, maxSleepTime, maxConnectTime);

            Thread threadClient = new Thread(client);
            threadClient.start();
        }
        
    }
}