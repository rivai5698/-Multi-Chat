/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

/**
 *
 * @author truon
 */
public interface MsgListener {
    public void onMsg(String fromLogin,String msgBody);
}
