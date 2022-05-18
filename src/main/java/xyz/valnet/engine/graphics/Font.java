package xyz.valnet.engine.graphics;

import java.util.Map;

import xyz.valnet.engine.math.Vector4i;

public class Font {

    private Map<Character, Sprite> charset;
    private final int w, h;

    public Font(Map<Character, Sprite> charset, int w, int h) {
        this.charset = charset;
        this.w = w;
        this.h = h;
    }

    public void drawString(String str, int x, int y) {
        int cursorX = x;
        int cursorY = y;

        Sprite s;

        for(char c : str.toCharArray()) {
            if(c == '\n') {
                cursorY += h;
                cursorX = x;
                continue;
            }
            if(c == '\r') continue;
            if(c == '\t') {
                cursorX += w * 4;
                continue;
            }
            if(c == ' ' || !charset.containsKey(c)) {
                cursorX += w;
                continue;
            }
            s = charset.get(c);
            Drawing.drawSprite(s, cursorX, cursorY);
            cursorX += w;
        }
    }

    public Vector4i measure(String text) {
      String[] lines = text.split("\n");
      int longest = 0;
      int c = 0;
      for(String line : lines) {
        c = line.length();
        if(c > longest) longest = c;
      }
      return new Vector4i(longest * w, lines.length * h, 0, 0);
    }
}
