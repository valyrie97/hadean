package xyz.valnet.hadean.gameobjects.worldobjects.agents;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;
import static org.lwjgl.opengl.GL20.glVertexAttrib2f;

import xyz.valnet.engine.graphics.Drawing;
import xyz.valnet.engine.math.Vector2f;
import xyz.valnet.engine.math.Vector2i;
import xyz.valnet.engine.math.Vector4f;
import xyz.valnet.engine.shaders.SimpleShader;
import xyz.valnet.hadean.gameobjects.Terrain;
import xyz.valnet.hadean.gameobjects.Tile;
import xyz.valnet.hadean.gameobjects.worldobjects.WorldObject;
import xyz.valnet.hadean.interfaces.ISelectable;
import xyz.valnet.hadean.pathfinding.AStarPathfinder;
import xyz.valnet.hadean.pathfinding.IPathfinder;
import xyz.valnet.hadean.pathfinding.Node;
import xyz.valnet.hadean.pathfinding.Path;
import xyz.valnet.hadean.util.Action;
import xyz.valnet.hadean.util.Assets;
import xyz.valnet.hadean.util.Layers;

public abstract class Agent extends WorldObject implements ISelectable {
  public abstract String getName();
  private int frameCounter = 0;
  private int speed = 100 + (int)(Math.random() * 50);

  private IPathfinder pathfinder;
  private Path path = null;

  protected boolean isPathing() {
    return path != null && !path.isComplete();
  }

  @Override
  public void start() {
    super.start();
    frameCounter = speed;
    pathfinder = new AStarPathfinder(terrain);
  }

  @Override
  public void update(float dTime) {
    think();
    act();
  }

  private void move() {
    frameCounter++;
    if(frameCounter >= speed) {
      Vector2i nextPos = path.pop().getPosition();
      this.x = nextPos.x;
      this.y = nextPos.y;
      if(path.isComplete()) path = null;
      frameCounter = 0;
    }
  }

  private void correctPath() {
    if(path != null && path.isComplete()) path = null;
    if(path == null) return;
    Tile nextTile = terrain.getTile(path.peek().getPosition());
    if(!nextTile.isWalkable()) {
      path = pathfinder.getPath(
        (int)Math.floor(x),
        (int)Math.floor(y),
        path.dst.x,
        path.dst.y
      );
    }
  }

  protected void think() {
    correctPath();
  }

  protected boolean act() {
    if(path != null) {
      move();
      return true;
    }

    return false;
  }

  protected void goTo(int x, int y) {
    if(x == (int) this.x && y == (int) this.y) return;
    frameCounter = 0;
    path = pathfinder.getPath((int)this.x, (int)this.y, x, y);
  }

  protected void goTo(Vector2i location) {
    goTo(location.x, location.y);
  }

  protected void wander() {
    System.out.println("WANDER!");
    int randomX = (int)Math.floor(Math.random() * Terrain.WORLD_SIZE);
    int randomY = (int)Math.floor(Math.random() * Terrain.WORLD_SIZE);
    path = pathfinder.getPath((int)x, (int)y, randomX, randomY);
  }

  @Override
  public void renderAlpha() {
    Drawing.setLayer(Layers.GENERAL_UI);
    Assets.flat.pushColor(Vector4f.opacity(0.4f));
    if(path != null) {
      for(Node node : path) {
        glBegin(GL_LINES);
          Vector2f u, v;

          if(node.from == null) u = camera.world2screen(x, y);
          else u = camera.world2screen(node.from.x + 0.5f, node.from.y + 0.5f);

          v = camera.world2screen(node.x + 0.5f, node.y + 0.5f);
          glVertexAttrib2f(SimpleShader.TEX_COORD, 0, 88 / 256f);
          glVertex3f(u.x, u.y, 3f);
          glVertexAttrib2f(SimpleShader.TEX_COORD, 0, 88 / 255f);
          glVertex3f(v.x, v.y, 3f);
        glEnd();
      }
      Assets.flat.swapColor(Vector4f.opacity(0.6f));

      Assets.selectionFrame.draw(
        camera.world2Screen(
          terrain.getTile(
            path.getDestination().getPosition()
          )
          .getWorldBox()
        )
        .toXYWH()
      );
    }
    Assets.flat.popColor();
  }

  @Override
  public Action[] getActions() {
    return new Action[0];
  }

  protected Vector2i getDestination() {
    if(path == null) return null;
    return path.getDestination().getPosition();
  }
}