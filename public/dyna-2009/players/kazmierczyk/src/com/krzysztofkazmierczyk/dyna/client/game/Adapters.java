package com.krzysztofkazmierczyk.dyna.client.game;

import org.jdyna.GameStateEvent;
import org.jdyna.IPlayerController.Direction;

import com.dawidweiss.dyna.corba.bindings.CBoardInfo;
import com.dawidweiss.dyna.corba.bindings.CControllerState;
import com.dawidweiss.dyna.corba.bindings.CDirection;
import com.dawidweiss.dyna.corba.bindings.CGameEvent;
import com.dawidweiss.dyna.corba.bindings.CGameEventType;
import com.dawidweiss.dyna.corba.bindings.CPlayer;

/** Various adapters which I cannot found in dyna project. */
public class Adapters {

	public static GameStateEvent getGameStateEvent(CGameEvent[] events,
			CBoardInfo info, CPlayer[] names) {
		for (int i = 0; i < events.length; i++) {
			if (events[i].discriminator().value() == CGameEventType._GAME_STATE) {
				return (GameStateEvent) (com.dawidweiss.dyna.corba.Adapters
						.adapt(events[i].gameState(), info, names));
			}
		}
		throw new RuntimeException(
				"CGameEvent with type _GAME_STATE not found in events.");
	}
	
	public static Direction getDirection(CControllerState ccontrolerState)
    {
        if (ccontrolerState != null)
        {
            CDirection cdirection = ccontrolerState.direction;

            if (cdirection == CDirection.DOWN)
            {
                return Direction.DOWN;
            }

            if (cdirection == CDirection.UP)
            {
                return Direction.UP;
            }

            if (cdirection == CDirection.LEFT)
            {
                return Direction.LEFT;
            }

            if (cdirection == CDirection.RIGHT)
            {
                return Direction.RIGHT;
            }

        }
        return null;
    }
}
