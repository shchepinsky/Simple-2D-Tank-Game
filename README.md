A simple 2D game created for fun and self-edicational purposes.
Topics touched:

1. Java language: exception handling, collections, interfaces, lambdas, threads, datagram channels, timing.
2. JavaFX framework: scene control injection, canvas.
3. JUnit: simple unit tests.
4. OOP: designing class hierarchy, inheritance, polymorphism, client-server principle.
5. design patterns: DRY, KISS, and some SOLID.
6. Path finding via A-star algorithm, basic AI

In-game controls:
arrow keys: turn and move in arrow direction.
shift: hold shift with arrow keys to move backwards.
space: fire cannon.

Some hot-keys in the game:
D: self-destruct :)
G: toggle display of map grid on/off.
E: toggle display of entity info on/off.
B: toggle collision bounds on/off.
A: toggle viewport coordinates on/off.
~: toggle console with various client and server state information.
INSERT: increase maximum AI player count, will take effect on enemy respawn.
DELETE: decrease maximum AI player count, will take effect on enemy respawn.
PLUS: make game speed faster
MINUS: make game speed slower
P: toggle pause on/off.
U: toggle local updates on/off and leaves only server ones.

BUGS:

1. Moving along left or top side of board results in sloppy movement of viewport.
This side-effect appears only on client side and is related to local updates and collision detection.
2. Erratic animation with local update on. This is due to server time sent in packets
is slightly late than client's time being locally updated. Possible fix: take most largest frame index when
updating from network.
3. AI players targeting each other sometimes stop in diagonal cells waiting for target to move. This results in mutual
waiting so no one moves. Probably can be fixed by introducing random decisions to AI, but currently this is left out.