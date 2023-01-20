package xyz.valnet.hadean.gameobjects.worldobjects.constructions;

import java.util.ArrayList;
import java.util.List;

import xyz.valnet.engine.math.Vector2i;
import xyz.valnet.hadean.gameobjects.Job;
import xyz.valnet.hadean.gameobjects.JobBoard;
import xyz.valnet.hadean.gameobjects.worldobjects.Buildable;
import xyz.valnet.hadean.gameobjects.worldobjects.items.Boulder;
import xyz.valnet.hadean.gameobjects.worldobjects.items.Item;
import xyz.valnet.hadean.interfaces.IItemPredicate;
import xyz.valnet.hadean.interfaces.IItemReceiver;
import xyz.valnet.hadean.interfaces.IWorkable;

public abstract class Construction extends Buildable implements IItemReceiver {

  private float work = 0;

  private List<Item> containedItems = new ArrayList<Item>();

  protected abstract IItemPredicate getBuildingMaterial();
  protected abstract int getBuildingMaterialCount();
  protected Vector2i getDimensions() {
    return new Vector2i(1, 1);
  }

  private final boolean isBuildingMaterialSatisfied() {
    return containedItems.size() >= getBuildingMaterialCount();
  }

  private void postNextJob() {
    if(!isBuildingMaterialSatisfied()) {
      Job job = get(JobBoard.class).postSimpleItemRequirementJob(
        "Haul items to building",
        getBuildingMaterial(),
        this
      );
      job.registerClosedListener(() -> {
        postNextJob();
      });
      return;
    }
    if(!isBuilt()) {
      Job job = get(JobBoard.class).postSimpleWorkJob(
        "Build " + getName(),
        new IWorkable() {
          @Override
          public boolean doWork(float dTime) {
            work += dTime;
            return isBuilt();
          }
    
          @Override
          public Vector2i[] getWorkablePositions() {
            return getWorldBox().toXYWH().asInt().getBorders();
          }
    
          @Override
          public String getJobName() {
            return "Build " + getName();
          }
        }
      );
      job.registerClosedListener(() -> {
        postNextJob();
      });
      return;
    }
  }

  protected float getMaxWork() {
    return 1000;
  }

  public boolean isBuilt() {
    return work >= getMaxWork();
  }

  @Override
  public void create() {
    super.create();
    postNextJob();
  }

  @Override
  public abstract boolean isWalkable();

  @Override
  public boolean shouldRemove() {
    return false;
  }

  @Override
  public void onRemove() {
    
  }

  @Override
  public abstract String getName();
  
  protected final boolean isBuilding() {
    return isBuildingMaterialSatisfied();
  }

  protected final float getBuildProgress() {
    return work / getMaxWork();
  }

  @Override
  public boolean receive(Item item) {
    if(item == null) return false;
    if(!item.matches(Boulder.BOULDER_PREDICATE)) return false;
    remove(item);
    // boulders ++;
    return true;
  }

  @Override
  public Vector2i[] getItemDropoffLocations() {
    return getWorldBox().toXYWH().asInt().getBorders();
  }
}