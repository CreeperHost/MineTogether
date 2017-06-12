package net.creeperhost.creeperhost.gui.hacky;

/**
 * Created by Aaron on 12/06/2017.
 */
public class BufferProxyGetterNew implements IBufferProxyGetter
{
    @Override
    public IBufferProxy get()
    {
        return new BufferProxyNew();
    }
}
