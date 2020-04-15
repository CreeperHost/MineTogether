package net.creeperhost.minetogether.util;

/**
 * Created by Aaron on 02/05/2017.
 */
public class Pair<L, R>
{
    /* LETS REINVENT THE WHEEL */
    private final L left;
    private final R right;
    
    public Pair(L left, R right)
    {
        this.left = left;
        this.right = right;
    }
    
    public L getLeft()
    {
        return left;
    }
    
    public R getRight()
    {
        return right;
    }
}
