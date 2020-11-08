/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 *
 * @author truon
 */
public class MsgPane extends JPanel implements MsgListener{

    private final Client client;
    private final String login;
    
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> msgList = new JList<>(listModel);
    private JTextField inputField = new JTextField();
    
    public MsgPane(Client client, String login){
        this.client = client;
        this.login = login;
        
        client.addMsgListener(this);
        
       
        setLayout(new BorderLayout());
        add(new JScrollPane(msgList),BorderLayout.CENTER);
        add(inputField,BorderLayout.SOUTH);
        
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    client.msg(login, text);
                    listModel.addElement("You: "+text);
                    inputField.setText("");
                    
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
}

    @Override
    public void onMsg(String fromLogin, String msgBody) {
        if(login.equalsIgnoreCase(fromLogin)){
      String line = fromLogin + ": "+msgBody;
      listModel.addElement(line);
    }
    }
}