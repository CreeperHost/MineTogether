package net.creeperhost.creeperhost.gui.hacky;

/**
 * Created by Aaron on 12/06/2017.
 */
public class BufferProxyGetterOld implements IBufferProxyGetter
{
    @Override
    public IBufferProxy get()
    {
        return new BufferProxyOld();
    }
}
