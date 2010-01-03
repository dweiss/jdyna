package org.jdyna;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HighlightDetector2 implements IHighlightDetector
{
    private FrameRange frameRange = null;
    private GameConfiguration gameConfiguration = null;
    private int frame = 0;

    private final int highlightReverseSeconds = 2;
    private final int highlightForwardSeconds = 2;

    private final int expireTimeInSeconds = 2;

    private final int eventsToFindHighlight = 4;

    /**
     * This list contains frames numbers with interesting events. 
     * It can contain many elements with the same value (f.e. 3 bombs exploding on the same frame). 
     * Elements of this lists are expired (removed) after {@link #expireTimeInSeconds} seconds.   
     */
    private final List<Integer> eventsFrames = Lists.newArrayList();

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
                        if (player.getLifeCount() != playersLives.get(player.getName())) eventsFrames
                            .add(frame);
                    }
                    playersLives.put(player.getName(), player.getLifeCount());
                }
            }
            else if (e instanceof ExplosionEvent)
            {
                for (int i = 0; i < ((ExplosionEvent) e).getMetadata().size(); i++)
                    eventsFrames.add(frame);
            }
        }

        /*
         * Let's see if we have something interesting here.
         */
        if (eventsFrames.size() >= eventsToFindHighlight)
        {
            int beginFrame = eventsFrames.get(0) - highlightReverseSeconds
                * gameConfiguration.DEFAULT_FRAME_RATE;
            int endFrame = eventsFrames.get(0) + highlightForwardSeconds
                * gameConfiguration.DEFAULT_FRAME_RATE;

            frameRange = new FrameRange(beginFrame, endFrame);
        }
    }

    private void expireEvents()
    {
        while (eventsFrames.size() > 0
            && eventsFrames.get(0) + expireTimeInSeconds
                * gameConfiguration.DEFAULT_FRAME_RATE < frame)
            eventsFrames.remove(0);

    }

    @Override
    public void onFrame(int frame, List<? extends GameEvent> events)
    {
        this.frame = frame;
        expireEvents();
        detectEvents(events);
    }
}
