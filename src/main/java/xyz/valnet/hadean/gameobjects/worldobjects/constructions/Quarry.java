package xyz.valnet.hadean.gameobjects.worldobjects.constructions;

import xyz.valnet.engine.math.Vector2i;
import xyz.valnet.engine.math.Vector4f;
import xyz.valnet.hadean.gameobjects.Job;
import xyz.valnet.hadean.gameobjects.JobBoard;
import xyz.valnet.hadean.gameobjects.worldobjects.Buildable;
import xyz.valnet.hadean.gameobjects.worldobjects.items.Boulder;
import xyz.valnet.hadean.interfaces.BuildableMetadata;
import xyz.valnet.hadean.interfaces.IWorkable;
import xyz.valnet.hadean.util.Assets;
import xyz.valnet.hadean.util.Layers;

@BuildableMetadata(category = "Buildings", name = "Quarry", type = BuildableMetadata.Type.SINGLE)
public class Quarry extends Buildable {

  private float work = 0;
  private static float MAX_WORK = 10000;

  private Job digJob = null;

  @Override
  public void create() {
    super.create();
    get(JobBoard.class).postSimpleWorkJob("Build Quarry", new IWorkable() {
      @Override
      public boolean doWork(float dTime) {
        work += dTime;
        return isBuilt();
      }

      @Override
      public Vector2i[] getWorkablePositions() {
        return new Vector2i[] {
          getWorldPosition().xy().south().east()
        };
      }

      @Override
      public String getJobName() {
        return "Build Quarry";
      }
    });
  }

  @Override
  public void render() {
    if(isBuilt()) {
      camera.draw(Layers.TILES, Assets.quarry, getWorldPosition());

      if(digJob != null && !digJob.isCompleted()) {
        camera.drawProgressBar(digProgress, getWorldBox());
      }

    } else {
      float p = work / MAX_WORK;
      float b = 4;

      Assets.flat.pushColor(new Vector4f(b, b, b, 0.5f));
      camera.draw(Layers.GROUND, Assets.quarry, getWorldPosition());
      Assets.flat.popColor();

      camera.drawProgressBar(p, getWorldBox());
    }
  }

  @Override
  public Vector2i getDimensions() {
    return new Vector2i(3, 3);
  }

  private float digProgress = 0;

  private void tryCreateDigJob() {
    if(!isBuilt()) return;
    if (digJob != null) return;
    if (terrain.getTile(getWorldPosition().xy().south().east()).has(Boulder.class)) return;

    digJob = get(JobBoard.class)
      .postSimpleWorkJob("Mine at Quarry", new IWorkable() {

        private static float MAX_WORK = 5000;
        private float work = 0;

        @Override
        public boolean doWork(float dTime) {
          work += dTime;
          digProgress = work / MAX_WORK;
          return work >= MAX_WORK;
        }

        @Override
        public Vector2i[] getWorkablePositions() {
          return new Vector2i[] {
            getWorldPosition().xy().south().east()
          };
        }

        @Override
        public String getJobName() {
          return "Mine at Quarry";
        }
      
    });
    digJob.registerClosedListener(() -> {
      digProgress = 0;
      Vector2i dropPos = getWorldPosition().xy().south().east();
      Boulder boulder = new Boulder(dropPos.x, dropPos.y);
      add(boulder);
      boulder.runAction(Boulder.HAUL);
      digJob = null;
    });
  }

  @Override
  public void update(float dTime) {
    super.update(dTime);
    tryCreateDigJob();
  }

  private boolean isBuilt() {
    return work >= MAX_WORK;
  }

  @Override
  public boolean isWalkable() {
    return true;
  }

  @Override
  public boolean shouldRemove() {
    return false;
  }

  @Override
  public void onRemove() {
  }

  @Override
  public String getName() {
    return "Quarry";
  }
}