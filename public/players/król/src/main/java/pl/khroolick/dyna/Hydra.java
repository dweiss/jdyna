package pl.khroolick.dyna;

import java.awt.Point;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dawidweiss.dyna.BoardInfo;
import com.dawidweiss.dyna.GameEvent;
import com.dawidweiss.dyna.GameStartEvent;
import com.dawidweiss.dyna.GameStateEvent;
import com.dawidweiss.dyna.IGameEventListener;
import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.Player;
import com.dawidweiss.dyna.SoundEffectEvent;

public class Hydra implements IPlayerController, IGameEventListener
{
    final Logger log = LoggerFactory.getLogger(Hydra.class);

    /** This player's name. */
    public final String myName;

    private int counter;

    MetaDataExtractor metaExtractor;
   
    private Direction currentDirection;

    public Hydra(String name)
    {
        this.myName = name;
    }

    /**
     * Cached board info.
     */
    private BoardInfo boardInfo;

    @Override
    public boolean dropsBomb()
    {
        if (metaExtractor != null)
        {
            Point myPoss = metaExtractor.hydraPlayer.boardPoss;
            for (PlayerInfo p : metaExtractor.opponents.values())
            {
                if (p.boardPoss.distanceSq(myPoss) < 4)
                {
                    return true;
                }
            }
        }
        return false;
    }

   
    @Override
    public Direction getCurrent()
    {
        return currentDirection;
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        if (++counter < frame)
        {
            log.warn("We have lost {} frames!", frame - counter);
            counter = frame;
        }

        for (GameEvent event : events)
        {
            switch (event.type)
            {
                case GAME_START:
                    counter = frame;
                    
                    this.boardInfo = ((GameStartEvent) event).getBoardInfo();
                    metaExtractor = new MetaDataExtractor(this, boardInfo);

                    break;

                case GAME_STATE:
                    final GameStateEvent gse = (GameStateEvent) event;

                    metaExtractor.updateMetaData(frame, gse);

                    if (metaExtractor.hydraPlayer.isDead)
                    {
                        log.debug("I'm dead!");
                    }
                    else
                    {
                        final Point pixelPosition = metaExtractor.hydraPlayer.pixelPoss;
                        final Point gridPosition = boardInfo.pixelToGrid(pixelPosition);
                       
                        log.trace("I'm at {} ({} exactly)", gridPosition, pixelPosition);
                      
                    }
                    break;

                case SOUND_EFFECT:
                    final SoundEffectEvent see = (SoundEffectEvent) event;
                    log.trace("I've just heard: {}", see.effect);
                    break;

                case GAME_OVER:
                    log.info("this game is over :(");
                    break;
            }
        }
    }

    /**
     * Create a named rabbit player.
     */
    public static Player createPlayer(String name)
    {
        return new Player(name, new Hydra(name));
    }

    /**
     * choose direction
     */
    protected void setCurentDirection(Direction dir)
    {
        currentDirection = dir;
    }
}
