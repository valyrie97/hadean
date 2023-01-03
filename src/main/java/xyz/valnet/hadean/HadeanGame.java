package xyz.valnet.hadean;

import java.util.ArrayList;
import java.util.List;

import xyz.valnet.engine.App;
import xyz.valnet.engine.Game;
import xyz.valnet.engine.graphics.Drawing;
import xyz.valnet.engine.math.Matrix4f;
import xyz.valnet.engine.math.Vector4f;
import xyz.valnet.hadean.scenes.GameScene;
import xyz.valnet.hadean.util.Assets;


public class HadeanGame extends Game {
  public static final HadeanGame Hadean = new HadeanGame();

  public static boolean debugView = false;

  public static void main(String[] args) {
    new App(Hadean).run();
  }

  @Override
  public void start() {
    Assets.flat.pushColor(Vector4f.one);
    changeScene(new GameScene());
  }

  @Override
  public void render() {
    Drawing.setLayer(0);
    super.render();

    if(!debugView) return;
    Drawing.setLayer(99);
    renderDebugInfo();
  }

  private static Runtime runtime = Runtime.getRuntime();
  private static Vector4f fontColor = new Vector4f(1, 0, 0, 1);

  private void renderDebugInfo() {
    
    long allocated = runtime.totalMemory();
    long max = runtime.maxMemory();
    int left = 770;
    int top = 10;

    List<String> strings = new ArrayList<String>();
    strings.add("     === [ DEBUG ] ===");
    strings.add("FPS:    " + Math.round(averageFPS) + "/" + measuredFPS + " | AVG/MEASURED");
    strings.add("Mouse:  <" + App.mouseX + ", " + App.mouseY + ">");
    strings.add("MEMORY: " + (int)((allocated / (double)max) * 100) + "% (" + (allocated / (1024 * 1024)) + "/" + (max / (1024 * 1024)) + "MB)");
    strings.add("dTime:  " + dTime);

    for(String str : strings) {
      Assets.flat.pushColor(Vector4f.black);
      Assets.font.drawString(str, left + 1, top + 1);
      Assets.flat.swapColor(fontColor);
      Assets.font.drawString(str, left, top);
      Assets.flat.popColor();
      top += 16;
    }
  }

  // receive the updated matrix every frame for the actual window.
  @Override
  public void updateViewMatrix(Matrix4f matrix) {
    Assets.flat.setMatrices(matrix);
  }
}
