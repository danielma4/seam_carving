import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

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
    //field of field okay here?
    if (!left.up.equals(up.left)
    || !right.up.equals(up.right)
    || !right.down.equals(down.right)
    || !left.down.equals(down.left)) {
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

  //this won't work (cyclic data), just use equals for now and change if needed
  //boolean samePixel(APixel that) {}

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
}

class Pixel extends APixel {
  Pixel(Color color, APixel left, APixel right, APixel up,
               APixel down, APixel topLeft, APixel topRight,
               APixel botLeft, APixel botRight) {
    super(color, left, right, up, down, topLeft, topRight, botLeft, botRight);
  }
}

class SeamInfo {
  APixel currentPixel;
  double totalWeight; //accumulative from start
  SeamInfo cameFrom; //follow path back to get seam of least energy
}

//class which represents the grid of seamInfos we want to build up
class ImageEditor {
  APixel topLeftSentinel;
  ArrayList<ArrayList<SeamInfo>> seamInfos;

  public ImageEditor(APixel topLeftSentinel) {
    this.topLeftSentinel = topLeftSentinel;
    this.seamInfos = new ArrayList<>();
  }

  //idea is just to compute the seam infos bottom up, left to right and to place them in the seamInfos ArrayList,
  //allowing for easy computation (above indices)
  //we can just remake the class/arraylist after each removal
}