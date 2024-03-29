import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

class Graph {
  CornerSentinel corner;
  
  Graph(CornerSentinel corner) {
    this.corner = corner;
  }
  
  Graph(FromFileImage img) {
    int width = (int) img.getWidth();
    int height = (int) img.getHeight();
    CornerSentinel origin = new CornerSentinel();
    
    for (int i = 0; i < width; i++) {
      origin.addLeft(new BorderSentinel());
    }
    for (int row = 0; row < height; row++) {
      BorderSentinel border = new BorderSentinel();
      for (int col = 0; col < width; col++) {
        border.addLeft(new Pixel(img.getColorAt(col, row)));
      }
      origin.addRowAbove(border);
    }
    this.corner = origin;
  }
  
  Graph() {
    this.corner = new CornerSentinel();
  }
  
  void render(ComputedPixelImage img) {
    this.corner.render(img);
  }
  
  ASeamInfo getVerticalSeam() {
    ArrayList<ASeamInfo> seams = this.corner.getVerticalSeams();
    return new SeamCarvingUtils().getMinWeightSeam(seams);
  }
  
  /*
  ASeamInfo getHorizontalSeam() {
    ArrayList<ASeamInfo> seams = this.corner.getHorizontalSeams();
    return new SeamCarvingUtils().getMinWeightSeam(seams);
  }
  */
}

abstract class APixel {
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  
  APixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    new SeamCarvingUtils().checkWellFormedPixel(left, right, up, down);
    this.color = color;
    this.updateLeft(left);
    this.updateRight(right);
    this.updateUp(up);
    this.updateDown(down);
  }
  
  APixel(Color color) {
    this.color = color;
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
  }
  
  double getBrightness() {
    //colors / 3 / 255
      return (this.color.getRed()
        + this.color.getBlue()
        + this.color.getGreen()) / 765.0;
  }
  
  double getEnergy() {
    double horizontalEnergy =
        (this.up.left.getBrightness()
            + (2 * this.left.getBrightness())
            + this.down.left.getBrightness()) -
        (this.up.right.getBrightness()
            + (2 * this.right.getBrightness())
            + this.down.right.getBrightness());
    double verticalEnergy =
        (this.up.left.getBrightness()
            + (2 * this.up.getBrightness())
            + this.up.right.getBrightness()) -
        (this.down.left.getBrightness()
            + (2 * this.down.getBrightness())
            + this.up.right.getBrightness());
    return Math.sqrt(Math.pow(horizontalEnergy, 2.0) + Math.pow(verticalEnergy, 2.0));
  }
  
  void updateUp(APixel that) {
    this.up = that;
    that.down = this;
  }
  
  void updateRight(APixel that) {
    this.right = that;
    that.left = this;
  }
  
  void updateDown(APixel that) {
    this.down = that;
    that.up = this;
  }
  
  void updateLeft(APixel that) {
    this.left = that;
    that.right = this;
  }
  
  void removeVertically() {
    this.right.shiftLeft();
    this.left.updateRight(this.right);
  }
  
  /*
  void removeHorizontally() {
    this.down.shiftUp();
    this.up.updateDown(this.down);
  }
  */
  
  void shiftLeft() {
    this.right.shiftLeft();
    this.updateUp(this.left.up);
    this.updateDown(this.left.down);
  }
  
  /*
  void shiftUp() {
    this.down.shiftUp();
    this.updateRight(this.up.right);
    this.updateLeft(this.up.left);
  }
  */
  
  void shiftLeftBorder() {
    
  }
  
  /*
  void shiftUpBorder() {
  
  }
  */
  
  void addLeft(APixel newLeft) {
    this.left.updateRight(newLeft);
    this.updateLeft(newLeft);
  }
  
  void addAbove(APixel newUp) {
    this.up.updateDown(newUp);
    this.updateUp(newUp);
  }
  
  void addRowAboveHelp(APixel newTop) {
    this.addAbove(newTop);
    this.right.addRowAboveHelp(newTop.right);
  }
 
  void renderColumn(ComputedPixelImage img, int row, int col) {
    
  }
  
  void renderDownwards(ComputedPixelImage img, int row, int col) {
    img.setColorAt(col, row, this.color);
    this.down.renderDownwards(img, row + 1, col);
  }
  
  ArrayList<ASeamInfo> rowInfo(ArrayList<ASeamInfo> soFar, APixel start) {
    if (!this.equals(start)) {
      soFar.add(new VerticalSeamInfo(this, this.getEnergy(), null));
      return this.right.rowInfo(soFar, start);
    } else {
      return soFar;
    }
  }
  
  /*
  ArrayList<ASeamInfo> colInfo(ArrayList<ASeamInfo> soFar, APixel start) {
    if (!this.equals(start)) {
      soFar.add(new HorizontalSeamInfo(this, this.getEnergy(), null));
      return this.down.colInfo(soFar, start);
    } else {
      return soFar;
    }
  }
  */
  
  ArrayList<ASeamInfo> rowInfo(ArrayList<ASeamInfo> prevRow, ArrayList<ASeamInfo> currRow) {
    if (currRow.size() == prevRow.size()) {
      return currRow;
    } else {
      ASeamInfo min = new SeamCarvingUtils().getMinWeightAdjacentSeam(prevRow, currRow.size());
      currRow.add(new VerticalSeamInfo(this, min.totalWeight + this.getEnergy(), min));
      return this.right.rowInfo(prevRow, currRow);
    }
  }
  
  /*
  ArrayList<ASeamInfo> colInfo(ArrayList<ASeamInfo> prevCol, ArrayList<ASeamInfo> currCol) {
    if (currCol.size() == prevCol.size()) {
      return currCol;
    } else {
      ASeamInfo min = new SeamCarvingUtils().getMinWeightAdjacentSeam(prevCol, currCol.size());
      currCol.add(new HorizontalSeamInfo(this, min.totalWeight + this.getEnergy(), min));
      return this.down.colInfo(prevCol, currCol);
    }
  }
  */
  
  ArrayList<ASeamInfo> accumulateRows(ArrayList<ASeamInfo> prevRow) {
    ArrayList<ASeamInfo> currRow = this.right.rowInfo(prevRow, new ArrayList<ASeamInfo>());
    ArrayList<ASeamInfo> finalRow = this.down.accumulateRows(currRow);
    return finalRow;
  }
  
  /*
  ArrayList<ASeamInfo> accumulateCols(ArrayList<ASeamInfo> prevCol) {
    ArrayList<ASeamInfo> currCol = this.down.colInfo(prevCol, new ArrayList<ASeamInfo>());
    ArrayList<ASeamInfo> finalCol = this.right.accumulateCols(currCol);
    return finalCol;
  }
  */
}

abstract class Sentinel extends APixel{
  
  Sentinel(APixel left, APixel right, APixel up, APixel down) {
    super(Color.BLACK, left, right, up, down);
  }
  
  Sentinel() {
    super(Color.BLACK);
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
  }
  
  double getEnergy() {
    return 0;
  }
  
  void renderColumn(ComputedPixelImage img, int row, int col) {
    this.down.renderDownwards(img, row, col);
    this.right.renderColumn(img, row, col + 1);
  }
  
  void renderDownwards(ComputedPixelImage img, int row, int col) {
    
  }
  
  void shiftLeft() {
    
  }
  
  /*
  void shiftUp() {
    
  }
  */
}

class CornerSentinel extends Sentinel {
  
  CornerSentinel() {
    super();
  }
  
  void addRowAbove(BorderSentinel rowStart) {
    super.addRowAboveHelp(rowStart);
  }
  
  void addRowAboveHelp(APixel newTop) {
    
  }
  
  void render(ComputedPixelImage img) {
    this.right.renderColumn(img, 0, 0);
  }
  
  void renderColumn(ComputedPixelImage img, int row, int col) {
    
  }
  
  void removeVertically() {
    
  }
  
  /*
  void removeHorizontally() {
    
  }
  */
  
  ArrayList<ASeamInfo> getVerticalSeams() {
    ArrayList<ASeamInfo> initialRow = this.right.rowInfo(new ArrayList<ASeamInfo>(), this);
    ArrayList<ASeamInfo> finalRow = this.down.accumulateRows(initialRow);
    return finalRow;
  }
  
  /*
  ArrayList<ASeamInfo> getHorizontalSeams() {
    ArrayList<ASeamInfo> initialCol = this.down.colInfo(new ArrayList<ASeamInfo>(), this);
    ArrayList<ASeamInfo> finalCol = this.right.accumulateCols(initialCol);
    return finalCol;
  }
  */
  
  ArrayList<ASeamInfo> accumulateRows(ArrayList<ASeamInfo> prevRow) {
    return prevRow;
  }
  
  /*
  ArrayList<ASeamInfo> accumulateCols(ArrayList<ASeamInfo> prevCol) {
    return prevCol;
  }
  */
}

class BorderSentinel extends Sentinel {
  
  BorderSentinel() {
    super();
  }
  
  void removeVertically() {
    this.right.shiftLeftBorder();
    this.left.updateRight(this.right);
  }
  
  /*
  void removeHorizontally() {
    this.down.shiftUpBorder();
    this.up.updateDown(this.down);
  }
  */
  
  void shiftLeftBorder() {
    this.right.shiftLeftBorder();
    this.updateUp(this.left.up);
    this.updateDown(this.left.down);
  }
  
  /*
  void shiftUpBorder() {
    this.down.shiftUpBorder();
    this.updateRight(this.up.right);
    this.updateLeft(this.up.left);
  }
  */
}

class Pixel extends APixel {
  
  Pixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    super(color, left, right, up, down);
  }
  
  Pixel(Color color) {
    super(color);
  }
}

abstract class ASeamInfo {
  APixel currentPixel;
  double totalWeight; //accumulative from start
  ASeamInfo cameFrom; //follow path back to get seam of least energy

  ASeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom) {
    this.currentPixel = currentPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
  }

  ASeamInfo leastWeight(ASeamInfo that) {
    if (this.totalWeight < that.totalWeight) {
      return this;
    } else {
      return that;
    }
  }
  
  abstract void remove();
  
  void paint(Color color) {
    if (this.cameFrom != null) {
      this.cameFrom.paint(color);
    }
    currentPixel.color = color;
  }
}

class VerticalSeamInfo extends ASeamInfo{

  VerticalSeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom) {
    super(currentPixel, totalWeight, cameFrom);
  }
  
  void remove() {
    if (this.cameFrom != null) {
      this.cameFrom.remove();
    }
    currentPixel.removeVertically();
  }
}

/*
class HorizontalSeamInfo extends ASeamInfo{

  HorizontalSeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom) {
    super(currentPixel, totalWeight, cameFrom);
  }
  
  void remove() {
    if (this.cameFrom != null) {
      this.cameFrom.remove();
    }
    currentPixel.removeHorizontally();
  }
}
*/

class SeamCarvingUtils {
  void checkWellFormedPixel(APixel left, APixel right, APixel up, APixel down) {
    if (left.up != up.left
        || right.up != up.right
        || right.down != down.right
        || left.down != down.left) {
      throw new IllegalArgumentException("Pixel not well-formed!");
    }
  }
  
  ASeamInfo getMinWeightSeam(ArrayList<ASeamInfo> seams) {
    ASeamInfo minWeight = seams.get(0);
    for (ASeamInfo s : seams) {
      if (s.totalWeight < minWeight.totalWeight) {
        minWeight = s;
      }
    }
    return minWeight;
  }
  
  ASeamInfo getMinWeightAdjacentSeam(ArrayList<ASeamInfo> adjacentSeams, int index) {
    ArrayList<ASeamInfo> seams = new ArrayList<ASeamInfo>();
    if (index != 0) {
      seams.add(adjacentSeams.get(index - 1));
    }
    seams.add(adjacentSeams.get(index));
    if (index != adjacentSeams.size() - 1) {
      seams.add(adjacentSeams.get(index + 1));
    }
    return this.getMinWeightSeam(seams);
  }
}

class SeamCarver extends World {
  
  Graph graph;
  int width;
  int height;
  int time;
  ASeamInfo seamToRemove;
  boolean paused;
  
  SeamCarver(String filepath) {
    FromFileImage img = new FromFileImage(filepath);
    this.graph = new Graph(img);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    this.time = 0;
    this.paused = false;
  }
  
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(this.width, this.height);
    ComputedPixelImage img = new ComputedPixelImage(this.width, this.height);
    graph.render(img);
    canvas.placeImageXY(img, this.width / 2, this.height / 2);
    return canvas;
  }
  
  public void onTick() {
    if (!paused) {
      if (this.time % 2 == 0) {
        this.seamToRemove = this.graph.getVerticalSeam(); 
        this.seamToRemove.paint(Color.RED);
      } else {
        this.seamToRemove.remove();
        this.width--;
      }
      this.time++;
    }
  }
  
  public void onKeyEvent(String key) {
    if (key.equals(" ")) {
      this.paused = !this.paused;
    }
    if (paused && key.equals("v")) {
      if (this.time % 2 == 0) {
        this.seamToRemove = this.graph.getVerticalSeam();
        this.seamToRemove.paint(Color.RED);
      } else {
        this.seamToRemove.remove();
        this.width--;
      }
      this.time++;
    }
  }
  
  public boolean shouldWorldEnd() {
    return this.width == 1;
  }
}

class ExamplesSeamCarver {
  SeamCarver s = new SeamCarver("src/balloons.jpeg");
  
  void testBang(Tester t) {
    s.bigBang(s.width, s.height, 0.0000001);
  }
}
