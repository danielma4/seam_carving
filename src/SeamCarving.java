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
    
    // Creates a row of BorderSentinels as next to the Corner sentinels,
    // with one for each pixel in the width of the image
    for (int i = 0; i < width; i++) {
      origin.addLeft(new BorderSentinel());
    }
    // Iterates through each row of the image and creates the appropriate border and pixels
    for (int row = 0; row < height; row++) {
      BorderSentinel border = new BorderSentinel();
      // Iterates through each pixel in this row of img and creates the correctly colored pixel
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
  
  // Renders the grid of pixels in the appropriate mode onto the provided ComputedPixelImage
  // "Color" renders each pixel in its color
  // "Energy" renders each pixel in grayscale, as its energy divided by the maximum possible energy
  // "Grayscale" renders each pixel in grayscale
  //"Vertical Weight" renders each pixel in grayscale as the minimum weight for a vertical seam to
  // reach that pixel divided by the largest weight of any vertical seam
  // "Horizontal Weight" renders each pixel in grayscale as the minimum weight for a horizontal
  // seam to reach that pixel divided by the largest weight of any horizontal seam
  // Any other modes throw an error
  void render(ComputedPixelImage img, String mode) {
    if (mode.equals("Color") || mode.equals("Energy") || mode.equals("Grayscale")) {
      this.corner.render(img, mode);
    } else {
      ArrayList<ArrayList<ASeamInfo>> seams;
      if (mode.equals("Vertical Weight")) {
        seams = this.corner.getAllVerticalSeams();
      } else if (mode.equals("Horizontal Weight")) {
        seams = this.corner.getAllHorizontalSeams();
      } else {
        throw new IllegalArgumentException("Invalid mode");
      }
      seams.remove(0);
      ASeamInfo maxWeightSeam = seams.get(0).get(0);
      for (ArrayList<ASeamInfo> arr : seams) {
        maxWeightSeam = maxWeightSeam.greatestWeight(new SeamCarvingUtils().getMaxWeightSeam(arr));
      }
      for (int outter = 0; outter < seams.size(); outter++) {
        for (int inner = 0; inner < seams.get(outter).size(); inner++) {
          ASeamInfo seam = seams.get(outter).get(inner);
          seam.render(img, maxWeightSeam.totalWeight, outter);
        }
      }
    }
  }
  
  // Gets the graph's vertical seam with the smallest weight
  ASeamInfo getVerticalSeam() {
    ArrayList<ASeamInfo> seams = this.corner.getVerticalSeams();
    return new SeamCarvingUtils().getMinWeightSeam(seams);
  }
  
  // Getsn the graph's vertical seam with the smallest weight
  ASeamInfo getHorizontalSeam() {
    ArrayList<ASeamInfo> seams = this.corner.getHorizontalSeams();
    return new SeamCarvingUtils().getMinWeightSeam(seams);
  }
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
            + this.down.right.getBrightness());
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
  
  // Vertically removes this pixel from the grid
  abstract void removeVertically();
  
  // Vertically inserts this pixel into the grid
  abstract void insertVertically();
  
  // Horizontally removes this pixel from the grid
  abstract void removeHorizontally();
  
  // Horizontally inserts this pixel into the grid
  abstract void insertHorizontally();
  
  // Shifts this pixel and all of it's rightward neighbors to the left
  abstract void shiftLeft();
  
  // Shifts this pixel and all of it's rightward neighbors to the right
  abstract void shiftRight();
  
  // Shifts this pixel and all of it's downward neighbors up
  abstract void shiftUp();
  
  // Shifts this pixel and all of it's downward neighbors down
  abstract void shiftDown();
  
  // Most APixels are not borders, and so do nothing
  void shiftLeftBorder() {
    // Intentionally blank
  }
  
  // Most APixels are not borders, and so do nothing
  void shiftRightBorder() {
    // Intentionally blank
  }
  
  // Most APixels are not borders, and so do nothing
  void shiftUpBorder() {
    // Intentionally blank
  }
  
  // Most APixels are not borders, and so do nothing
  void shiftDownBorder() {
    // Intentionally blank
  }
  
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
  void renderColumn(ComputedPixelImage img, String mode, int row, int col) {
    //Intentionally black
  }
  
  // Renders this pixel and the pixels below it in the appropriate mode
  // onto the provided ComputedPixelImage
  // "Color" renders each pixel in its color
  // "Energy" renders each pixel in grayscale, as its energy divided by the maximum possible energy
  // "Grayscale" renders each pixel in grayscale
  void renderDownwards(ComputedPixelImage img, String mode, int row, int col) {
    if (mode.equals("Color")) {
      img.setColorAt(col, row, this.color);
    } else if (mode.equals("Energy")) {
      double maxEnergy = 2 * Math.sqrt(5);
      int grayscaleValue = (int) (255 * (this.getEnergy() / maxEnergy));
      img.setColorAt(col, row, new Color(grayscaleValue, grayscaleValue, grayscaleValue));
    } else if (mode.equals("Grayscale")) {
      double brightness = this.getBrightness();
      int grayscaleValue = (int) (255 * brightness);
      img.setColorAt(col, row, new Color(grayscaleValue, grayscaleValue, grayscaleValue));
    } else {
      throw new IllegalArgumentException("Invalid mode");
    }
    this.down.renderDownwards(img, mode, row + 1, col);
  }
  
  // Constructs an ArrayList of VerticalSeamInfos with their cameFrom field initialized to null,
  // stopping once the starting pixel is reached
  ArrayList<ASeamInfo> rowInfo(ArrayList<ASeamInfo> soFar, APixel start) {
    if (!this.equals(start)) {
      soFar.add(new VerticalSeamInfo(this, this.getEnergy(), null, soFar.size()));
      return this.right.rowInfo(soFar, start);
    } else {
      return soFar;
    }
  }
  
  // Constructs an ArrayList of VerticalSeamInfo, with the cameFrom field based on the neighboring
  // SeamInfo of minimum weight in the previous row
  ArrayList<ASeamInfo> rowInfo(ArrayList<ASeamInfo> prevRow, ArrayList<ASeamInfo> currRow) {
    if (currRow.size() == prevRow.size()) {
      return currRow;
    } else {
      ASeamInfo min = new SeamCarvingUtils().getMinWeightAdjacentSeam(prevRow, currRow.size());
      currRow.add(new VerticalSeamInfo(this, min.totalWeight + this.getEnergy(), min, 
          currRow.size()));
      return this.right.rowInfo(prevRow, currRow);
    }
  }
  
  // Constructs an ArrayList of HorizontalSeamInfo with their cameFrom field initialized to null,
  // stopping once the starting pixel is reached
  ArrayList<ASeamInfo> colInfo(ArrayList<ASeamInfo> soFar, APixel start) {
    if (!this.equals(start)) {
      soFar.add(new HorizontalSeamInfo(this, this.getEnergy(), null, soFar.size()));
      return this.down.colInfo(soFar, start);
    } else {
      return soFar;
    }
  }
  
  // Constructs an ArrayList of HorizontalSeamInfo, with the cameFrom field based on the neighboring
  // SeamInfo of minimum weight in the previous col
  ArrayList<ASeamInfo> colInfo(ArrayList<ASeamInfo> prevCol, ArrayList<ASeamInfo> currCol) {
    if (currCol.size() == prevCol.size()) {
      return currCol;
    } else {
      ASeamInfo min = new SeamCarvingUtils().getMinWeightAdjacentSeam(prevCol, currCol.size());
      currCol.add(new HorizontalSeamInfo(this, min.totalWeight + this.getEnergy(), min,
          currCol.size()));
      return this.down.colInfo(prevCol, currCol);
    }
  }
  
  // Computes the ArrayList of VerticalSeamInfo for the bottom row of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
  ArrayList<ASeamInfo> accumulateRows(ArrayList<ASeamInfo> prevRow) {
    ArrayList<ASeamInfo> currRow = this.right.rowInfo(prevRow, new ArrayList<ASeamInfo>());
    ArrayList<ASeamInfo> finalRow = this.down.accumulateRows(currRow);
    return finalRow;
  }
  
  // Creates a 2D ArrayList of VerticalSeamInfos of minimum weight where each SeamInfo's position
  // in the arrayList corresponds to the position of its currentPixel in the grid
  ArrayList<ArrayList<ASeamInfo>> accumulateAllRows(ArrayList<ArrayList<ASeamInfo>> soFar) {
    ArrayList<ASeamInfo> prevRow = soFar.get(soFar.size() - 1);
    ArrayList<ASeamInfo> currRow = this.right.rowInfo(prevRow, new ArrayList<ASeamInfo>());
    soFar.add(currRow);
    ArrayList<ArrayList<ASeamInfo>> allSeams = this.down.accumulateAllRows(soFar);
    return allSeams;
  }
  
  // Computes the ArrayList of HorizontalSeamInfo for the rightmost col of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
  ArrayList<ASeamInfo> accumulateCols(ArrayList<ASeamInfo> prevCol) {
    ArrayList<ASeamInfo> currCol = this.down.colInfo(prevCol, new ArrayList<ASeamInfo>());
    ArrayList<ASeamInfo> finalCol = this.right.accumulateCols(currCol);
    return finalCol;
  }
  
  // Creates a 2D ArrayList of HorizontalSeamInfos of minimum weight where each SeamInfo's position
  // in the arrayList corresponds to the position of its currentPixel in the grid
  ArrayList<ArrayList<ASeamInfo>> accumulateAllCols(ArrayList<ArrayList<ASeamInfo>> soFar) {
    ArrayList<ASeamInfo> prevCol = soFar.get(soFar.size() - 1);
    ArrayList<ASeamInfo> currCol = this.down.colInfo(prevCol, new ArrayList<ASeamInfo>());
    soFar.add(currCol);
    ArrayList<ArrayList<ASeamInfo>> allSeams = this.right.accumulateAllCols(soFar);
    return allSeams;
  }
}

// Represents a pixel that is on the edge of a grid of pixels, that is not included when rendering
abstract class ASentinel extends APixel {
  
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
  void renderColumn(ComputedPixelImage img, String mode, int row, int col) {
    this.down.renderDownwards(img, mode, row, col);
    this.right.renderColumn(img, mode, row, col + 1);
  }
  
  // Sentinels do nothing when rendered
  void renderDownwards(ComputedPixelImage img, String mode, int row, int col) {
    // Intentionally blank
  }
  
  // Sentinels cannot be shifted left normally
  void shiftLeft() {
    // Intentionally left blank
  }
  
  // Sentinels do not shift right, but do update the last pixel to the left
  void shiftRight() {
    this.left.up = this.up.left;
    this.left.down = this.down.left;
  }
  
  // Sentinels cannot be shifted up normally
  void shiftUp() {
    // Intentionally left blank
  }
  
  // Sentinels do not shift down, but do update the last pixel above
  void shiftDown() {
    this.up.right = this.right.up;
    this.up.left = this.left.up;
  }
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
  void render(ComputedPixelImage img, String mode) {
    this.right.renderColumn(img, mode, 0, 0);
  }
  
  // Once the addRowAboveHelp is called on the CornerSentinel, all pixels have been rendered
  void renderColumn(ComputedPixelImage img, String mode, int row, int col) {
    // Intentionally blank
  }
  
  // CornerSentinels cannot be removed
  void removeVertically() {
    // Intentionally blank
  }
  
  // CornerSentinels cannot be inserted
  void insertVertically() {
    // Intentionally blank
  }
  
  // CornerSentinels cannot be removed
  void removeHorizontally() {
    // Intentionally blank
  }
  
  // CornerSentinels cannot be inserted
  void insertHorizontally() {
    // Intentionally blank
  }
  
  // CornerSentinels do not shift right, but do update the last border to left
  void shiftRightBorder() {
    this.left.up = this.up.left;
    this.left.down = this.down.left;
  }
  
  // CornerSentinels do not shift down, but do update the last border above
  void shiftDownBorder() {
    this.up.right = this.right.up;
    this.up.left = this.left.up;
  }
  
  // Computes the ArrayList of VerticalSeamInfo for the bottom row of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
  ArrayList<ASeamInfo> getVerticalSeams() {
    ArrayList<ASeamInfo> initialRow = this.right.rowInfo(new ArrayList<ASeamInfo>(), this);
    ArrayList<ASeamInfo> finalRow = this.down.accumulateRows(initialRow);
    return finalRow;
  }
  
  // Creates a 2D ArrayList of VerticalSeamInfos of minimum weight where each SeamInfo's position
  // in the arrayList corresponds to the position of its currentPixel in the grid
  ArrayList<ArrayList<ASeamInfo>> getAllVerticalSeams() {
    ArrayList<ArrayList<ASeamInfo>> seams = new ArrayList<ArrayList<ASeamInfo>>();
    ArrayList<ASeamInfo> initialRow = this.right.rowInfo(new ArrayList<ASeamInfo>(), this);
    seams.add(initialRow);
    ArrayList<ArrayList<ASeamInfo>> allSeams = this.down.accumulateAllRows(seams);
    return allSeams;
  }
  
  // Computes the ArrayList of HorizontalSeamInfo for the rightmost col of a graph, where each one's
  // cameFrom field can be followed back to get the path of least weight to the top of the graph
  ArrayList<ASeamInfo> getHorizontalSeams() {
    ArrayList<ASeamInfo> initialCol = this.down.colInfo(new ArrayList<ASeamInfo>(), this);
    ArrayList<ASeamInfo> finalCol = this.right.accumulateCols(initialCol);
    return finalCol;
  }
  
  // Creates a 2D ArrayList of HorizontalSeamInfos of minimum weight where each SeamInfo's position
  // in the arrayList corresponds to the position of its currentPixel in the grid
  ArrayList<ArrayList<ASeamInfo>> getAllHorizontalSeams() {
    ArrayList<ArrayList<ASeamInfo>> seams = new ArrayList<ArrayList<ASeamInfo>>();
    ArrayList<ASeamInfo> initialCol = this.down.colInfo(new ArrayList<ASeamInfo>(), this);
    seams.add(initialCol);
    ArrayList<ArrayList<ASeamInfo>> allSeams = this.right.accumulateAllCols(seams);
    return allSeams;
  }

  // Once the accumulateRows method is called on the CornerSentinel, all SeamInfos have been
  // computed, so return the last ArrayList to be created
  ArrayList<ASeamInfo> accumulateRows(ArrayList<ASeamInfo> prevRow) {
    return prevRow;
  }
  
  // Once the accumulateAllRows method is called on the CornerSentinel, all SeamInfos have been
  // computed, so return the last 2D ArrayList to be created
  ArrayList<ArrayList<ASeamInfo>> accumulateAllRows(ArrayList<ArrayList<ASeamInfo>> soFar) {
    return soFar;
  }
  
  // Once the accumulateRows method is called on the CornerSentinel, all SeamInfos have been
  // computed, so return the last ArrayList to be created
  ArrayList<ASeamInfo> accumulateCols(ArrayList<ASeamInfo> prevCol) {
    return prevCol;
  }
  
  // Once the accumulateAllRows method is called on the CornerSentinel, all SeamInfos have been
  // computed, so return the last 2D ArrayList to be created
  ArrayList<ArrayList<ASeamInfo>> accumulateAllCols(ArrayList<ArrayList<ASeamInfo>> soFar) {
    return soFar;
  }
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
  
  // If a border Sentinel is being vertically inserted, shift the borders to its right rightwards
  void insertVertically() {
    this.left.updateRight(this);
    this.right.updateLeft(this);
    if (!(this.right instanceof CornerSentinel)) {
      this.right.shiftRightBorder();
    }
  }
  
  // If a border Sentinel is being horizontally removed, shift the borders below it upwards
  void removeHorizontally() {
    this.down.shiftUpBorder();
    this.up.updateDown(this.down);
  }
  
  // If a border Sentinel is being horizontally inserted, shift the borders below it downwards
  void insertHorizontally() {
    this.down.updateUp(this);
    this.up.updateDown(this);
    if (!(this.down instanceof CornerSentinel)) {
      this.down.shiftDownBorder();
    }
  }
  
  // Shifts this border and all the borders to the right leftwards
  void shiftLeftBorder() {
    this.right.shiftLeftBorder();
    this.updateUp(this.left.up);
    this.updateDown(this.left.down);
  }
  
  // Shifts this border and all the borders to the right rightwards
  void shiftRightBorder() {
    this.up.updateDown(this.left);
    this.down.updateUp(this.left);
    this.right.shiftRightBorder();
  }
  
  // Shifts this border and all the borders below it upwards
  void shiftUpBorder() {
    this.down.shiftUpBorder();
    this.updateRight(this.up.right);
    this.updateLeft(this.up.left);
  }
  
  // Shifts this border and all the borders below it downwards
  void shiftDownBorder() {
    this.right.updateLeft(this.up);
    this.left.updateRight(this.up);
    this.down.shiftDownBorder();
  }
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
  
  //Removes this pixel from the grid by shifting all the rightward neighbors of this pixel left
  void removeVertically() {
    this.right.shiftLeft();
    this.left.updateRight(this.right);
  }
  
  // Inserts this pixel into the grid by shifting all the rightward neighbors of this pixel right
  void insertVertically() {
    this.left.updateRight(this);
    this.right.updateLeft(this);
    if (!(this.right instanceof BorderSentinel)) {
      this.right.shiftRight();
    }
  }
  
  // Removes this pixel from the grid by shifting all the downward neighbors of this pixel up
  void removeHorizontally() {
    this.down.shiftUp();
    this.up.updateDown(this.down);
  }
  
  // Inserts this pixel into the grid by shifting all the downward neighbors of this pixel down
  void insertHorizontally() {
    this.down.updateUp(this);
    this.up.updateDown(this);
    if (!(this.down instanceof BorderSentinel)) {
      this.down.shiftDown();
    }
  }
  
  //Shifts this pixel and all of it's rightward neighbors to the left
  void shiftLeft() {
    this.right.shiftLeft();
    this.updateUp(this.left.up);
    this.updateDown(this.left.down);
  }
  
  // Shifts this pixel and all of it's rightward neighbors to the right
  void shiftRight() {
    this.up.updateDown(this.left);
    this.down.updateUp(this.left);
    this.right.shiftRight();
  }
  
  //Shifts this pixel and all of it's downward neighbors up
  void shiftUp() {
    this.down.shiftUp();
    this.updateRight(this.up.right);
    this.updateLeft(this.up.left);
  }
  
  // Shifts this pixel and all of it's downward neighbors down
  void shiftDown() {
    this.right.updateLeft(this.up);
    this.left.updateRight(this.up);
    this.down.shiftDown();
  }
}

// Represents a info about a path from one side of the grid to the currentPixel
abstract class ASeamInfo {
  
  APixel currentPixel;
  double totalWeight; //accumulative from start
  ASeamInfo cameFrom; //follow path back to get every pixel in the seam
  int index; // Either the row or col of the seamInfo, depending on if Horizontal or Vertical
  
  // Creates an ASeamInfo with the given pixel, weight, and previous SeamInfo
  ASeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom, int index) {
    this.currentPixel = currentPixel;
    this.totalWeight = totalWeight;
    this.cameFrom = cameFrom;
    this.index = index;
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
  
  //Returns the seam info with the greatest weight between this SeamInfo and the provided one.
  // If the weights are the same, the provided SeamInfo is returned
  ASeamInfo greatestWeight(ASeamInfo that) {
    if (this.totalWeight > that.totalWeight) {
      return this;
    } else {
      return that;
    }
  }
  
  // Removes the pixel of this SeamInfo and all the pixels of the previous SeamInfos from the grid
  abstract void remove();
  
  // Inserts the pixel of this SeamInfo and all the pixels of the previous SeamInfos into the grid
  abstract void insert();
  
  // Renders this SeamInfo and all the previous SeamInfos onto the img in the provided color,
  // starting from the provided coord
  abstract void render(ComputedPixelImage img, Color color, int dimension);
  
  // Renders this SeamInfo onto the img in grayscale, as its weight out of the total weight,
  // starting from the provided coord
  abstract void render(ComputedPixelImage img, double maxWeight, int dimension);
}

// Represents a SeamInfo from the top to the bottom of an image
class VerticalSeamInfo extends ASeamInfo {
  
  // Creates a VerticalSeamInfo with the given pixel, weight, and cameFrom
  VerticalSeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom, int row) {
    super(currentPixel, totalWeight, cameFrom, row);
  }
  
  // Vertically removes this pixel and the pixels of all the previous SeamInfos
  void remove() {
    if (this.cameFrom != null) {
      this.cameFrom.remove();
    }
    currentPixel.removeVertically();
  }
  
  // Vertically inserts this pixel and the pixels of all the previous SeamInfos
  void insert() {
    currentPixel.insertVertically();
    if (this.cameFrom != null) {
      this.cameFrom.insert();
    }
  }
  
  // Renders this SeamInfo and all the previous SeamInfos onto the img in the provided color,
  // starting from the provided row
  void render(ComputedPixelImage img, Color color, int row) {
    if (this.cameFrom != null) {
      this.cameFrom.render(img, color, row - 1);
    }
    if (!(this.currentPixel instanceof ASentinel)) {
      img.setColorAt(this.index, row, color);
    }
  }
  
  // Renders this SeamInfo onto the img in grayscale, as its weight out of the total weight,
  // starting from the provided row
  void render(ComputedPixelImage img, double maxWeight, int row) {
    if (!(this.currentPixel instanceof ASentinel)) {
      int grayscaleValue = (int) (255 * (this.totalWeight / maxWeight));
      img.setColorAt(this.index, row, new Color(grayscaleValue, grayscaleValue, grayscaleValue));
    }
  }
}

// Represents a SeamInfo from the left to the right sides of an image
class HorizontalSeamInfo extends ASeamInfo {

  HorizontalSeamInfo(APixel currentPixel, double totalWeight, ASeamInfo cameFrom, int col) {
    super(currentPixel, totalWeight, cameFrom, col);
  }
  
  // Horizontally removes this pixel and the pixels of all the previous SeamInfos
  void remove() {
    if (this.cameFrom != null) {
      this.cameFrom.remove();
    }
    currentPixel.removeHorizontally();
  }
  
  // Horizontally inserts this pixel and the pixels of all the previous SeamInfos
  void insert() {
    currentPixel.insertHorizontally();
    if (this.cameFrom != null) {
      this.cameFrom.insert();
    }
  }
  
  // Renders this SeamInfo and all the previous SeamInfos onto the img in the provided color,
  // starting from the provided col
  void render(ComputedPixelImage img, Color color, int col) {
    if (this.cameFrom != null) {
      this.cameFrom.render(img, color, col - 1);
    }
    if (!(this.currentPixel instanceof ASentinel)) {
      img.setColorAt(col, this.index, color);
    }
  }
  
  // Renders this SeamInfo onto the img in grayscale, as its weight out of the total weight,
  // starting from the provided col
  void render(ComputedPixelImage img, double maxWeight, int col) {
    if (!(this.currentPixel instanceof ASentinel)) {
      int grayscaleValue = (int) (255 * (this.totalWeight / maxWeight));
      img.setColorAt(col, this.index, new Color(grayscaleValue, grayscaleValue, grayscaleValue));
    }
  }
}

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
    // Iterates through every seam in seams and updates minWeight if the current seam has a
    // lower weight than the previous minimum weight seam
    for (ASeamInfo s : seams) {
      minWeight = s.leastWeight(minWeight);
    }
    return minWeight;
  }
  
  // Returns the seam of greatest weight in an ArrayList of seams
  ASeamInfo getMaxWeightSeam(ArrayList<ASeamInfo> seams) {
    ASeamInfo maxWeight = seams.get(0);
    // Iterates through every seam in seams and updates maxWeight if the current seam has a
    // greater weight than the previous minimum weight seam
    for (ASeamInfo s : seams) {
      maxWeight = s.greatestWeight(maxWeight);
    }
    return maxWeight;
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

// A program to remove seams of minimum weight from an image.
class SeamCarver extends World {
  
  Graph graph;
  int width;
  int height;
  int time;
  ASeamInfo seamToRemove;
  boolean paused;
  boolean isCurrentlyVertical;
  ArrayList<ASeamInfo> removedSeams;
  String renderMode;
  
  // Creates a SeamCarver with the image at the given filepath
  SeamCarver(String filepath) {
    FromFileImage img = new FromFileImage(filepath);
    this.graph = new Graph(img);
    this.width = (int) img.getWidth();
    this.height = (int) img.getHeight();
    this.time = 0;
    this.paused = false;
    this.removedSeams = new ArrayList<ASeamInfo>();
    this.renderMode = "Color";
  }
  
  // Renders the image on a WorldScene in its renderMode
  public WorldScene makeScene() {
    WorldScene canvas = new WorldScene(this.width, this.height);
    ComputedPixelImage img = new ComputedPixelImage(this.width, this.height);
    graph.render(img, renderMode);
    if (this.time % 2 == 1) {
      if (this.isCurrentlyVertical) {
        this.seamToRemove.render(img, Color.RED, this.height - 1);
      } else {
        this.seamToRemove.render(img, Color.RED, this.width - 1);
      }
    }
    canvas.placeImageXY(img, this.width / 2, this.height / 2);
    return canvas;
  }
  
  // Every tick, if not paused, alternate between finding a seam to remove and coloring it red,
  // and removing it. The seam to be removed is randomly chosen to be horizontal or vertical
  public void onTick() {
    if (!paused) {
      double randNum = Math.floor(Math.random() * 2);
      if (this.time % 2 == 0) {
        if (randNum == 0.0) {
          this.seamToRemove = this.graph.getVerticalSeam();
          this.isCurrentlyVertical = true;
        } else {
          this.seamToRemove = this.graph.getHorizontalSeam();
          this.isCurrentlyVertical = false;
        }
      } else {
        this.seamToRemove.remove();
        this.removedSeams.add(0, this.seamToRemove);
        if (this.isCurrentlyVertical) {
          this.width -= 1;
        } else {
          this.height -= 1;
        }
      }
      this.time += 1;
    }
  }
  
  // Space pauses/unpauses the automatic seam removal.
  // While paused, 'v' or 'h' can be pressed to move one tick forward and
  // remove a vertical or horizontal seam, respectively
  // While paused, 'i' can be pressed to insert the last removed seam back into the grid
  // At any time the render mode can be update with the following keys:
  //  - 'c' sets the image to render in full color
  //  - 'e' sets the image to render as each pixel's energy
  //  - 'g' sets the image to render in grayscale
  //  - 'V' sets the image to render in grayscale as a function of the minimum weight for a
  //    vertical seam to reach each point
  //  - 'H' sets the image to render in grayscale as a function of the minimum weight for a
  //    horizontal seam to reach each point
  public void onKeyEvent(String key) {
    if (key.equals(" ")) {
      this.paused = !this.paused;
    } else if (key.equals("c")) {
      this.renderMode = "Color";
    } else if (key.equals("e")) {
      this.renderMode = "Energy";
    } else if (key.equals("g")) {
      this.renderMode = "Grayscale";
    } else if (key.equals("V")) {
      this.renderMode = "Vertical Weight";
    } else if (key.equals("H")) {
      this.renderMode = "Horizontal Weight";
    } else {
      //remove case: time is odd, remove seam
      if (paused && (key.equals("v") || key.equals("h")) && this.time % 2 == 1) {
        this.seamToRemove.remove();
        this.removedSeams.add(0, this.seamToRemove);
        if (this.isCurrentlyVertical) {
          this.width -= 1;
        } else {
          this.height -= 1;
        }
        time += 1;
      } else if (paused && key.equals("v")) { //vertical case
        this.seamToRemove = this.graph.getVerticalSeam();
        this.isCurrentlyVertical = true;
        time += 1;
      } else if (paused && key.equals("h")) { //horizontal case
        this.seamToRemove = this.graph.getHorizontalSeam();
        this.isCurrentlyVertical = false;
        time += 1;
      } else if (paused && this.time % 2 == 0) {
        if (key.equals("i") && this.removedSeams.size() != 0) {
          removedSeams.get(0).insert();
          if (removedSeams.get(0) instanceof VerticalSeamInfo) {
            this.width += 1;
          } else {
            this.height += 1;
          }
          removedSeams.remove(0);
        }
      }
    }
  }
  
  // The world ends once the width or height of the image is 1
  public boolean shouldWorldEnd() {
    return this.width == 1
        || this.height == 1;
  }
}

class ExamplesSeamCarver {
  
  SeamCarver s = new SeamCarver("src/balloons.jpeg");
  
  
  //void testBang(Tester t) {
  //  s.bigBang(s.width, s.height, 0.0000001);
  //}
  
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
        0.154414, .00001)
        && t.checkInexact(topMid.getEnergy(),
            0.466767, .00001)
        && t.checkInexact(topRight.getEnergy(),
            0.171406, .00001)
        && t.checkInexact(midLeft.getEnergy(),
            0.731346, .00001)
        && t.checkInexact(mid.getEnergy(),
            0.175767, .00001)
        && t.checkInexact(midRight.getEnergy(),
            0.536250, .00001)
        && t.checkInexact(botLeft.getEnergy(),
            0.223716, .00001)
        && t.checkInexact(botMid.getEnergy(),
            0.741415, .00001)
        && t.checkInexact(botRight.getEnergy(),
            0.235765, .00001);

    ArrayList<ASeamInfo> firstRowInfo = topLeft.rowInfo(
            new ArrayList<>(), border3);

    //using getEnergy to avoid inexact values, already tested getEnergy above
    boolean testFirstRow = t.checkExpect(firstRowInfo.get(0),
            new VerticalSeamInfo(topLeft, topLeft.getEnergy(), null, 0))
            && t.checkExpect(firstRowInfo.get(1),
            new VerticalSeamInfo(topMid, topMid.getEnergy(), null, 1))
            && t.checkExpect(firstRowInfo.get(firstRowInfo.size() - 1),
            new VerticalSeamInfo(topRight, topRight.getEnergy(), null, 2));

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
        0.553898, .00001)
        && t.checkInexact(finalRow.get(1).totalWeight,
            1.071596, .00001)
        && t.checkInexact(finalRow.get(2).totalWeight,
            0.565947, .00001);

    boolean testLeastWeight = t.checkExpect(finalRow.get(0).leastWeight(finalRow.get(1)),
            finalRow.get(0))
            && t.checkExpect(finalRow.get(0).leastWeight(finalRow.get(2)),
            finalRow.get(0));

    border3.shiftLeftBorder();

    boolean testShiftBorder = t.checkExpect(border3.up, border6)
            && t.checkExpect(border3.down, midRight)
            && t.checkExpect(border6.down, border3)
            && t.checkExpect(midRight.up, border3);

    finalRow.get(0).remove();

    boolean testRemove = t.checkExpect(border1.right, botMid)
            && t.checkExpect(midLeft.right, midRight)
            && t.checkExpect(border3.right, topMid)
            && t.checkExpect(corner.right, border5);
    
    return testGetVertSeam && testRemove
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
            4.724464, .00001);
  }
  
  boolean testSeamCarvingUtils(Tester t) {
    APixel topLeft = new Pixel(Color.RED);
    APixel top = new Pixel(Color.ORANGE);
    APixel topRight = new Pixel(Color.YELLOW);
    APixel left = new Pixel(Color.GREEN);
    APixel right = new Pixel(Color.BLUE);
    APixel botLeft = new Pixel(Color.GRAY);
    APixel bot = new Pixel(Color.WHITE);
    APixel botRight = new Pixel(Color.BLACK);
    topLeft.updateRight(top);
    top.updateRight(topRight);
    topLeft.updateDown(left);
    topRight.updateDown(right);
    left.updateDown(botLeft);
    right.updateDown(botRight);
    botLeft.updateRight(bot);
    bot.updateRight(botRight);
    SeamCarvingUtils u = new SeamCarvingUtils();
    
    boolean wellFormedTests = t.checkNoException(u, "checkWellFormedPixel", left, right, top, bot)
        && t.checkException(new IllegalArgumentException("Pixel not well-formed!"),
            u, "checkWellFormedPixel", right, left, top, bot);
    
    Graph sadMarks = new Graph(new FromFileImage("src/sadmarks.png"));
    ArrayList<ASeamInfo> seams = sadMarks.corner.getVerticalSeams();
    
    boolean smallestSeamsTests = t.checkExpect(u.getMinWeightSeam(seams), seams.get(147))
        && t.checkExpect(u.getMinWeightAdjacentSeam(seams, 0), seams.get(1))
        && t.checkExpect(u.getMinWeightAdjacentSeam(seams, 1), seams.get(2))
        && t.checkExpect(u.getMinWeightAdjacentSeam(seams, 53), seams.get(52))
        && t.checkExpect(u.getMinWeightAdjacentSeam(seams, 99), seams.get(98))
        && t.checkExpect(u.getMinWeightAdjacentSeam(seams, 299), seams.get(298));
    
    return wellFormedTests && smallestSeamsTests;
  }
  
  boolean testSentinels(Tester t) {
    CornerSentinel corner = new CornerSentinel();
    corner.addLeft(new BorderSentinel());
    corner.addLeft(new BorderSentinel());
    corner.addLeft(new BorderSentinel());
    BorderSentinel row1 = new BorderSentinel();
    row1.addLeft(new Pixel(Color.RED));
    row1.addLeft(new Pixel(Color.ORANGE));
    row1.addLeft(new Pixel(Color.YELLOW));
    BorderSentinel row2 = new BorderSentinel();
    row2.addLeft(new Pixel(Color.GREEN));
    row2.addLeft(new Pixel(Color.CYAN));
    row2.addLeft(new Pixel(Color.BLUE));
    BorderSentinel row3 = new BorderSentinel();
    row3.addLeft(new Pixel(Color.BLACK));
    row3.addLeft(new Pixel(Color.WHITE));
    row3.addLeft(new Pixel(Color.GRAY));
    
    boolean checkInits = t.checkExpect(corner.right, corner.left.left.left)
        && t.checkExpect(row1.right.right.down, row1.left.left)
        && t.checkExpect(row2.up, row2)
        && t.checkExpect(row3.right.down, row3.down.left.up.left.left);
    
    corner.addRowAbove(row1);
    corner.addRowAbove(row2);
    corner.addRowAbove(row3);
    
    boolean checkGrid = t.checkExpect(corner.right, corner.left.left.left)
        && t.checkExpect(row1.right.right.down, row2.left.left)
        && t.checkExpect(row1.right.right.down, row2.right.right)
        && t.checkExpect(row2.up, row1)
        && t.checkExpect(row3.right.down, row3.down.left.up.left.left.down)
        && t.checkExpect(row3.right.down, corner.right);
    
    boolean energies = t.checkExpect(corner.getEnergy(), 0.0)
        && t.checkExpect(row1.getEnergy(), 0.0)
        && t.checkExpect(row3.getEnergy(), 0.0)
        && t.checkInexact(row2.right.getEnergy(), 2.939752, 0.0001)
        && t.checkInexact(row1.right.right.getEnergy(), 2.108185, 0.0001);
    
    corner.removeVertically();
    
    boolean testCornerRemoval = t.checkExpect(corner.right.left, corner)
        && t.checkExpect(corner.left.right, corner)
        && t.checkExpect(corner.up.down, corner)
        && t.checkExpect(corner.down.up, corner);
    
    APixel border1Up = corner.right.up;
    APixel border1Down = corner.right.down;
    APixel border2Up = corner.right.right.up;
    APixel border2Down = corner.right.right.down;
    
    corner.right.shiftLeft();
    
    boolean testRegularShiftLeft = t.checkExpect(corner.right.up, border1Up)
        && t.checkExpect(corner.right.down, border1Down)
        && t.checkExpect(corner.right.right.up, border2Up)
        && t.checkExpect(corner.right.right.down, border2Down);
    
    corner.right.right.shiftLeftBorder();
    
    // Note: the links in the grid are slightly messed up when looked at as a whole
    // due to the use of the shiftLeftBorder method outside of the removeVertical method
    // and without the removal of a full seam
    boolean testBorderShiftLeft = t.checkExpect(corner.right.up, border1Up)
        && t.checkExpect(corner.right.down, border1Down)
        && t.checkExpect(corner.right.right.up, border1Up)
        && t.checkExpect(corner.right.right.down, border1Down)
        && t.checkExpect(border1Up.down, corner.right.right)
        && t.checkExpect(border1Down.up, corner.right.right);
    
    return checkInits && checkGrid && energies && testCornerRemoval
        && testRegularShiftLeft && testBorderShiftLeft;
  }
  
  boolean testSeamCarver(Tester t) {
    SeamCarver balloons = new SeamCarver("src/balloons.jpeg");
    SeamCarver balloons2 = new SeamCarver("src/balloons.jpeg");
    
    boolean unpaused = t.checkExpect(balloons.paused, false);
    balloons.onKeyEvent(" ");
    boolean paused = t.checkExpect(balloons.paused, true);
    WorldScene init = balloons.makeScene();
    balloons.onTick();
    balloons.onTick();
    boolean tickWhilePaused = t.checkExpect(balloons.makeScene(), init)
        && t.checkExpect(balloons.time, 0);
    
    balloons.onKeyEvent("v");
    balloons2.seamToRemove = balloons2.graph.getVerticalSeam();
    balloons2.isCurrentlyVertical = true;
    balloons2.time++;
    boolean checkHighlightSeam = t.checkExpect(balloons.seamToRemove, balloons2.seamToRemove)
        && t.checkExpect(balloons.makeScene(), balloons2.makeScene())
        && t.checkExpect(balloons.time, 1)
        && t.checkExpect(balloons.width, 800);
    
    balloons.onKeyEvent("v");
    balloons2.seamToRemove.remove();
    balloons2.width--;
    balloons2.time++;
    boolean checkSeamRemoved = t.checkExpect(balloons.makeScene(), balloons2.makeScene())
        && t.checkExpect(balloons.time, 2)
        && t.checkExpect(balloons.width, 799);
    
    WorldScene init2 = balloons2.makeScene();
    balloons2.onKeyEvent("v");
    balloons2.onKeyEvent("v");
    balloons2.onKeyEvent("v");
    boolean checkKeyWhileUnpaused = t.checkExpect(balloons2.makeScene(), init2);
    
    return unpaused && paused && tickWhilePaused && checkHighlightSeam
        && checkSeamRemoved && checkKeyWhileUnpaused;
  }
  
  boolean testSeamCarvingMore(Tester t) {
    SeamCarver balloons = new SeamCarver("src/balloons.jpeg");
    boolean init = t.checkExpect(balloons.shouldWorldEnd(), false);
    balloons.width = 1;
    boolean end = t.checkExpect(balloons.shouldWorldEnd(), true);
    balloons.onKeyEvent("c");
    boolean color = t.checkExpect(balloons.renderMode, "Color");
    balloons.onKeyEvent("e");
    boolean energy = t.checkExpect(balloons.renderMode, "Energy");
    balloons.onKeyEvent("g");
    boolean grayscale = t.checkExpect(balloons.renderMode, "Grayscale");
    balloons.onKeyEvent("V");
    boolean vweight = t.checkExpect(balloons.renderMode, "Vertical Weight");
    balloons.onKeyEvent("H");
    boolean hweight = t.checkExpect(balloons.renderMode, "Horizontal Weight");
    return init && end && color && energy && grayscale && vweight && hweight;
  }
  
  boolean testConstructors(Tester t) {
    APixel topLeft = new Pixel(Color.RED);
    APixel top = new Pixel(Color.ORANGE);
    APixel topRight = new Pixel(Color.YELLOW);
    APixel left = new Pixel(Color.GREEN);
    APixel right = new Pixel(Color.BLUE);
    APixel botLeft = new Pixel(Color.GRAY);
    APixel bot = new Pixel(Color.WHITE);
    APixel botRight = new Pixel(Color.BLACK);
    topLeft.updateRight(top);
    top.updateRight(topRight);
    topLeft.updateDown(left);
    topRight.updateDown(right);
    left.updateDown(botLeft);
    right.updateDown(botRight);
    botLeft.updateRight(bot);
    bot.updateRight(botRight);
    
    boolean constructors = t.checkConstructorNoException("Good Construction", "Pixel",
        Color.RED, left, right, top, bot)
        && t.checkConstructorNoException("Good Construction", "BorderSentinel",
            left, right, top, bot)
        && t.checkConstructorNoException("Good Construction", "CornerSentinel",
            left, right, top, bot);
    
    return constructors;
  }
  
  boolean testGreatestWeight(Tester t) {
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
    
    boolean testGreatestWeight = t.checkExpect(finalRow.get(0).greatestWeight(finalRow.get(1)),
        finalRow.get(1))
        && t.checkExpect(finalRow.get(1).greatestWeight(finalRow.get(2)),
        finalRow.get(1));
    
    Graph sadmarks = new Graph(new FromFileImage("src/sadmarks.png"));
    
    ArrayList<ASeamInfo> vertSeams = sadmarks.corner.getVerticalSeams();
    ArrayList<ASeamInfo> horzSeams = sadmarks.corner.getHorizontalSeams();
    SeamCarvingUtils u = new SeamCarvingUtils();
    boolean testMaxWeightSeam = t.checkExpect(u.getMaxWeightSeam(vertSeams), vertSeams.get(125))
        && t.checkExpect(u.getMaxWeightSeam(horzSeams), horzSeams.get(124));
    return testGreatestWeight && testMaxWeightSeam;
  }
  
}
