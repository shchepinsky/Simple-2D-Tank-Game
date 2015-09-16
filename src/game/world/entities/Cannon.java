package game.world.entities;

import game.Resources;

/**
 * Controls entity firing, reaming delay and bullet entity generation.
 */
public class Cannon {
    private final Positionable ownerEntity;

    private final double MAX_FIRE_RATE = 0.5;               // be careful, more than 100 can generate too much packets!

    private double rearmingDelay = 1000 / MAX_FIRE_RATE;

    public Cannon(Positionable ownerEntity) {
        this.ownerEntity = ownerEntity;
    }

    public boolean canShoot() { return rearmingDelay == 0; }

    public short shoot() {
        rearmingDelay = 1000 / MAX_FIRE_RATE;

        Bullet bullet = new Bullet(ownerEntity.getOwnerUniqueID(), ownerEntity.getBoard());

        double heading = ownerEntity.getHeading();

        Point pos = muzzlePoint();

        bullet.setParentKey(ownerEntity.getKey());          // set parent to exclude self from collision detection
        bullet.setPos(pos);                                 // calculated position
        bullet.setHeading(heading);                         // orientation as requested
        bullet.setOrderedHeading(heading);                  //

        ownerEntity.getBoard().registerEntity(bullet);

        return bullet.getKey();
    }

    public Point muzzlePoint() {
        double barrelLength = 0;
        double heading = ownerEntity.getHeading();

        if (ownerEntity instanceof Collidable) {            // if dimensions are available - use them
            Collidable collidable = (Collidable) ownerEntity;
            barrelLength = barrelLength + collidable.getBounds().getHeight() / 2;
        }

        return ownerEntity.getPos().at(heading, barrelLength);
    }

    public void update() {
        if (rearmingDelay >0) rearmingDelay--;
    }

    public int maximumRange() {
        EntityTypeInfo bulletInfo = Resources.ofClass(Bullet.class);
        return bulletInfo.maxRange;
    }
}
