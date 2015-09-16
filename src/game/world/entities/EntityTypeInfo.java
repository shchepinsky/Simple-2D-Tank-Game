package game.world.entities;

import game.graphics.ImageFrameInfo;
import game.Resources;
import game.world.Board;
import game.world.Bounds;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EntityTypeInfo {

    public final String className;
    public final ImageFrameInfo imageInfo;
    public final byte maxHitPoints;
    public final int maxLifetime;
    public final int maxRange;

    public final double maxForwardSpeed;
    public final double maxReverseSpeed;
    public final double maxAcceleration;
    public final double maxDeceleration;
    public final double maxTurnSpeed;

    public final Bounds bounds;

    public EntityTypeInfo(String line) {
        String[] params = line.split("\\s*:\\s*");
        if (params.length < 9) throw new IllegalArgumentException("Wrong number of parameters in line: " + line);

        this.className          = params[0];
        this.imageInfo          = Resources.getFrameInfo(params[1]);
        this.maxHitPoints       = Byte.parseByte(params[2]);
        String[] speeds         = params[3].split("\\s*,\\s*");
        this.maxForwardSpeed    = Integer.parseInt(speeds[0]) / 1000.0;
        this.maxReverseSpeed    = Integer.parseInt(speeds[1]) / 1000.0;
        this.maxTurnSpeed       = Integer.parseInt(params[4]) / 1000.0;
        String[] accel          = params[5].split("\\s*,\\s*");
        this.maxAcceleration    = Integer.parseInt(accel[0]) / (1000*1000.0);
        this.maxDeceleration    = Integer.parseInt(accel[1]) / (1000*1000.0);
        this.bounds             = Bounds.fromText(params[6]);
        this.maxLifetime        = Integer.parseInt(params[7]);
        this.maxRange           = Integer.parseInt(params[8]);
    }

    public static Entity createFromBuffer(Board board, ByteBuffer buf) {
        byte typeIndex = buf.get(buf.position());           // get type byte from current entity
        EntityTypeInfo entityTypeInfo = Resources.ofIndex(typeIndex);
        UUID uuid = new UUID(0, 0);                         // uuid is not used when creating from buffer on client side

        switch (entityTypeInfo.className) {                 // determine appropriate class name
            case "Tank":                return new Tank(uuid, board, buf);
            case "Enemy":               return new Enemy(uuid, board, buf);
            case "Bullet":              return new Bullet(uuid, board, buf);
            case "SmallExplosion":      return new SmallExplosion(uuid, board, buf);
            case "MediumExplosion":     return new MediumExplosion(uuid, board, buf);
            case "SpecialExplosion":    return new SpecialExplosion(uuid, board, buf);
            default:
                throw new IllegalArgumentException("Unknown Entity typeIndex: " + typeIndex);
        }
    }
}
