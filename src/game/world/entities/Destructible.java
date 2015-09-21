package game.world.entities;

/**
 * An entity that acn be destroyed.
 */
public interface Destructible {
    boolean isDead();
    byte getHitPoints();
    void takeDamage(byte amount);
}
