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
    ArrayList<SeamInfo> leastEnergyPath = new ArrayList<>();
    ArrayList<SeamInfo> verticalSeams = this.corner.getVerticalSeams(
            new ArrayList<>(), new ArrayList<>());

    SeamInfo minSeam = null;
    double currLowestWeight = Integer.MAX_VALUE;

    for (SeamInfo seam : verticalSeams) {
      if (seam.totalWeight < currLowestWeight) {
        minSeam = seam;
        currLowestWeight = seam.totalWeight;
      }
    }
    return minSeam;
    }
}

abstract class APixel {
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  APixel topLeft;
  APixel topRight;
  APixel botLeft;
  APixel botRight;
  
  APixel(Color color, APixel left, APixel right, APixel up,
      APixel down, APixel topLeft, APixel topRight,
      APixel botLeft, APixel botRight) {
    new PixelUtils().checkWellFormed(left, right, up, down,
        topLeft, topRight, botLeft, botRight);
    this.color = color;
    this.left = left;
    this.right = right;
    this.up = up;
    this.down = down;
    this.topLeft = topLeft;
    this.topRight = topRight;
    this.botLeft = botLeft;
    this.botRight = botRight;
    
    this.left.right = this;
    this.right.left = this;
    this.up.down = this;
    this.down.up = this;
    this.topLeft.botRight = this;
    this.botRight.topLeft = this;
    this.topRight.botLeft = this;
    this.botLeft.topRight = this;
  }
  
  APixel(Color color) {
    this.color = color;
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
    this.topLeft = this;
    this.topRight = this;
    this.botLeft = this;
    this.botRight = this;
  }
  
  double getBrightness() {
    //colors / 3 / 255
    return (this.color.getRed()
        + this.color.getBlue()
        + this.color.getGreen()) / 765.0;
  }
  
  double getEnergy() {
    double horizontalEnergy =
        (this.topLeft.getBrightness()
            + (2 * this.left.getBrightness())
            + this.botLeft.getBrightness()) -
        (this.topRight.getBrightness()
            + (2 * this.right.getBrightness())
            + this.botRight.getBrightness());
    double verticalEnergy =
        (this.topLeft.getBrightness()
            + (2 * this.up.getBrightness())
            + this.topRight.getBrightness()) -
        (this.botLeft.getBrightness()
            + (2 * this.down.getBrightness())
            + this.botRight.getBrightness());
    return Math.sqrt(Math.pow(horizontalEnergy, 2.0) + Math.pow(verticalEnergy, 2.0));
  }
  
  void remove() {
    this.topLeft.botRight = this.botRight;
    this.up.down = this.down;
    this.topRight.botLeft = this.botLeft;
    this.left.right = this.right;
    this.right.left = this.left;
    this.botLeft.topRight = this.topRight;
    this.down.up = this.up;
    this.botRight.topLeft = this.topLeft;
  }
  
  void addLeft(APixel newLeft) {
    this.botLeft.topRight = newLeft;
    newLeft.botLeft = this.botLeft;
    this.left.right = newLeft;
    newLeft.left = this.left;
    this.topLeft.botRight = newLeft;
    newLeft.topLeft = this.topLeft;
    this.botLeft = newLeft.down;
    newLeft.botRight = this.down;
    this.left = newLeft;
    newLeft.right = this;
    this.topLeft = newLeft.up;
    newLeft.topRight = this.up;
  }
  
  void addAbove(APixel newUp) {
    this.topLeft.botRight = newUp;
    newUp.topLeft = this.topLeft;
    this.up.down = newUp;
    newUp.up = this.up;
    this.topRight.botLeft = newUp;
    newUp.topRight = this.topRight;
    this.topLeft = newUp.left;
    newUp.botLeft = this.left;
    this.up = newUp;
    newUp.down = this;
    this.topRight = newUp.right;
    newUp.botRight = this.right;
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
  
  abstract ArrayList<SeamInfo> getVerticalSeams(ArrayList<SeamInfo> prevSeam, ArrayList<SeamInfo> currRowSeam);
}

abstract class Sentinel extends APixel{
  
  Sentinel(APixel left, APixel right, APixel up,
      APixel down, APixel topLeft, APixel topRight,
      APixel botLeft, APixel botRight) {
    super(Color.BLACK, left, right, up, down, topLeft, topRight, botLeft, botRight);
  }
  
  Sentinel() {
    super(Color.BLACK);
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
    this.topLeft = this;
    this.topRight = this;
    this.botLeft = this;
    this.botRight = this;
  }
  
  void renderColumn(ComputedPixelImage img, int row, int col) {
    this.down.renderDownwards(img, row, col);
    this.right.renderColumn(img, row, col + 1);
  }
  
  void renderDownwards(ComputedPixelImage img, int row, int col) {
    
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
  
  //so first call for vertical seam in corner sentinel will go DOWN
  //iterate through, calculating until you reach a sentinel, this will be the start of the row again
  //go down, continue
  //stop when you reach the corner sentinel, this means you went through the whole grid

  public ArrayList<SeamInfo> getVerticalSeams(ArrayList<SeamInfo> prevSeams, ArrayList<SeamInfo> currRowSeam) {
    //grid not passed through
    if (prevSeams.isEmpty()) {
      return this.down.getVerticalSeams(prevSeams, new ArrayList<>());
    } else {
      return prevSeams;
    }
  }
}

class BorderSentinel extends Sentinel {
  
  BorderSentinel() {
    super();
  }
  
  public ArrayList<SeamInfo> getVerticalSeams(ArrayList<SeamInfo> prevSeams, ArrayList<SeamInfo> currRowSeams) {
    //havent processed the pixels in current row yet
    if (currRowSeams.isEmpty()) {
      return this.right.getVerticalSeams(prevSeams, currRowSeams);
      //this case happens after recurring through the row
    } else {
      return this.down.getVerticalSeams(currRowSeams, new ArrayList<>());
    }
  }
}

class Pixel extends APixel {
  
  Pixel(Color color, APixel left, APixel right, APixel up,
      APixel down, APixel topLeft, APixel topRight,
      APixel botLeft, APixel botRight) {
    super(color, left, right, up, down, topLeft, topRight, botLeft, botRight);
  }
  
  Pixel(Color color) {
    super(color);
  }
  
  public ArrayList<SeamInfo> getVerticalSeams(ArrayList<SeamInfo> prevSeam, ArrayList<SeamInfo> currRowSeam) {
    //seems like theres a lot of code which can be abstracted here
    SeamInfo currSeamInfo;
    //first row case
    if (prevSeam.isEmpty()) {
      currSeamInfo = new SeamInfo(
              this,
              this.getEnergy(),
              new SeamInfo(
                      this.up,
                      0.0,
                      //can we have null here?
                      null));
      currRowSeam.add(currSeamInfo);
    } else {
      int currIndex = currRowSeam.size();
      //case one: first pixel, only consider top and top right
      if (currIndex == 0) {
        SeamInfo prevLowest = prevSeam.get(0).lowerOrEqualTotalWeight(prevSeam.get(1));
        currSeamInfo = new SeamInfo(
                this,
                this.getEnergy() + prevLowest.totalWeight,
                prevLowest);
        currRowSeam.add(currSeamInfo);
      } else if (currIndex == prevSeam.size() - 1) { //case two: last pixel, only consider top left and top
        SeamInfo prevLowest = prevSeam.get(currIndex).lowerOrEqualTotalWeight(prevSeam.get(currIndex - 1));
        currSeamInfo = new SeamInfo(
                this,
                this.getEnergy() + prevLowest.totalWeight,
                prevLowest);
        currRowSeam.add(currSeamInfo);
      } else { //case 3: middle element
        //finds lowest total weight of top, topleft, topright
        SeamInfo prevLowest = prevSeam.get(currIndex - 1).lowerOrEqualTotalWeight(prevSeam.get(currIndex))
                .lowerOrEqualTotalWeight(prevSeam.get(currIndex + 1));
        currSeamInfo = new SeamInfo(
                this,
                this.getEnergy() + prevLowest.totalWeight,
                prevLowest);
        currRowSeam.add(currSeamInfo);
      }
    }
    return this.right.getVerticalSeams(prevSeam, currRowSeam);
  }
}

class PixelUtils {
  void checkWellFormed(APixel left, APixel right, APixel up,
      APixel down, APixel topLeft, APixel topRight,
      APixel botLeft, APixel botRight) {
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

  SeamInfo lowerOrEqualTotalWeight(SeamInfo that) {
    return this.totalWeight <= that.totalWeight
            ? this : that;
  }
}

class SeamCarver extends World {
  
  Graph graph;
  int width;
  int height;
  
  SeamCarver(String filepath) {
    FromFileImage img = new FromFileImage(filepath);
    SeamCarverUtils u = new SeamCarverUtils();
    this.graph = u.imageToGraph(img);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
  }
  
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(width, height);
    ComputedPixelImage img = new ComputedPixelImage(width, height);
    graph.render(img);
    canvas.placeImageXY(img, width / 2, height / 2);
    return canvas;
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
    s.bigBang(s.width, s.height, 1);
  }
}
