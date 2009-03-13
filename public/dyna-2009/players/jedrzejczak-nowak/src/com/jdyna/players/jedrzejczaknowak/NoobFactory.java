package com.jdyna.players.jedrzejczaknowak;

import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;
import org.omg.CORBA.UserException;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.ICPlayerFactory;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;
import com.dawidweiss.dyna.corba.bindings.ICPlayerControllerHelper;

/**
 * n00b-producing factory. Capable of generating both local and corba players.
 */
public class NoobFactory implements IPlayerFactory, ICPlayerFactory {

    @Override
    public IPlayerController getController(String playerName) {
        return new NoobPlayer(playerName);
    }

    @Override
    public String getDefaultPlayerName() {
        return "n00b";
    }

    @Override
    public String getVendorName() {
        return "Piotr Jedrzejczak, Michal Nowak";
    }

    @Override
    public ICPlayerController getController(String playerName, POA poa) {
        try {
            return ICPlayerControllerHelper
                    .narrow(poa
                            .servant_to_reference(new ICPlayerControllerAdapterDecorator(
                                    this.getController(playerName))));
        } catch (UserException e) {
            throw new RuntimeException(e);
        }
    }

}
