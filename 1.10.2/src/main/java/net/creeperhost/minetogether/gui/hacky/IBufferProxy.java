package net.creeperhost.minetogether.gui.hacky;

import net.minecraft.client.renderer.vertex.VertexFormat;

/**
 * Created by Aaron on 11/06/2017.
 */
public interface IBufferProxy
{
    void begin(int glMode, VertexFormat format);

    IBufferProxy pos(double x, double y, double z);

    IBufferProxy color(int red, int green, int blue, int alpha);

    IBufferProxy tex(double u, double v);

    void endVertex();
}
