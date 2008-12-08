package com.dawidweiss.dyna.corba;

import com.dawidweiss.dyna.corba.bindings.CPlayer;
import com.dawidweiss.dyna.corba.bindings.ICPlayerController;

/** Registered player data. */
final class PlayerData
{
    public boolean idle = true;
    public final CPlayer info;
    public final ICPlayerController controller;

    PlayerData(CPlayer info, ICPlayerController controller)
    {
        this.info = info;
        this.controller = controller;
    }
}