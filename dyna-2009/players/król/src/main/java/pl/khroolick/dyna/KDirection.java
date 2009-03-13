package pl.khroolick.dyna;

import java.awt.Point;

import org.jdyna.IPlayerController;


public enum KDirection
{
    LEFT
    {
        public int getXSign()
        {
            return -1;
        }

        public int getYSign()
        {
            return 0;
        }

        public IPlayerController.Direction getDirection()
        {
            return IPlayerController.Direction.LEFT;
        }
    },
    UP
    {
        public int getXSign()
        {
            return 0;
        }

        public int getYSign()
        {
            return -1;
        }

        public IPlayerController.Direction getDirection()
        {
            return IPlayerController.Direction.UP;
        }
    },
    RIGHT
    {
        public int getXSign()
        {
            return 1;
        }

        public int getYSign()
        {
            return 0;
        }

        public IPlayerController.Direction getDirection()
        {
            return IPlayerController.Direction.RIGHT;
        }
    },
    DOWN
    {
        public int getXSign()
        {
            return 0;
        }

        public int getYSign()
        {
            return 1;
        }

        public IPlayerController.Direction getDirection()
        {
            return IPlayerController.Direction.DOWN;
        }
    };

    public abstract int getXSign();

    public abstract int getYSign();

    public abstract IPlayerController.Direction getDirection();

    public static KDirection [] getDesiredDirs(Point targetOffset, KDirection [] array)
    {
        KDirection [] allDirs = KDirection.values();

        if (array == null)
        {
            // allocate new one
            array = new KDirection [allDirs.length];
        }

        final int tarX = targetOffset.x;
        final int tarY = targetOffset.y;
        int startId = 0;
        if (tarX < 0 && tarY <= 0)
        {
            startId = 0;
        }
        else if (tarX >= 0 && tarY < 0)
        {
            startId = 1;
        }
        else if (tarX > 0 && tarY >= 0)
        {
            startId = 2;
        }
        else if (tarX <= 0 && tarY > 0)
        {
            startId = 3;
        }

        for (int i = 0; i < allDirs.length; i++)
        {
            array[i] = allDirs[(startId + i) % allDirs.length];
        }
        return array;
    }
}
