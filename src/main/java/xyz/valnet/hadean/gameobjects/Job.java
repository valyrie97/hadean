package xyz.valnet.hadean.gameobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import xyz.valnet.engine.math.Vector2f;
import xyz.valnet.engine.math.Vector2i;
import xyz.valnet.engine.math.Vector4f;
import xyz.valnet.engine.scenegraph.GameObject;
import xyz.valnet.hadean.gameobjects.worldobjects.Stockpile;
import xyz.valnet.hadean.gameobjects.worldobjects.items.Item;
import xyz.valnet.hadean.interfaces.IWorkable;

public class Job extends GameObject {

  private Job that = this;
  private List<Callback> closedListeners = new ArrayList<Callback>();

  public abstract class JobStep {
    public abstract Vector2i[] getLocations();
    public void next() {
      that.nextStep();
    }
  }

  public class PickupItem extends JobStep {
    public Item item;
    public Vector2f[] locations;
    
    public PickupItem(Item item, Vector2f[] possibleLocations) {
      this.item = item;
    }

    @Override
    public Vector2i[] getLocations() {
      return new Vector2i[] { item.getWorldPosition().asInt() };
    }
  }

  public class DropoffAtStockpile extends JobStep {
    public Item item;
    public DropoffAtStockpile(Item item) {
      this.item = item;
    }

    public Vector2i[] getLocations() {
      Stockpile pile = that.get(Stockpile.class);
      // Vector4f box = pile.getWorldBox().toXYWH();
      return new Vector2i[] {
        pile.getFreeTile()
      };
    }
  }

  public class Work extends JobStep {
    public IWorkable subject;
    public Work(IWorkable subject) {
      this.subject = subject;
    }
    @Override
    public Vector2i[] getLocations() {
      return subject.getWorkablePositions();
    }
    public boolean doWork() {
      return subject.doWork();
    }
  }

  private List<JobStep> steps;
  private String name;
  private int step;

  public void reset() {
    step = 0;
  }

  public Job(String name) {
    this.steps = new ArrayList<JobStep>();
    this.name = name;
  }

  public void addStep(JobStep step) {
    steps.add(step);
  }

  public Vector2i[] getLocations() {
    if(steps.size() == 0) throw new Error("Cannot get location of job with no steps");
    JobStep step = steps.get(0);
    return step.getLocations();
  }

  public void nextStep() {
    step ++;
    if(isCompleted()) {
      get(JobBoard.class).completeJob(this);
      for(Callback callback : closedListeners) {
        callback.apply();
      }
      remove(this);
    }
  }

  public boolean isCompleted() {
    return step >= steps.size();
  }

  public JobStep getCurrentStep() {
    if(step >= steps.size()) return null;
    return steps.get(step);
  }

  public String getJobName() {
    return name;
  }

  @FunctionalInterface
  public interface Callback {
    public void apply();
  }

  public void registerClosedListener(Callback callback) {
    closedListeners.add(callback);
  }
}
