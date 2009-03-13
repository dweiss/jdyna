package com.dawidweiss.dyna.corba;

import java.util.List;

import javax.swing.SwingUtilities;

import org.jdyna.GameEvent;
import org.jdyna.IGameEventListener;
import org.jdyna.view.swing.BoardFrame;


/**
 * A listener that shows Swing GUI for the game and hides it at game over.
 */
final class LocalGameView implements IGameEventListener
{
    private BoardFrame view;

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent ge : events)
        {
            switch (ge.type)
            {
                case GAME_START:
                    view = new BoardFrame();
                    view.setVisible(true);
                    break;

                case GAME_OVER:
                    final BoardFrame v = view;
                    view = null;

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            v.dispose();
                        }
                    });

                    return;
            }
        }

        view.onFrame(frame, events);
    }
}
