import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// Represents rectangular grid of APixels
class Graph {
  
  // The corner of the APixel grid
  CornerSentinel corner;
  
  // Creates a graph from the given corner
  Graph(CornerSentinel corner) {
    this.corner = corner;
  }
  
  // Creates a graph from a FromFileImage by creating a Pixel for each pixel in the image
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
  
  // Creates a graph by making a new CornerSentinel
  Graph() {
    this.corner = new CornerSentinel();
  }
  
  // Sets each pixel on the ComputedPixelImage to the color of the associated APixel
  void render(ComputedPixelImage img) {
    this.corner.render(img);
  }
  
  // Gets the graph's vertical seam with the smallest weight
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

// Represents an abstract pixel
abstract class APixel {
  
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  
  // creates an APixel of the given color, with the given neighbors
  APixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    new SeamCarvingUtils().checkWellFormedPixel(left, right, up, down);
    this.color = color;
    this.updateLeft(left);
    this.updateRight(right);
    this.updateUp(up);
    this.updateDown(down);
  }
  
  // creates an APixel of the given color, with each of its neighbors pointing to itself
  APixel(Color color) {
    this.color = color;
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
  }
  
  // Computes the brightness of this pixel
  double getBrightness() {
    //colors / 3 / 255
      return (this.color.getRed()
        + this.color.getBlue()
        + this.color.getGreen()) / 765.0;
  }
  
  // Computes the energy of this pixel
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
  
  // Sets this pixel's up to that, and that pixel's down to this
  void updateUp(APixel that) {
    this.up = that;
    that.down = this;
  }
  
  // Sets this pixel's right to that, and that pixel's left to this
  void updateRight(APixel that) {
    this.right = that;
    that.left = this;
  }
  
  // Sets this pixel's down to that, and that pixel's up to this
  void updateDown(APixel that) {
    this.down = that;
    that.up = this;
  }
  
  // Sets this pixel's left to that, and that pixel's right to this
  void updateLeft(APixel that) {
    this.left = that;
    that.right = this;
  }
  
  // Removes this pixel from the grid by shifting all the rightward neighbors of this pixel left
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
  
  // Shifts this pixel and all of it's rightward neightbors to the left
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
  
  // Most APixels are not borders, and so do nothing
  void shiftLeftBorder() {
    // Intentionally blank
  }
  
  /*
  void shiftUpBorder() {
  
  }
  */
  
  // Adds a pixel to the left of this pixel
  void addLeft(APixel newLeft) {
    this.left.updateRight(newLeft);
    this.updateLeft(newLeft);
  }
  
  // Adds a pixel above this pixel
  void addAbove(APixel newUp) {
    this.up.updateDown(newUp);
    this.updateUp(newUp);
  }
  
  // Adds a row above this pixel
  void addRowAboveHelp(APixel newTop) {
    this.addAbove(newTop);
    this.right.addRowAboveHelp(newTop.right);
  }
  
  // Most pixels are not columns, and so cannot render a full column
  void renderColumn(ComputedPixelImage img, int row, int col) {
    //Intentionally black
  }
  
  // Renders this pixel in its color, and continues to render the pixels below it
  void renderDownwards(ComputedPixelImage img, int row, int col) {
    img.setColorAt(col, row, this.color);
    this.down.renderDownwards(img, row + 1, col);
  }
  
  // Constructs an ArrayList of VerticalSeamInfos with their cameFrom field initialized to null,
  // stopping once the starting pixel is reached
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
  
  // Constructs an ArrayList of VerticalSeamInfo, with the cameFrom field based on the neighboring
  // SeamInfo of minimum weight in the previous row
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
  
  // Computes the ArrayList of VerticalSeamInfo for the bottom row of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
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

// Represents a Sentinel pixel
abstract class ASentinel extends APixel{
  
  // Creates a Sentinel that points to the given neighbors. The color of all Sentinels is black
  ASentinel(APixel left, APixel right, APixel up, APixel down) {
    super(Color.BLACK, left, right, up, down);
  }
  
  // Creates a Sentinel that points to itself. All sentinels are assigned the color black
  ASentinel() {
    super(Color.black);
  }
  
  // All Sentinels have 0 energy
  double getEnergy() {
    return 0;
  }
  
  // Renders all the pixels below this Sentinel, and all the columns to the right of it
  void renderColumn(ComputedPixelImage img, int row, int col) {
    this.down.renderDownwards(img, row, col);
    this.right.renderColumn(img, row, col + 1);
  }
  
  // Sentinels do nothing when rendered
  void renderDownwards(ComputedPixelImage img, int row, int col) {
    // Intentionally blank
  }
  
  // Sentinels cannot be shifted left normally
  void shiftLeft() {
    // Intentionally left blank
  }
  
  /*
  void shiftUp() {
    
  }
  */
}

// Represents a Sentinel at the corner of the grid
class CornerSentinel extends ASentinel {
  
  // Creates a CornerSentinel with the given neighbors
  CornerSentinel(APixel left, APixel right, APixel up, APixel down) {
    super(left, right, up, down);
  }
  
  // Creates a CornerSentinel that points to itself
  CornerSentinel() {
    super();
  }
  
  // Adds a row above this corner that starts with the given BorderSentinel
  void addRowAbove(BorderSentinel rowStart) {
    super.addRowAboveHelp(rowStart);
  }
  
  // Once the addRowAboveHelp is called on the CornerSentinel, all pixels have been added
  void addRowAboveHelp(APixel newTop) {
    // Intentionally blank
  }
  
  // Renders every columns of Pixels to the right of this Corner, where the top left pixel is (0,0)
  void render(ComputedPixelImage img) {
    this.right.renderColumn(img, 0, 0);
  }
  
  // Once the addRowAboveHelp is called on the CornerSentinel, all pixels have been rendered
  void renderColumn(ComputedPixelImage img, int row, int col) {
    // Intentionally blank
  }
  
  // CornerSentinels cannot be removed
  void removeVertically() {
    // Intentionally blank
  }
  
  /*
  void removeHorizontally() {
    
  }
  */
  
  // Computes the ArrayList of VerticalSeamInfo for the bottom row of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
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
  
  // Once the accumulateRows method is called on the CornerSentinel, all SeamInfos have been
  // computed, so return the last ArrayList to be created
  ArrayList<ASeamInfo> accumulateRows(ArrayList<ASeamInfo> prevRow) {
    return prevRow;
  }
  
  /*
  ArrayList<ASeamInfo> accumulateCols(ArrayList<ASeamInfo> prevCol) {
    return prevCol;
  }
  */
}

// Represents a Sentinel on the edge of the grid
class BorderSentinel extends ASentinel {
  
  // Creates a BorderSentinel with the given neighbors
  BorderSentinel(APixel left, APixel right, APixel up, APixel down) {
    super(left, right, up, down);
  }
  
  // Creates a BorderSentinel that points to itself
  BorderSentinel() {
    super();
  }
  
  // If a border Sentinel is being vertically removed, shift the borders to its right leftwards
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
  
  // Shifts this border and all the borders to the right leftwards
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

// Represents a Pixel on the grid
class Pixel extends APixel {
  
  // Creates a pixel with the given color and neighbors
  Pixel(Color color, APixel left, APixel right, APixel up, APixel down) {
    super(color, left, right, up, down);
  }
  
  // Creates a pixel with the given color, that points to itself
  Pixel(Color color) {
    super(color);
  }
}

// Represents a SeamInfo
abstract class ASeamInfo {
  
  APixel currentPixel;
  double totalWeight; //accumulative from start
  ASeamInfo cameFrom; //follow path back to get every pixel in the seam
  
  // Creates an ASeamInfo with the given pixel, weight, and previous SeamInfo
  ASeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom) {
    this.currentPixel = currentPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
  }
  
  // Returns the seam info with the least weight between this SeamInfo and the provided one.
  // If the weights are the same, the provided SeamInfo is returned
  ASeamInfo leastWeight(ASeamInfo that) {
    if (this.totalWeight < that.totalWeight) {
      return this;
    } else {
      return that;
    }
  }
  
  // Removes the pixel of this SeamInfo and all the pixels of the previous SeamInfos from the grid
  abstract void remove();
  
  // Changes the color of this SeamInfo's pixel and the colors of all the previous
  // SeamInfo's pixels to the given color
  void paint(Color color) {
    if (this.cameFrom != null) {
      this.cameFrom.paint(color);
    }
    currentPixel.color = color;
  }
}

// Represents a SeamInfo from the top to the bottom of an image
class VerticalSeamInfo extends ASeamInfo{
  
  // Creates a VerticalSeamInfo with the given pixel, weight, and cameFrom
  VerticalSeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom) {
    super(currentPixel, totalWeight, cameFrom);
  }
  
  // Vertically removes this pixel and the pixels of all the previous seamInfos
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

// Utility methods for this file
class SeamCarvingUtils {
  
  // Checks whether a pixel's neighbors are well formed by making sure that the
  // given neighbors all make sense
  void checkWellFormedPixel(APixel left, APixel right, APixel up, APixel down) {
    if (left.up != up.left
        || right.up != up.right
        || right.down != down.right
        || left.down != down.left) {
      throw new IllegalArgumentException("Pixel not well-formed!");
    }
  }
  
  // Returns the seam of lowest weight in an ArrayList of seams
  ASeamInfo getMinWeightSeam(ArrayList<ASeamInfo> seams) {
    ASeamInfo minWeight = seams.get(0);
    for (ASeamInfo s : seams) {
      minWeight = s.leastWeight(minWeight);
    }
    return minWeight;
  }
  
  // Returns the seam of least weight in an ArrayList of Seams that is adjacent to the given index
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

// Represents a SeamCarver
class SeamCarver extends World {
  
  Graph graph;
  int width;
  int height;
  int time;
  ASeamInfo seamToRemove;
  boolean paused;
  
  // Creates a SeamCarver with the image at the given filepath
  SeamCarver(String filepath) {
    FromFileImage img = new FromFileImage(filepath);
    this.graph = new Graph(img);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    this.time = 0;
    this.paused = false;
  }
  
  // Renders the image on a WorldScene
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(this.width, this.height);
    ComputedPixelImage img = new ComputedPixelImage(this.width, this.height);
    graph.render(img);
    canvas.placeImageXY(img, this.width / 2, this.height / 2);
    return canvas;
  }
  
  // Every tick, alternate between finding a seam to remove and coloring it red, and removing it
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
  
  // Space pauses/unpauses the automatic seam removal.
  // While paused, 'v' can be pressed to move one tick forward
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
  
  // The world ends once the width of the image is 1
  public boolean shouldWorldEnd() {
    return this.width == 1;
  }
}

class ExamplesSeamCarver {
  SeamCarver s = new SeamCarver("src/balloons.jpeg");
  void testBang(Tester t) {
    s.bigBang(s.width, s.height, 0.0000001);
  }

  boolean testAPixel(Tester t) {
    //3x3 example
    CornerSentinel corner = new CornerSentinel();
    //bottom up, left to right
    APixel border1 = new BorderSentinel();
    APixel border2 = new BorderSentinel();
    APixel border3 = new BorderSentinel();
    APixel border4 = new BorderSentinel();
    APixel border5 = new BorderSentinel();
    APixel border6 = new BorderSentinel();
    APixel topLeft = new Pixel(new Color(100, 100, 100));
    APixel topMid = new Pixel(new Color(20, 20, 10));
    APixel topRight = new Pixel(new Color(15, 25, 75));
    APixel midLeft = new Pixel(new Color(1, 2, 3));
    APixel mid = new Pixel(new Color(4, 5, 6));
    APixel midRight = new Pixel(new Color(7, 8, 9));
    APixel botLeft = new Pixel(new Color(3, 6, 9));
    APixel botMid = new Pixel(new Color(44, 22, 11));
    APixel botRight = new Pixel(new Color(12, 24, 255));

    //new row
    APixel newRow1 = new Pixel(Color.RED);
    APixel newRow2 = new Pixel(Color.GREEN);
    APixel newRow3 = new Pixel(Color.ORANGE);

    newRow1.updateRight(newRow2);
    newRow2.updateRight(newRow3);

    border1.updateRight(botLeft);
    botLeft.updateRight(botMid);
    botMid.updateRight(botRight);
    botRight.updateRight(border1);
    border2.updateRight(midLeft);
    midLeft.updateRight(mid);
    mid.updateRight(midRight);
    midRight.updateRight(border2);
    border3.updateRight(topLeft);
    topLeft.updateRight(topMid);
    topMid.updateRight(topRight);
    topRight.updateRight(border3);
    corner.updateRight(border4);
    border4.updateRight(border5);
    border5.updateRight(border6);
    border6.updateRight(corner);

    corner.updateDown(border3);
    border3.updateDown(border2);
    border2.updateDown(border1);
    border1.updateDown(corner);
    border4.updateDown(topLeft);
    topLeft.updateDown(midLeft);
    midLeft.updateDown(botLeft);
    botLeft.updateDown(border4);
    border5.updateDown(topMid);
    topMid.updateDown(mid);
    mid.updateDown(botMid);
    botMid.updateDown(border5);
    border6.updateDown(topRight);
    topRight.updateDown(midRight);
    midRight.updateDown(botRight);
    botRight.updateDown(border6);

    boolean testConstructorException = t.checkConstructorException(
            new IllegalArgumentException("Pixel not well-formed!"),
            "Pixel",
            Color.RED, border1, border2, border3, border4);

    boolean testUpdateMutation = t.checkExpect(border1.up,
            border2)
            && t.checkExpect(border2.down,
            border1)
            && t.checkExpect(border1.right,
            botLeft)
            && t.checkExpect(botLeft.left,
            border1)
            && t.checkExpect(border1.left,
            botRight)
            && t.checkExpect(botRight.right,
            border1)
            && t.checkExpect(border1.down,
            corner)
            && t.checkExpect(corner.up,
            border1)
            && t.checkExpect(mid.left,
            midLeft)
            && t.checkExpect(mid.up,
            topMid)
            && t.checkExpect(mid.right,
            midRight)
            && t.checkExpect(mid.down,
            botMid);

    boolean testBrightness = t.checkInexact(topLeft.getBrightness(),
            0.392156, .00001)
            && t.checkInexact(topMid.getBrightness(),
            .065359, .00001)
            && t.checkInexact(topRight.getBrightness(),
            .150326, .00001)
            && t.checkInexact(midLeft.getBrightness(),
            .0078431, .00001)
            && t.checkInexact(mid.getBrightness(),
            .0196078, .00001)
            && t.checkInexact(midRight.getBrightness(),
            .0313725, .00001)
            && t.checkInexact(botLeft.getBrightness(),
            .0235294, .00001)
            && t.checkInexact(botMid.getBrightness(),
            .100653, .00001)
            && t.checkInexact(botRight.getBrightness(),
            .38039, .00001)
            && t.checkInexact(border1.getBrightness(),
            0.0, .00001);

    boolean testEnergy = t.checkInexact(topLeft.getEnergy(),
            .151142, .00001)
            && t.checkInexact(topMid.getEnergy(),
            .462530, .00001)
            && t.checkInexact(topRight.getEnergy(),
            .171406, .00001)
            && t.checkInexact(midLeft.getEnergy(),
            .765286, .00001)
            && t.checkInexact(mid.getEnergy(),
            .339265, .00001)
            && t.checkInexact(midRight.getEnergy(),
            .536250, .00001)
            && t.checkInexact(botLeft.getEnergy(),
            .221471, .00001)
            && t.checkInexact(botMid.getEnergy(),
            .738755, .00001)
            && t.checkInexact(botRight.getEnergy(),
            .235765, .00001);

    ArrayList<ASeamInfo> firstRowInfo = topLeft.rowInfo(
            new ArrayList<>(), border3);

    //using getEnergy to avoid inexact values, already tested getEnergy above
    boolean testFirstRow = t.checkExpect(firstRowInfo.get(0),
            new VerticalSeamInfo(topLeft, topLeft.getEnergy(), null))
            && t.checkExpect(firstRowInfo.get(1),
            new VerticalSeamInfo(topMid, topMid.getEnergy(), null))
            && t.checkExpect(firstRowInfo.get(firstRowInfo.size() - 1),
            new VerticalSeamInfo(topRight, topRight.getEnergy(), null));

    ArrayList<ASeamInfo> nextRowInfo = midLeft.rowInfo(firstRowInfo, new ArrayList<>());

    //can't test inexact numbers
    boolean testNextRowInfo = t.checkExpect(nextRowInfo.get(0).cameFrom,
            firstRowInfo.get(0))
            && t.checkExpect(nextRowInfo.get(1).cameFrom,
            firstRowInfo.get(0))
            //lol
            && t.checkExpect(t.checkExpect(nextRowInfo.get(2).cameFrom,
                    firstRowInfo.get(2)), true);

    ArrayList<ASeamInfo> finalRow = border2.accumulateRows(firstRowInfo);

    boolean testAccumulateRows = t.checkExpect(finalRow.get(0).cameFrom,
            nextRowInfo.get(1))
            && t.checkExpect(finalRow.get(1).cameFrom,
            nextRowInfo.get(1))
            && t.checkExpect(finalRow.get(2).cameFrom,
            nextRowInfo.get(1));

    botLeft.removeVertically();

    boolean testRemoveVertically = t.checkExpect(border1.right, botMid)
            && t.checkExpect(botMid.up, midLeft)
            && t.checkExpect(botMid.down, border4);

    botMid.addLeft(botLeft);

    boolean testAddLeft = t.checkExpect(botMid.left, botLeft)
            && t.checkExpect(border1.right, botLeft);

    topMid.shiftLeft();

    boolean testShift = t.checkExpect(topMid.up, border4)
            && t.checkExpect(topMid.down, midLeft)
            && t.checkExpect(midLeft.up, topMid)
            && t.checkExpect(border4.down, topMid)
            && t.checkExpect(topLeft.right, topMid);

    topLeft.addAbove(newRow1);

    boolean testAddAbove = t.checkExpect(topLeft.up, newRow1)
            && t.checkExpect(newRow1.down, topLeft);

    return testConstructorException && testBrightness && testEnergy
            && testUpdateMutation && testFirstRow && testNextRowInfo
            && testAccumulateRows && testRemoveVertically && testAddAbove
            && testAddLeft && testShift;
  }

  boolean testMoreMutationAndSeamInfo(Tester t) {
    //3x3 example
    CornerSentinel corner = new CornerSentinel();
    //bottom up, left to right
    APixel border1 = new BorderSentinel();
    APixel border2 = new BorderSentinel();
    APixel border3 = new BorderSentinel();
    APixel border4 = new BorderSentinel();
    APixel border5 = new BorderSentinel();
    APixel border6 = new BorderSentinel();
    APixel topLeft = new Pixel(new Color(100, 100, 100));
    APixel topMid = new Pixel(new Color(20, 20, 10));
    APixel topRight = new Pixel(new Color(15, 25, 75));
    APixel midLeft = new Pixel(new Color(1, 2, 3));
    APixel mid = new Pixel(new Color(4, 5, 6));
    APixel midRight = new Pixel(new Color(7, 8, 9));
    APixel botLeft = new Pixel(new Color(3, 6, 9));
    APixel botMid = new Pixel(new Color(44, 22, 11));
    APixel botRight = new Pixel(new Color(12, 24, 255));

    //new row
    APixel newRow1 = new Pixel(Color.RED);
    APixel newRow2 = new Pixel(Color.GREEN);
    APixel newRow3 = new Pixel(Color.ORANGE);

    newRow1.updateRight(newRow2);
    newRow2.updateRight(newRow3);

    border1.updateRight(botLeft);
    botLeft.updateRight(botMid);
    botMid.updateRight(botRight);
    botRight.updateRight(border1);
    border2.updateRight(midLeft);
    midLeft.updateRight(mid);
    mid.updateRight(midRight);
    midRight.updateRight(border2);
    border3.updateRight(topLeft);
    topLeft.updateRight(topMid);
    topMid.updateRight(topRight);
    topRight.updateRight(border3);
    corner.updateRight(border4);
    border4.updateRight(border5);
    border5.updateRight(border6);
    border6.updateRight(corner);

    corner.updateDown(border3);
    border3.updateDown(border2);
    border2.updateDown(border1);
    border1.updateDown(corner);
    border4.updateDown(topLeft);
    topLeft.updateDown(midLeft);
    midLeft.updateDown(botLeft);
    botLeft.updateDown(border4);
    border5.updateDown(topMid);
    topMid.updateDown(mid);
    mid.updateDown(botMid);
    botMid.updateDown(border5);
    border6.updateDown(topRight);
    topRight.updateDown(midRight);
    midRight.updateDown(botRight);
    botRight.updateDown(border6);

    ArrayList<ASeamInfo> finalRow = corner.getVerticalSeams();

    boolean testGetVertSeam = t.checkInexact(finalRow.get(0).totalWeight,
            .711879, .00001)
            && t.checkInexact(finalRow.get(1).totalWeight,
            1.22916, .00001)
            && t.checkInexact(finalRow.get(2).totalWeight,
            .726174, .00001);

    boolean testLeastWeight = t.checkExpect(finalRow.get(0).leastWeight(finalRow.get(1)),
            finalRow.get(0))
            && t.checkExpect(finalRow.get(0).leastWeight(finalRow.get(2)),
            finalRow.get(0));

    border3.shiftLeftBorder();

    boolean testShiftBorder = t.checkExpect(border3.up, border6)
            && t.checkExpect(border3.down, midRight)
            && t.checkExpect(border6.down, border3)
            && t.checkExpect(midRight.up, border3);

    finalRow.get(0).paint(Color.RED);
    finalRow.get(0).cameFrom.cameFrom.paint(Color.RED);

    boolean testPaint = t.checkExpect(botLeft.color,
            Color.RED)
            && t.checkExpect(mid.color,
            Color.RED)
            && t.checkExpect(topLeft.color,
            Color.RED)
            && t.checkExpect(border4.color,
            Color.RED);

    finalRow.get(0).remove();

    boolean testRemove = t.checkExpect(border1.right, botMid)
            && t.checkExpect(midLeft.right, midRight)
            && t.checkExpect(border3.right, topMid)
            && t.checkExpect(corner.right, border5);

    return testGetVertSeam && testPaint && testRemove
            && testShiftBorder && testLeastWeight;
  }

  boolean testGraph(Tester t) {
    FromFileImage img = new FromFileImage("src/sadmarks.png");
    Graph g = new Graph(img);

    return t.checkExpect(g.corner.right.color,
            Color.BLACK)
            && t.checkExpect(g.corner.down.right.color,
            Color.WHITE)
            && t.checkInexact(g.getVerticalSeam().totalWeight,
            3.55688, .00001);
  }
}
