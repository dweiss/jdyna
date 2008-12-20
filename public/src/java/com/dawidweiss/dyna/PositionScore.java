package com.dawidweiss.dyna;

import java.awt.Point;
import java.util.Comparator;

/**
 * On many occasions we will probably need to "score" a position on the board. This
 * is a utility class for such scoring.
 */
public class PositionScore
{
    public final Point position;
    public final float score;

    PositionScore(float score, Point position)
    {
        this.score = score;
        this.position = position;
    }
    
    @Override
    public String toString()
    {
        return "[" + position + ", score=" + score + "]";
    }

    public static final Comparator<PositionScore> BY_SCORE_ASC
        = new Comparator<PositionScore>()
    {
        public int compare(PositionScore o1, PositionScore o2)
        {
            if (o1.score < o2.score) return -1;
            if (o1.score > o2.score) return 1;
            return 0;
        }
    };

    public static final Comparator<PositionScore> BY_SCORE_DESC
        = new Comparator<PositionScore>()
    {
        public int compare(PositionScore o1, PositionScore o2)
        {
            return -BY_SCORE_ASC.compare(o1, o2);
        }
    };        
}