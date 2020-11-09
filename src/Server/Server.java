/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author truon
 */
public class Server extends Thread{

    private final int serverPort;
    private final ArrayList<ServerWorker> workerList =  new ArrayList<>();
    public Server(int serverPort){
        this.serverPort = serverPort;
    }
    
     public List<ServerWorker> getWorkerList(){
         
        return workerList;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while (true) {                
                System.out.println("connection info...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted connetion from: "+ clientSocket);
                ServerWorker worker = new ServerWorker(this,clientSocket);
                workerList.add(worker);
                worker.start();
                
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
    
    public void removeWorker(ServerWorker serverWorker){
         workerList.remove(serverWorker);
     }
    
}
