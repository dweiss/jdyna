package pl.khroolick.dyna;


import com.dawidweiss.dyna.IPlayerController;
import com.dawidweiss.dyna.IPlayerFactory;

public class HydraFactory implements IPlayerFactory
{

    @Override
    public IPlayerController getController(String playerName)
    {
        return new Hydra(playerName);
    }

    @Override
    public String getDefaultPlayerName()
    {
        return "Hydra";
    }

    @Override
    public String getVendorName()
    {
        return "Przemysław Król";
    }

}
