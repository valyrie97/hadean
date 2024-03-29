package xyz.valnet.engine.math;

import java.io.Serializable;

public class Vector2i implements Serializable {

	public int x, y;

  public static Vector2i one = new Vector2i(1, 1);
  public static Vector2i zero = new Vector2i(0, 0);

	public Vector2i() {
		x = 0;
		y = 0;
	}

	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

  public boolean equals(Vector2i other) {
    return x == other.x && y == other.y;
  }

  public boolean isOneOf(Vector2i[] others) {
    for(Vector2i other : others) {
       if(other.equals(this)) {
        return true;
      }
    }
    return false;
  }

  public float distanceTo(int x, int y) {
    int a = this.x - x;
    int b = this.y - y;
    return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
  }

  public Vector2f asFloat() {
    return new Vector2f(x, y);
  }

  public Vector2i north() {
    return new Vector2i(x, y - 1);
  }

  public Vector2i east() {
    return new Vector2i(x + 1, y);
  }

  public Vector2i south() {
    return new Vector2i(x, y + 1);
  }

  public Vector2i west() {
    return new Vector2i(x - 1, y);
  }

  public TileBox getTileBox() {
    return new TileBox(x, y, 1, 1);
  }

  public String toString() {
    return "<" + x + ", " + y + ">";
  }

  public Vector2i sub(Vector2i b) {
    return new Vector2i(this.x - b.x, this.y - b.y);
  }

}
