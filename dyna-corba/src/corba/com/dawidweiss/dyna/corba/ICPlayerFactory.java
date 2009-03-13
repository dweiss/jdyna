package com.dawidweiss.dyna.corba;

import org.jdyna.IPlayerFactory;
import org.omg.PortableServer.POA;

import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/**
 * A factory for constructing {@link CIPlayerController}, for folks that do not have a
 * local controller for their players.
 * 
 * @see IPlayerFactory
 */
public interface ICPlayerFactory
{
    /**
     * Constructs and returns a controller for the given player name on the designated POA.
     */
    ICPlayerController getController(String playerName, POA poa);

    /**
     * Return the default player name for this factory. See class documentation.
     */
    String getDefaultPlayerName();

    /**
     * Return the implementation author(s), see class documentation.
     */
    String getVendorName();
}
