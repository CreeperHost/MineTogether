package net.creeperhost.minetogether.gui.hacky;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.VertexFormat;

/**
 * Created by Aaron on 12/06/2017.
 */
public class BufferProxyOld implements IBufferProxy
{
    private VertexBuffer buffer;

    public BufferProxyOld() {
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
        buffer.tex(u, v);
        return this;
    }

    @Override
    public void endVertex()
    {
        buffer.endVertex();
    }
}
