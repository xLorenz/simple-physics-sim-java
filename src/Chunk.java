package src;

import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public List<PhysicsObject> objects = new ArrayList<>();

    public boolean isEmpty;

    Chunk() {

    }

    // update isEmpty
    public void updateState() {
        isEmpty = objects.isEmpty();
    }

}