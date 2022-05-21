package xyz.valnet.hadean.gameobjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.valnet.engine.scenegraph.GameObject;
import xyz.valnet.hadean.input.Button;
import xyz.valnet.hadean.input.IButtonListener;
import xyz.valnet.hadean.input.SimpleButton;
import xyz.valnet.hadean.util.Action;
import xyz.valnet.hadean.util.Assets;

public class SelectionUI extends GameObject implements ISelectionChangeListener, IButtonListener {

  private String name = "";
  private int count = 0;
  private List<ISelectable> selected = new ArrayList<ISelectable>();
  private HashMap<String, Integer> selectedTypes = new HashMap<String, Integer>();
  private HashMap<String, Button> narrowButtons = new HashMap<String, Button>();
  private HashMap<Button, List<ISelectable>> narrowBuckets = new HashMap<Button, List<ISelectable>>();

  private static final Button[] ACTIONS_BUTTONS_NULL = new Button[] {};

  private Button[] actionButtons = ACTIONS_BUTTONS_NULL;

  private Selection selectionManager;

  // this will be null normally, but set if
  // a button has been pressed to update the selection.
  // its a simple workaround to get rid of a concurrent
  // exception, where the buttons are attempting to
  // change while updating. 
  // TODO this could be fixed by delaying button clicks to the next frame.
  private List<ISelectable> newSelection = null;;

  public void start() {
    selectionManager = get(Selection.class);
    selectionManager.subscribe(this);
  }

  public void render() {
    if(selected.isEmpty()) return;

    Assets.uiFrame.draw(10, 366, 300, 200);

    // int i = 0;
    // for(String name : selectedTypes.keySet()) {
    //   int n = selectedTypes.get(name);
    //   Assets.font.drawString("" + n + "x " + name, 26, 376 + 16 * i);
    //   i ++;
    // }


    if(selectedTypes.size() == 1) {
      Assets.font.drawString("" + count + "x " + name, 26, 376);

      if(count == 1) {
        String details = selected.get(0).details();
        Assets.font.drawString(details, 26, 376 + 32);
      }
      
      for(Button btn : actionButtons) {
        btn.draw();
      }

    } else {
      for(Button btn : narrowButtons.values()) {
        btn.draw();
      }
    }

  }

  @Override
  public void tick(float dTime) {
    if(newSelection != null) {
      selectionManager.updateSelection(newSelection);
      newSelection = null;
    }

    if(selectedTypes.size() == 1) {
      for(Button btn : actionButtons) {
        btn.update();
      }
    } else {
      for(Button btn : narrowButtons.values()) {
        btn.update();
      }
    }
  }

  private HashMap<Button, Action> buttonActionMap = new HashMap<Button, Action>();

  @Override
  public void selectionChanged(List<ISelectable> selected) {
    this.selected = selected;

    selectedTypes.clear();
    narrowButtons.clear();
    narrowBuckets.clear();
    buttonActionMap.clear();
    actionButtons = ACTIONS_BUTTONS_NULL;
    for(ISelectable selectable : selected) {
      String name = selectable.getClass().getName();
      String[] splitName = name.split("\\.");
      String shortName = splitName[splitName.length - 1];
      
      if(selectedTypes.containsKey(name)) {
        selectedTypes.replace(name, selectedTypes.get(name) + 1);
        Button btn = narrowButtons.get(name);
        List<ISelectable> items = narrowBuckets.get(btn);
        items.add(selectable);
        btn.setText("" + items.size() + "x " + shortName);
        count ++;
      } else {
        Button btn = new SimpleButton("1x " + shortName, 20, 376 + 30 * selectedTypes.size(), 280, 24);
        btn.registerClickListener(this);
        selectedTypes.put(name, 1);
        narrowButtons.put(name, btn);
        List<ISelectable> list = new ArrayList<ISelectable>();
        list.add(selectable);
        narrowBuckets.put(btn, list);
        count = 1;
        this.name = shortName;
        if(actionButtons == ACTIONS_BUTTONS_NULL) {
          Action[] actions = selectable.getActions();
          Button[] actionButtons = new Button[actions.length];
          for(int i = 0; i < actions.length; i ++) {
            actionButtons[i] = new SimpleButton(actions[i].name, 320 + i * 110, 466, 100, 100);
            actionButtons[i].registerClickListener(this);
            buttonActionMap.put(actionButtons[i], actions[i]);
          }
          this.actionButtons = actionButtons;
        }
      }
    }
  }

  @Override
  public void click(Button target) {
    if(narrowBuckets.containsKey(target)) {
      newSelection = narrowBuckets.get(target);
    } else if(buttonActionMap.containsKey(target)) {
      Action action = buttonActionMap.get(target);
      for(ISelectable selectable : selected) {
        selectable.runAction(action);
      }
    }
  }
}
