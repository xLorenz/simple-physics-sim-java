# Simple Physics Simulator

A very simple version of a physics engine for game development. Fully developed in java with its own Vector2 implementation. 

https://github.com/user-attachments/assets/eef61410-b232-4e8a-bff5-b7d5e09e16e8



## Features 
- Colision detection and reaction between two objects
- Two Physics Objects implementations, Physics Circle and Physics Rect
- Process of gravity, elastic forces and inmovable objects
- Custom Chunk system
- Contacts information collection
- Sleeping objects
- Supported state for objects
- Camera position and scale modification

## The Process
I developed this physics engine with the goal of using it for game design later. I wanted data structures from where i can just inherit to a playable character and have it run flawlessly. 
It doesn't offer much, but it's open for expansion in the future. I'll be personally using it, fixing any bugs and implementing missing features. 

## Running the Project
1. Clone the repository
2. Run the main function
3. That's it. If you want to interact with te physics currently you'll have to modify [Panel.java](https://github.com/xLorenz/simple-physics-sim-java/blob/05bf181484559db0e55515ce2def97f44173aa5e/physics/Panel.java).

Spawn circles with SPACE and CTRL, spawn rects with RIGHT and LEFT clicks.
Spawn a camera tracked circle with LSHIFT + SPACE.
Delete objects with LSHIFT + RIGHT and LEFT clicks.
Clear the map with C.
Toggle debug display with X.
Zoom in/out with UP/DOWN.

## Previews


https://github.com/user-attachments/assets/b7bd5191-2315-471c-bebc-7b02d88d927e




https://github.com/user-attachments/assets/3773c8af-73d3-48e0-afca-47d9a9fcc454

