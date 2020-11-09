/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author truon
 */
public class ServerWorker extends Thread {

    private final Server server;
    private final Socket clientSocket;
    private String login = null;
    private OutputStream outputStream;
    private final HashSet<String> topicSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException, SQLException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];

                if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown" + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
                //String msg = "You type: " +line +"\n";
                // outputStream.write(msg.getBytes());
            }
        }
        clientSocket.close();

    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException, SQLException {
        //  if(tokens.length == 3){

        String login = tokens[1];
        String password = tokens[2];
        String logins = "", passwords = "";
        //  if(login.equals("guest")&&password.equals("guest")||login.equals("admin")&&password.equals("admin")){
        //               if (login.equals("") || password.equals("")) {

        Connection conn = JDBCConnection.getJDBCConnection();

        String sql = "SELECT * FROM demomc WHERE LOGIN =  ? AND PASSWORD = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, login);
        pst.setString(2, password);
        System.out.println(pst);
        ResultSet resultSet = pst.executeQuery();

        System.out.println(login + " --- " + password);
        if (resultSet.next()) {

            String msg = "login successfully\n";
            outputStream.write(msg.getBytes());
            this.login = login;
            System.out.println("User login successfully: " + login);

            List<ServerWorker> workerList = server.getWorkerList();
            //send current user all other online login
            for (ServerWorker worker : workerList) {
                if (!login.equals(worker.getLogin())) {
                    if (worker.getLogin() != null) {
                        String msg2 = "online " + worker.getLogin() + "\n";
                        send(msg2);
                    }
                }
            }
            //send other online user current user's stt
            String onLineMsg = "online " + login + "\n";
            for (ServerWorker worker : workerList) {
                if (!login.equals(worker.getLogin())) {
                    worker.send(onLineMsg);
                }
            }

        } else {
            String msg = "login failed\n";
            outputStream.write(msg.getBytes());
            System.out.println("Login failed for: " + login);
        }
    }
    //  }

    public String getLogin() {
        return login;
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();
        //send other online user current user's stt
        String onLineMsg = "offline " + login + "\n";
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onLineMsg);
            }
        }
        clientSocket.close();
    }
    // msg + login + body
    // msg + topic + body

    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];
        boolean isTopic = sendTo.charAt(0) == '#';
        List<ServerWorker> workerList = server.getWorkerList();

        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

}
