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
  
  Graph() {
    this.corner = new CornerSentinel();
  }
  
  void render(ComputedPixelImage img) {
    this.corner.render(img);
  }
  
  SeamInfo getVerticalSeam() {
    ArrayList<SeamInfo> seams = this.corner.getVerticalSeams();
    SeamInfo minWeight = seams.get(0);
    for (SeamInfo s : seams) {
      if (s.totalWeight < minWeight.totalWeight) {
        minWeight = s;
      }
    }
    return minWeight;
  }
}

abstract class APixel {
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  
  APixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    new PixelUtils().checkWellFormed(left, right, up, down);
    this.color = color;
    this.left = left;
    this.right = right;
    this.up = up;
    this.down = down;
    
    this.left.right = this;
    this.right.left = this;
    this.up.down = this;
    this.down.up = this;
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
 //   System.out.println(this.color + "" + this.color.getRed());
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
  
  void removeVertically() {
    this.up.down = this.right;
    this.left.right = this.right;
    this.down.up = this.right;
    
    this.right.shiftLeft();
    this.right.left =this.left;
  }
  
  void shiftLeft() {
    this.right.shiftLeft();
    this.up = this.left.up;
    this.left.up.down = this;
    this.down = this.left.down;
    this.left.down.up = this;
  }
  
  void shiftLeftBorder() {
    
  }
  
  void addLeft(APixel newLeft) {
    this.left.right = newLeft;
    newLeft.left = this.left;
    this.left = newLeft;
    newLeft.right = this;
  }
  
  void addAbove(APixel newUp) {
    this.up.down = newUp;
    newUp.up = this.up;
    this.up = newUp;
    newUp.down = this;
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
  
  ArrayList<SeamInfo> rowInfo(ArrayList<SeamInfo> soFar, APixel start) {
    if (!this.equals(start)) {
      soFar.add(new SeamInfo(this, this.getEnergy(), null));
      return this.right.rowInfo(soFar, start);
    } else {
      return soFar;
    }
  }
  
  ArrayList<SeamInfo> rowInfo(ArrayList<SeamInfo> prevRow, ArrayList<SeamInfo> currRow, int currCol) {
    if (currCol == prevRow.size()) {
      return currRow;
    } else {
      SeamInfo prevTopLeft;
      SeamInfo prevTop = prevRow.get(currCol);
      SeamInfo prevTopRight;
      SeamInfo min;
      if (currCol == 0) {
        prevTopRight = prevRow.get(currCol + 1);
        min = prevTopRight.leastWeight(prevTop);
      } else if (currCol == prevRow.size() - 1) {
        prevTopLeft = prevRow.get(currCol - 1);
        min = prevTop.leastWeight(prevTopLeft);
      } else {
        prevTopRight = prevRow.get(currCol + 1);
        prevTopLeft = prevRow.get(currCol - 1);
        min = prevTopRight.leastWeight(prevTop).leastWeight(prevTopLeft);
      }
      currRow.add(new SeamInfo(this, min.totalWeight + this.getEnergy(), min));
      return this.right.rowInfo(prevRow, currRow, currCol + 1);
    }
  }
  
  ArrayList<SeamInfo> accumulateSeams(ArrayList<SeamInfo> prevRow) {
    ArrayList<SeamInfo> currRow = this.right.rowInfo(prevRow, new ArrayList<SeamInfo>(), 0);
    ArrayList<SeamInfo> finalRow = this.down.accumulateSeams(currRow);
    return finalRow;
  }
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
  
  void remove() {
    
  }
  
  void removeVertically() {
    
  }
  
  ArrayList<SeamInfo> getVerticalSeams() {
    ArrayList<SeamInfo> initialRow = this.right.rowInfo(new ArrayList<SeamInfo>(), this);
    ArrayList<SeamInfo> finalRow = this.down.accumulateSeams(initialRow);
    return finalRow;
  }
  
  ArrayList<SeamInfo> accumulateSeams(ArrayList<SeamInfo> prevRow) {
    return prevRow;
  }
}

class BorderSentinel extends Sentinel {
  
  BorderSentinel() {
    super();
  }
  
  void removeVertically() {
    this.up.down = this.right;
    this.left.right = this.right;
    this.down.up = this.right;
    
    this.right.shiftLeftBorder();
    this.right.left =this.left;
  }
  
  void shiftLeftBorder() {
    this.right.shiftLeftBorder();
    this.up = this.left.up;
    this.left.up.down = this;
    this.down = this.left.down;
    this.left.down.up = this;
  }
}

class Pixel extends APixel {
  
  Pixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    super(color, left, right, up, down);
  }
  
  Pixel(Color color) {
    super(color);
  }
}

class PixelUtils {
  void checkWellFormed(APixel left, APixel right, APixel up, APixel down) {
    if (left.up != up.left
        || right.up != up.right
        || right.down != down.right
        || left.down != down.left) {
      throw new IllegalArgumentException("Pixel not well-formed!");
    }
  } 
}

class SeamInfo {
  APixel currentPixel;
  double totalWeight; //accumulative from start
  SeamInfo cameFrom; //follow path back to get seam of least energy

  SeamInfo(APixel currentPixel, double totalWeight, SeamInfo cameFrom) {
    this.currentPixel = currentPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
  }

  SeamInfo leastWeight(SeamInfo that) {
    if (this.totalWeight < that.totalWeight) {
      return this;
    } else {
      return that;
    }
  }
  
  void remove() {
    if (this.cameFrom != null) {
      this.cameFrom.remove();
    }
    currentPixel.removeVertically();
  }
  
  void paint(Color color) {
    if (this.cameFrom != null) {
      this.cameFrom.paint(color);
    }
    currentPixel.color = color;
  }
}

class SeamCarver extends World {
  
  Graph graph;
  int width;
  int height;
  int time;
  SeamInfo seam;
  
  SeamCarver(String filepath) {
    FromFileImage img = new FromFileImage(filepath);
    SeamCarverUtils u = new SeamCarverUtils();
    this.graph = u.imageToGraph(img);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    this.time = 0;
    this.seam = this.graph.getVerticalSeam();
  }
  
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(width, height);
    ComputedPixelImage img = new ComputedPixelImage(width, height);
    graph.render(img);
    canvas.placeImageXY(img, width / 2, height / 2);
    return canvas;
  }
  
  public void onTick() {
    if (this.time % 2 == 0) {
      this.seam = this.graph.getVerticalSeam();
      this.seam.paint(Color.RED);
    } else {
      this.seam.remove();
    }
    this.time++;
  }
  
  public void onKeyEvent(String key) {
    if (key.equals("p")) {
      this.seam = this.graph.getVerticalSeam();
      this.seam.paint(Color.RED);
    } else if (key.equals("r")) {
      this.seam.remove();
    }
  }
}

class SeamCarverUtils {
  Graph imageToGraph(FromFileImage img) {
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
    
    return new Graph(origin);
  }
}

class ExamplesSeamCarver {
  SeamCarver s = new SeamCarver("src/balloons.jpeg");
  
  void testBang(Tester t) {
    s.bigBang(s.width, s.height, 0.1);
  }
  
//  void testSeans(Tester t) {
//    s.graph.getVerticalSeam().remove();
//    t.checkExpect(s.graph, 1);
//  }
  
}
