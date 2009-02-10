package com.arturklopotek.dyna.ai;

import java.awt.Point;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameOverEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerSprite;

/**An abstract class that is the base for specific player controller implementations. It
 *
 */
public abstract class AbstractPlayerController
    implements IPlayerController,IGameEventListener,Runnable
{

    /** the controlled player's name */
    protected final String playerName;
    
    /** This should be set by {@link #execute()} to alter the controller state */
    protected Direction direction;
    /** This should be set by {@link #execute()} to alter the controller state */
    protected boolean dropsBomb;
    
    /** The current game state (updated each time the {@link #waitForFrame()} method is called) */
    protected GameStateEvent state;
    /** The current frame (updated each time the {@link #waitForFrame()} method is called) */
    protected int frame;
    
    private BoardInfo boardInfo;
    private Thread thread;
    
    /**Constructs the controller for the player with the given name. Note that providing 
     * the correct name (a name of an existing player) is crucial for the controller 
     * to operate correctly.
     * @param playerName
     */
    public AbstractPlayerController(String playerName)
    {
        this.playerName = playerName;
    }

    @Override
    public boolean dropsBomb()
    {
        return dropsBomb;
    }

    @Override
    public Direction getCurrent()
    {
        return direction;
    }
    
    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        for (GameEvent event : events)
        {
            if (event instanceof GameStartEvent)
            {
                //reset the controller state and start the controller thread
                reset((GameStartEvent) event);
            }
            else if (event instanceof GameStateEvent)
            {
                //wait until waitForFrame() is called 
                synchronized (this) {
                    state = (GameStateEvent) event;
                    this.frame = frame; 
                    notify();
                }
            } else if (event instanceof GameOverEvent) {
                //game over - interrupt the controller thread
                thread.interrupt();
            }
        }
    }
    
    private void reset(GameStartEvent ev)
    {
        dropsBomb = false;
        direction = null;
        boardInfo = ev.getBoardInfo();
        thread = new Thread(this);
        thread.start();
    }

    protected Point toPixel(Point point) {
        return boardInfo.gridToPixel(point);
    }

    protected Point toGrid(Point point) {
        return boardInfo.pixelToGrid(point);
    }
    
    protected IPlayerSprite me()
    {

        for (IPlayerSprite p : state.getPlayers())
        {
            if (p.getName().equals(playerName))
            {
                return p;
            }
        }
        return null;
    }
    
    /**Waits for the next frame. The {@link #state} and {@link #frame} fields are updated 
     * after the method returns.
     * @throws InterruptedException when the {@link GameOverEvent} has been received
     */
    synchronized protected void waitForFrame() throws InterruptedException {
        
        if (thread.isInterrupted())
            throw new InterruptedException();
        wait();
    }
    
    @Override
    public void run()
    {
        try {
            synchronized (this) {
                execute();
            }
        } catch (RuntimeException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "AI exception",e);
        } catch (InterruptedException e) {
            Logger.getAnonymousLogger().log(Level.INFO, "AI thread finished");
        }
    }
    
    /**This method is executed in the context of a separate controller thread. It is called once
     * the {@link GameStartEvent} has been received. Hence, returning from this method will 'freeze'
     * the controller in the last state and it won't be possible to alter that state anymore. The
     * implementors should call waitForFrame() repeatedly to wait for the next frame and utilize
     * {@link #state} field to get the current game state.
     * @throws InterruptedException when the game is over
     */
    public abstract void execute() throws InterruptedException;
}
