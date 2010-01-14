package org.jdyna;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Random;

import com.google.common.collect.Maps;



public class BonusRandomizer implements Cloneable, Serializable
{
    /**
     * @see GameEvent#serialVersionUID
     */
    private static final long serialVersionUID = 0x201001131529L;
    
    /**
     * Bonus cells and their weights for calculating probability of selection
     * for random placement of bonuses.
     */
    private EnumMap<CellType, Integer> bonusWeights;
    private int sumOfWeights;
    private Random rnd = new Random();

    /**
     * Creates a bonus randomizer with default bonus weights.
     */
    public BonusRandomizer()
    {
        bonusWeights = Maps.newEnumMap(CellType.class);

        /*
         * We divide bonus probabilities into three categories for now.
         * I believe some of the bonuses may be outside of the default three
         * categories and may be assigned more/ less frequently (e.g., immortality). 
         */

        for (CellType ct : EnumSet.of(
            CellType.CELL_BONUS_BOMB, 
            CellType.CELL_BONUS_RANGE))
        {
            bonusWeights.put(ct, Constants.FREQUENT_BONUS_WEIGHT);
        }

        for (CellType ct : EnumSet.of(
            CellType.CELL_BONUS_MAXRANGE, 
            CellType.CELL_BONUS_SPEED_UP, 
            CellType.CELL_BONUS_CRATE_WALKING, 
            CellType.CELL_BONUS_AHMED))
        {
            bonusWeights.put(ct, Constants.COMMON_BONUS_WEIGHT);
        }

        for (CellType ct : EnumSet.of(
            CellType.CELL_BONUS_DIARRHEA, 
            CellType.CELL_BONUS_NO_BOMBS,
            CellType.CELL_BONUS_IMMORTALITY,
            CellType.CELL_BONUS_SLOW_DOWN,
            CellType.CELL_BONUS_BOMB_WALKING,
            CellType.CELL_BONUS_CONTROLLER_REVERSE,
            CellType.CELL_BONUS_SURPRISE))
        {
            bonusWeights.put(ct, Constants.COMMON_BONUS_WEIGHT);
        }
        
        // Calculate base sum of all weights for assignment probability distribution.
        int sum = 0;
        for (CellType ct : bonusWeights.keySet())
        {
            sum += bonusWeights.get(ct);
        }
        sumOfWeights = sum;
    }

    /**
     * Return a random surprise bonus (random bonus, but not {@link 
     * CellType#CELL_BONUS_SURPRISE}). 
     */
    public CellType surpriseBonus()
    {
        return CellType.BONUSES_NO_SURPRISE.get(rnd.nextInt(CellType.BONUSES_NO_SURPRISE.size()));
    }
    
    /**
     * Return a random bonus, according to probability distribution given by weights
     * of each cell type. 
     */
    public CellType randomBonus()
    {
        final int t = rnd.nextInt(sumOfWeights);
        int s = 0;
        for (CellType ct : bonusWeights.keySet())
        {
            final int v = bonusWeights.get(ct);
            if (t >= s && t < s + v)
                return ct;
            s += v;
        }
        throw new RuntimeException("Unreachable block: "
            + t + ", " + s);
    }
    
    /**
     * Sets new weight of given bonus.
     * 
     * @param bonus bonus to be re-weighted
     * @param weight new weight to set for bonus; it has to be non-negative
     */
    public void setWeightOfBonus(final CellType bonus, final int weight)
    {
        if (weight < 0)
            throw new ArithmeticException("Weight has to be non-negative.");
        final Integer oldWeight;
        // remove/update weight
        if (weight == 0)
            oldWeight = bonusWeights.remove(bonus);
        else
            oldWeight = bonusWeights.put(bonus, weight);
        // update sum of weights
        if (oldWeight != null)
            sumOfWeights -= oldWeight;
        sumOfWeights += weight;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public BonusRandomizer clone()
    {
        try
        {
            final BonusRandomizer objectClone = (BonusRandomizer) super.clone();
            objectClone.rnd = new Random();
            objectClone.bonusWeights = Maps.newEnumMap(CellType.class);
            for (CellType ct: bonusWeights.keySet())
                objectClone.bonusWeights.put(ct, bonusWeights.get(ct));
            return objectClone;
        }
        catch (CloneNotSupportedException e)
        {
            // intentional fall-through - it can never occur
            return null;
        }
    }
}
