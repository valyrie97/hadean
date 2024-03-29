package xyz.valnet.hadean.pathfinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import xyz.valnet.engine.math.Vector2i;

public class AStarPathfinder implements IPathfinder, Serializable {

  private IPathable pathable;

  public AStarPathfinder(IPathable pathable) {
    this.pathable = pathable;
  }

  private Node findNodeInList(List<Node> nodes, int x, int y) {
    for(Node node : nodes) {
      if(node.x == x && node.y == y) return node;
    }
    return null;
  }

  private Node getPathfindingNode(int x, int y, List<Node> open, List<Node> closed, Node parent, int dstX, int dstY) {
    if(pathable.isOutOfBounds(x, y)) {
      return null;
    }

    Node node = findNodeInList(open, x, y);
    if(node == null) {
      node = findNodeInList(closed, x, y);
    }
    if(node == null) {
      node = new Node();
      node.x = x;
      node.y = y;
      node.from = parent;
      node.g = 0;
      if(parent != null) {
        node.g += parent.g;
      }
      if(node.from == null) {
        node.g = 0;
      } else {
        node.g += (int)(Math.round(10 * Math.sqrt(Math.pow(node.from.x - x, 2) + Math.pow(node.from.y - y, 2))));
      }
      node.h = (int)(Math.round(10 * Math.sqrt(Math.pow(dstX - x, 2) + Math.pow(dstY - y, 2))));
    }
    return node;
  }

  private Node[] getPathableNodesFromNode(Node base, List<Node> open, List<Node> closed, int dstX, int dstY) {
    List<Node> nodes = new ArrayList<Node>();
    Node north = getPathfindingNode(base.x,     base.y - 1, open, closed, base, dstX, dstY);
    Node east =  getPathfindingNode(base.x - 1, base.y,     open, closed, base, dstX, dstY);
    Node south = getPathfindingNode(base.x,     base.y + 1, open, closed, base, dstX, dstY);
    Node west =  getPathfindingNode(base.x + 1, base.y,     open, closed, base, dstX, dstY);

    boolean northWalkable = north != null && pathable.isWalkable(north.x, north.y);
    boolean eastWalkable =  east  != null && pathable.isWalkable(east.x, east.y);
    boolean southWalkable = south != null && pathable.isWalkable(south.x, south.y);
    boolean westWalkable =  west  != null && pathable.isWalkable(west.x, west.y);

    if(northWalkable) nodes.add(north);
    if(eastWalkable)  nodes.add(east);
    if(southWalkable) nodes.add(south);
    if(westWalkable)  nodes.add(west);

    if(northWalkable && eastWalkable) nodes.add(getPathfindingNode(base.x - 1, base.y - 1, open, closed, base, dstX, dstY));
    if(northWalkable && westWalkable) nodes.add(getPathfindingNode(base.x + 1, base.y - 1, open, closed, base, dstX, dstY));
    if(southWalkable && eastWalkable) nodes.add(getPathfindingNode(base.x - 1, base.y + 1, open, closed, base, dstX, dstY));
    if(southWalkable && westWalkable) nodes.add(getPathfindingNode(base.x + 1, base.y + 1, open, closed, base, dstX, dstY));

    Node[] nodes2 = new Node[nodes.size()];
    nodes.toArray(nodes2);

    return nodes2;
  }

  public Path getPath(int x1, int y1, int x2, int y2) {

    if(x1 == x2 && y1 == y2) return null;

    List<Node> open = new ArrayList<Node>();
    List<Node> closed = new ArrayList<Node>();

    if(!pathable.isWalkable(x2, y2)) return null;

    open.add(getPathfindingNode(x1, y1, open, closed, null, x2, y2));
    
    while (open.size() != 0) {
      open.sort(new Comparator<Node>() {
        @Override
        public int compare(Node a, Node b) {
          int aCost = a.getCost();
          int bCost = b.getCost();
          if(aCost > bCost) {
            return 1;
          } else if (aCost < bCost) {
            return -1;
          } else if (a.h > b.h) {
            return 1;
          } else if (a.h < b.h) {
            return -1;
          } else {
            return 0;
          }
        }
      });
      Node current = open.get(0);

      open.remove(current);
      closed.add(current);

      if(current.x == x2 && current.y == y2) {
        Stack<Node> path = new Stack<Node>();

        Node n = current;
        while(n != null) {
          path.push(n);
          n = n.from;
        }

        path.pop();

        return new Path(path, current);
      }

      Node[] neighbors = getPathableNodesFromNode(current, open, closed, x2, y2);

      for(Node node : neighbors) {
        if(node == null) continue;
        if(closed.contains(node)) continue;
        if(!pathable.isWalkable(node.x, node.y)) continue;
        if(open.contains(node)) {
          int newGCost = current.g + (int)(Math.round(10 * Math.sqrt(Math.pow(node.x - current.x, 2) + Math.pow(node.y - current.y, 2))));
          
          if(node.g > newGCost) {
            if(closed.contains(node)) {
              closed.remove(node);
            }
            if(!open.contains(node)) {
              open.add(node);
            }
            node.g = newGCost;
            node.from = current;
          }
        } else {
          open.add(node);
        }


      }

    }

    // i guess theres no path?!

    return null;
  }

  @Override
  public Path getBestPath(Vector2i src, Vector2i[] dsts) {

    if(src.isOneOf(dsts)) return null;

    int cost = Integer.MAX_VALUE;
    Path bestPath = null;
    for(Vector2i dst : dsts) {
      Path path = getPath(src.x, src.y, dst.x, dst.y);
      if(path == null) continue;
      if(path.cost < cost) {
        cost = path.cost;
        bestPath = path;
      }
    }
    return bestPath;
  }
}
