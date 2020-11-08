/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author truon
 */
public class ClientListPane extends JPanel implements ClientState{

    private final Client client;
    private JList<String> clientListUI;
    private DefaultListModel<String> clientListModel;
    
    public ClientListPane(Client client){
        this.client = client;
        this.client.addClientState(this);
        
        clientListModel = new DefaultListModel<>();
        clientListUI = new JList<>(clientListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(clientListUI),BorderLayout.CENTER);
        clientListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               if(e.getClickCount()>1){
                   String login = clientListUI.getSelectedValue();
                   MsgPane msgPane = new MsgPane(client,login);
                   
                   JFrame f = new JFrame("Msg: "+login);
                   f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                   f.setSize(500,500);
                   f.getContentPane().add(msgPane,BorderLayout.CENTER);
                   f.setVisible(true);
                   
               }
            }
            
        });
    }
    
    public static void main(String[] args) {
         Client client = new Client("localhost", 69);
         ClientListPane clientListPane = new ClientListPane(client);
         JFrame frame = new JFrame("Client List Online");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(400,400);
         frame.getContentPane().add(clientListPane,BorderLayout.CENTER);
         frame.setVisible(true);
         
         if(client.connect()){
             try {
                 client.login("guest", "guest");
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         };
         
    }

    @Override
    public void online(String login) {
        clientListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        clientListModel.removeElement(login);
    }
}
