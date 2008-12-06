package com.dawidweiss.dyna;

import com.dawidweiss.dyna.view.IBoardSnapshot;

/**
 * Events from {@link Game}.
 */
public interface IGameListener
{
    void onNextFrame(int frame, IBoardSnapshot snapshot);
}
