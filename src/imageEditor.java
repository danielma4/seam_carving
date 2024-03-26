import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

interface IPixel {
  ArrayList<SeamInfo> getVerticalSeams(ArrayList<SeamInfo> prevSeam, ArrayList<SeamInfo> currRowSeam);
}

abstract class APixel implements IPixel {
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
    //field of field okay here?
    if (left.up != up.left
    || right.up != up.right
    || right.down != down.right
    || left.down != down.left) {
      throw new IllegalArgumentException("Pixel not well-formed!");
    }
    this.color = color;
    this.left = left;
    this.right = right;
    this.up = up;
    this.down = down;
    this.topLeft = topLeft;
    this.topRight = topRight;
    this.botLeft = botLeft;
    this.botRight = botRight;
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
}

class Sentinel extends APixel {
  Sentinel(APixel left, APixel right, APixel up, APixel down,
                  APixel topLeft, APixel topRight, APixel botLeft, APixel botRight) {

    super(Color.BLACK, left, right, up, down, topLeft, topRight, botLeft, botRight);
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

//guarantee this is corner through exceptions?
class CornerSentinel extends APixel {
  public CornerSentinel(Color color, APixel left, APixel right, APixel up, APixel down, APixel topLeft, APixel topRight, APixel botLeft, APixel botRight) {
    super(color, left, right, up, down, topLeft, topRight, botLeft, botRight);
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

class Pixel extends APixel {
  Pixel(Color color, APixel left, APixel right, APixel up,
               APixel down, APixel topLeft, APixel topRight,
               APixel botLeft, APixel botRight) {
    super(color, left, right, up, down, topLeft, topRight, botLeft, botRight);
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

//class which represents the grid of seamInfos we want to build up
class ImageEditor {
  CornerSentinel cornerSentinel;

  ImageEditor(CornerSentinel cornerSentinel) {
    this.cornerSentinel = cornerSentinel;
  }

  SeamInfo getVerticalSeam() {
    ArrayList<SeamInfo> leastEnergyPath = new ArrayList<>();
    ArrayList<SeamInfo> verticalSeams = this.cornerSentinel.getVerticalSeams(
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

  class ExamplesSeams {
    //3x3 pixel example
    //how do we do this? we need mutation, an update pixel method maybe? but this would be pretty tedious
  }