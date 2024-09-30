package game.core.rendering;

import game.Color;
import game.ecs.ECSystem;
import game.ecs.comps.Transform;

import com.raylib.Raylib;

public class TriangleRenderer extends ECSystem {
    Triangle triangle;
    Transform trans;

    @Override
    public void setup() {
        triangle = require(Triangle.class);
        trans = require(Transform.class);
    }
    
    @Override
    public void render() {
        triangle.position = trans.position;
        triangle.updatePoints();

        Raylib.rlPushMatrix();
        Raylib.rlTranslatef(triangle.position.x, triangle.position.y, 0);
        Raylib.rlRotatef(trans.rotation, 0, 0, -1);
        Raylib.rlTranslatef(-triangle.position.x, -triangle.position.y, 0);

        triangle.render();

        Raylib.rlPopMatrix();
    }
}
