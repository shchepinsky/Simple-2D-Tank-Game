package game.world.entities;

// can be destroyed by damage
public interface Destructible {
    boolean isDead();
    byte getHitPoints();
    void takeDamage(byte amount);
}
