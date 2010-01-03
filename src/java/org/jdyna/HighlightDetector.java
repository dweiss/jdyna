package org.jdyna;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class HighlightDetector implements IHighlightDetector
{
    private FrameRange frameRange = null;
    private GameConfiguration gameConfiguration = null;
    private int frame = 0;

    private final int highlightReverseSeconds = 4;
    private final int highlightForwardSeconds = 3;

    /**
     * Mapping player's names to their lives.
     */
    private Map<String, Integer> playersLives = Maps.newHashMap();

    @Override
    public FrameRange getHighlightFrameRange()
    {
        FrameRange result = null;
        if (frameRange != null)
        {
            result = new FrameRange(frameRange.beginFrame, frameRange.endFrame);
            frameRange = null;
        }
        return result;
    }

    @Override
    public boolean isHighlightDetected()
    {
        return (frameRange == null ? false : true);
    }

    public void detectEvents(List<? extends GameEvent> events)
    {
        int playersDead = 0;
        int explosionEvents = 0;

        for (GameEvent e : events)
        {
            if (e instanceof GameStartEvent)
            {
                gameConfiguration = ((GameStartEvent) e).getConfiguration();
            }
            else if (e instanceof GameStateEvent)
            {
                for (IPlayerSprite player : ((GameStateEvent) e).getPlayers())
                {
                    if (playersLives.get(player.getName()) != null)
                    {
                        if (player.getLifeCount() != playersLives.get(player.getName())) playersDead++;
                    }
                    playersLives.put(player.getName(), player.getLifeCount());
                }
            }
            else if (e instanceof ExplosionEvent)
            {
                explosionEvents += ((ExplosionEvent) e).getMetadata().size();
            }
        }

        /*
         * Let's see if we have something interesting here.
         */
        if (playersDead > 1 || (explosionEvents >= 3 && playersDead > 0))
        {
            int beginFrame = frame - highlightReverseSeconds
                * gameConfiguration.DEFAULT_FRAME_RATE;
            int endFrame = frame + highlightForwardSeconds
                * gameConfiguration.DEFAULT_FRAME_RATE;

            /*
             * If anything interesting happens before end of recent action, we'd like to
             * see whole action as one.
             */
            if (frameRange != null && frameRange.endFrame > beginFrame) frameRange.endFrame = endFrame;
            else
            {
                frameRange = new FrameRange(beginFrame, endFrame);
            }
        }
    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.frame = frame;
        detectEvents(events);
    }
}
