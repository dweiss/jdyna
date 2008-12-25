package com.dawidweiss.dyna.corba;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;

import com.dawidweiss.dyna.*;
import com.dawidweiss.dyna.IPlayerController.Direction;
import com.dawidweiss.dyna.corba.bindings.*;
import com.google.common.collect.Lists;

/**
 * Adapters between Corba and Java game structures.
 */
public final class Adapters
{
    Adapters()
    {
        // no instances.
    }

    /*
     * 
     */
    public static CBoardInfo adapt(BoardInfo boardInfo)
    {
        return new CBoardInfo(adapt(boardInfo.gridSize), boardInfo.cellSize,
            adapt(boardInfo.pixelSize));
    }

    /*
     * 
     */
    public static BoardInfo adapt(CBoardInfo boardInfo)
    {
        return new BoardInfo(adapt(boardInfo.gridSize), boardInfo.cellSize);
    }

    /*
     * 
     */
    public static Dimension adapt(CDimension d)
    {
        return new Dimension(d.width, d.height);
    }

    /*
     * 
     */
    public static CDimension adapt(Dimension d)
    {
        return new CDimension(d.width, d.height);
    }

    /*
     * 
     */
    public static IPlayerController.Direction adapt(CDirection direction)
    {
        if (direction == null) return null;

        switch (direction.value())
        {
            case CDirection._NONE:
                return null;
            case CDirection._DOWN:
                return IPlayerController.Direction.DOWN;
            case CDirection._UP:
                return IPlayerController.Direction.UP;
            case CDirection._LEFT:
                return IPlayerController.Direction.LEFT;
            case CDirection._RIGHT:
                return IPlayerController.Direction.RIGHT;
        }

        throw new RuntimeException(/* unreachable */);
    }
    
    /*
     * 
     */
    public static List<GameEvent> adapt(CGameEvent [] events, CBoardInfo info, CPlayer [] pNames)
    {
        final ArrayList<GameEvent> adapted = Lists.newArrayList();

        for (CGameEvent e : events)
        {
            switch (e.discriminator().value())
            {
                case CGameEventType._GAME_STATE:
                    adapted.add(adapt(e.gameState(), info, pNames));
                    break;
                case CGameEventType._SOUND_EFFECT:
                    adapted.add(adapt(e.soundEffect()));
                    break;
                    
                default:
                    // Unrecognized event.
                    break;
            }
        }

        return adapted;
    }

    /*
     * 
     */
    public static CGameEvent [] adapt(List<GameEvent> events)
    {
        final ArrayList<CGameEvent> result = Lists.newArrayList();

        for (GameEvent ev : events)
        {
            final CGameEvent gameEvent = new CGameEvent();
            switch (ev.type)
            {
                case GAME_STATE:
                    gameEvent.gameState(adapt((GameStateEvent) ev)); 
                    break;
                case SOUND_EFFECT:
                    gameEvent.soundEffect(adapt((SoundEffectEvent) ev)); 
                    break;
                default:
                    throw new RuntimeException();
            }
            result.add(gameEvent);
        }
        
        return result.toArray(new CGameEvent [result.size()]);
    }    
    
    /*
     * 
     */
    private static CSoundEffect adapt(SoundEffectEvent ev)
    {
        final CSoundEffectType effect;
        switch (ev.effect)
        {
            case BOMB:  effect = CSoundEffectType.BOMB; break;
            case BONUS:  effect = CSoundEffectType.BONUS; break;
            case DYING:  effect = CSoundEffectType.DYING; break;
            default:
                throw new RuntimeException();
        };

        return new CSoundEffect(effect, ev.count);
    }

    /*
     * 
     */
    public static GameStateEvent adapt(CGameState gameState, CBoardInfo info, CPlayer [] names)
    {
        final ISprite.Type [] types = ISprite.Type.getPlayerSprites();
        
        final CPlayerState [] cplayers = gameState.players;
        final IPlayerSprite [] players = new IPlayerSprite [cplayers.length];
        for (int i = 0; i < players.length; i++)
        {
            final ISprite.Type t = types[i % types.length];
            final PlayerSpriteImpl np = new PlayerSpriteImpl(t, names[i].name);
            np.position.setLocation(adapt(cplayers[i].position));
            np.animationFrame = cplayers[i].animationFrame;
            np.animationState = cplayers[i].animationState;
            players[i] = np;
        }

        final int h = info.gridSize.height;
        final int w = info.gridSize.width;
        final short [] cdata = gameState.cells;
        final Cell [][] cells = new Cell [w][];
        for (int r = 0; r < w; r++)
        {
            cells[r] = new Cell [h];
        }
        
        for (int r = 0; r < h; r++)
        {
            for (int c = 0; c < w; c++)
            {
                final short v = cdata[c + r * w];
                final int type = v & 0x7f;
                final Cell cell = Cell.getInstance(CellType.valueOf(type));
                cell.counter = v >>> 7;
                cells[c][r] = cell; 
            }
        }

        return new GameStateEvent(cells, Arrays.asList(players));
    }

    /*
     * 
     */
    public static SoundEffectEvent adapt(CSoundEffect e)
    {
        final SoundEffect effect;
        switch (e.effect.value())
        {
            case CSoundEffectType._BOMB: effect = SoundEffect.BOMB; break;
            case CSoundEffectType._BONUS: effect = SoundEffect.BONUS; break;
            case CSoundEffectType._DYING: effect = SoundEffect.DYING; break;
            default:
                throw new RuntimeException();
        };
        return new SoundEffectEvent(effect, e.count);
    }
    
    /*
     * 
     */
    public static CGameState adapt(GameStateEvent in)
    {
        final List<? extends IPlayerSprite> p = in.getPlayers();
        final CPlayerState [] players = new CPlayerState [p.size()];
        for (int i = 0; i < p.size(); i++)
        {
            players[i] = adapt(p.get(i));
        }

        final short [] cells = adapt(in.getCells());

        return new CGameState(cells, players);
    }

    /*
     * 
     */
    public static short [] adapt(Cell [][] cells)
    {
        final int w = cells.length;
        final int h = cells[0].length;
        final short [] ca = new short [w * h];
        for (int c = 0; c < w; c++)
        {
            for (int r = 0; r < h; r++)
            {
                final Cell cell = cells[c][r];
                final short v = (short) ((cell.counter << 7) | (cell.type.ordinal()));
                ca[c + r * w] = v;
            }
        }
        return ca;
    }

    /*
     * 
     */
    public static CPlayerState adapt(IPlayerSprite player)
    {
        return new CPlayerState(player.getAnimationFrame(), player.getAnimationState(),
            adapt(player.getPosition()));
    }

    /*
     * 
     */
    public static CPoint adapt(Point position)
    {
        return new CPoint(position.x, position.y);
    }

    /*
     * 
     */
    public static Point adapt(CPoint position)
    {
        return new Point(position.x, position.y);
    }

    /*
     * 
     */
    public static CControllerState adapt(IPlayerController controller)
    {
        final boolean dropsBomb = controller.dropsBomb();
        final IPlayerController.Direction direction = controller.getCurrent();
        return new CControllerState(adapt(direction), dropsBomb);
    }

    /*
     * 
     */
    public static CDirection adapt(Direction direction)
    {
        if (direction == null) return CDirection.NONE;
        switch (direction)
        {
            case LEFT:
                return CDirection.LEFT;
            case RIGHT:
                return CDirection.RIGHT;
            case UP:
                return CDirection.UP;
            case DOWN:
                return CDirection.DOWN;
        }
        throw new RuntimeException();
    }
}
