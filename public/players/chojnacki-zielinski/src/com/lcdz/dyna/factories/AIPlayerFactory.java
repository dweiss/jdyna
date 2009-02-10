package com.lcdz.dyna.factories;

import com.dawidweiss.dyna.*;
import com.lcdz.dyna.ai.AIPlayerController;

public final class AIPlayerFactory implements IPlayerFactory
{
    private IPlayerController controller;
    
    public AIPlayerFactory(IPlayerController controller) {
        this.controller = controller;
    }

    @Override
    public IPlayerController getController(String playerName) {
    	if (controller == null) {
    		controller = new AIPlayerController(playerName);
    	}
    	return controller;
    }

    @Override
    public String getDefaultPlayerName() {
        return "twoj_stary";
    }

    @Override
    public String getVendorName() {
        return "chojnacki-zielinski";
    }
}
