package net.creeperhost.minetogether.client.screen.hacky;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;

/**
 * Created by Aaron on 12/06/2017.
 */
public class BufferProxyNew implements IBufferProxy
{
    private BufferBuilder buffer;
    
    public BufferProxyNew()
    {
        buffer = Tessellator.getInstance().getBuffer();
    }
    
    @Override
    public void begin(int glMode, VertexFormat format)
    {
        buffer.begin(glMode, format);
    }
    
    @Override
    public IBufferProxy pos(double x, double y, double z)
    {
        buffer.pos(x, y, z);
        return this;
    }
    
    @Override
    public IBufferProxy color(int red, int green, int blue, int alpha)
    {
        buffer.color(red, green, blue, alpha);
        return this;
    }
    
    @Override
    public IBufferProxy tex(double u, double v)
    {
        buffer.tex((float) u, (float) v);
        return this;
    }
    
    @Override
    public void endVertex()
    {
        buffer.endVertex();
    }
}
