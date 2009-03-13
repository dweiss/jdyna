package pl.khroolick.dyna;


import org.jdyna.IPlayerController;
import org.jdyna.IPlayerFactory;

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
