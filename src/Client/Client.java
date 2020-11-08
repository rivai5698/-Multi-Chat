/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author truon
 */
public class Client{

    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private ArrayList<ClientState> clientStates = new ArrayList<>();
    private ArrayList<MsgListener> msgListeners = new ArrayList<>();
    
    public Client(String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 69);
        client.addClientState(new ClientState() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: "+login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: "+login);
            }
        });
        
        client.addMsgListener(new MsgListener() {
            @Override
            public void onMsg(String fromLogin, String msgBody) {
                System.out.println("You got a msg from " + fromLogin+" =>" + msgBody);
            }
            });
        
        if(!client.connect()){
            System.err.println("Connect failed.");
        }else{
            System.out.println("Connect Successful.");
            if(client.login("guest","guest")){
                System.out.println("Login succesful");
                client.msg("admin","Hello");
            }else{
                System.out.println("Login failed");
            }
          //  client.logoff();
        }
        
    }
    public boolean connect(){
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port: "+socket.getLocalPort());
            this.serverIn = socket.getInputStream();
            this.serverOut = socket.getOutputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean login(String login, String password) throws IOException{
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
        String res = bufferedIn.readLine();
        System.out.println("Res Line: " +res);
        if("login successfully".equalsIgnoreCase(res)){
            startMsgReader();
            return true;
        }else{
            return false;
        }
    }  
    public  void addClientState(ClientState listener){
        clientStates.add(listener);
    }
    public  void removeClientState(ClientState listener){
        clientStates.remove(listener);
    }
    private void startMsgReader(){
        Thread t = new Thread(){
            @Override
            public void run() {
                
                    readMsgLoop();
               
            }
            
        };
        t.start();
    }
    private void readMsgLoop(){
        try {
             String line;
        while ((line = bufferedIn.readLine())!=null) {            
            String[] tokens = StringUtils.split(line);
            if(tokens!=null&&tokens.length>0){
                String cmd = tokens[0];
                if("online".equalsIgnoreCase(cmd)){
                    handleOnline(tokens);
            }else if("offline".equalsIgnoreCase(cmd)){
                    handleOffline(tokens);
                } else if("msg".equalsIgnoreCase(cmd) ){
                    String[] tokenMsg = StringUtils.split(line, null, 3);
                    handleMsg(tokenMsg);
                }
        }
        }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex1) {
              ex1.printStackTrace();
            }
        
       
    }
}

    private void handleOnline(String[] tokens) {
      String login = tokens[1];
      for(ClientState listener: clientStates){
          listener.online(login);
      }
    }

    private void handleOffline(String[] tokens) {
       String login = tokens[1];
      for(ClientState listener: clientStates){
          listener.offline(login);
      }
    }
    public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg "+sendTo+" "+msgBody+"\n";
        serverOut.write(cmd.getBytes());
    }
    public  void addMsgListener(MsgListener listener){
        msgListeners.add(listener);
    }
     public  void removeMsgListener(MsgListener listener){
        msgListeners.remove(listener);
    }

    private void handleMsg(String[] tokenMsg) {
        String login = tokenMsg[1];
        String msbBody = tokenMsg[2];
        for(MsgListener listener:msgListeners){
            listener.onMsg(login, msbBody);
        }
    }
    
}