package nikolay.images;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import nikolay.utils.ImageUtils;

public class Algorithms {

	private BufferedImage imageRgb;
	private int[][] image;
	private int h;
	private int w;
	private int completeRows = 0;

	public static final int TYPE_EUCLIDEAN = 1;
	public static final int TYPE_MANHATTAN = 2;

	public Algorithms(BufferedImage image) {
		this.h = image.getHeight();
		this.w = image.getWidth();
		this.imageRgb = image;
		this.image = imageToMatrix();
	}

	public BufferedImage getImageRgb() {
		return imageRgb;
	}

	public BufferedImage getImage() {
		return matrixToImage();
	}

	public int[][] getImageMatrix() {
		return image;
	}

	private int[][] imageToMatrix() {
		System.out.println("Converting to int matrix...");
		int[][] matrix = new int[h][w];

		Thread t1 = new Thread(() -> {
			for (int row = 0; row < h / 2; row++) {
				for (int col = 0; col < w; col++) {
					matrix[row][col] = imageRgb.getRGB(col, row);
				}
			}
		});

		Thread t2 = new Thread(() -> {
			for (int row = h / 2; row < h; row++) {
				matrix[row] = new int[w];
				for (int col = 0; col < w; col++) {
					matrix[row][col] = imageRgb.getRGB(col, row);
				}
			}
		});

		t1.start();
		t2.start();

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return matrix;
	}

	private BufferedImage matrixToImage() {
		System.out.println("Converting int matrix to image...");
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		Thread t1 = new Thread(() -> {
			for (int row = 0; row < h / 2; row++) {
				for (int col = 0; col < w; col++) {
					result.setRGB(col, row, image[row][col]);
				}
			}
		});

		Thread t2 = new Thread(() -> {
			for (int row = h / 2; row < h; row++) {
				for (int col = 0; col < w; col++) {
					result.setRGB(col, row, image[row][col]);
				}
			}
		});

		t1.start();
		t2.start();

		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void quantize(int levels) {
		if (levels < 2) {
			throw new IllegalArgumentException("Cannot quantize to less than 2 levels");
		}

		int[] levelsValues = new int[levels];
		int diff = 256 / ((levels - 1) * 2);
		for (int i = 0; i < levels; i++) {
			levelsValues[i] = (i * 255) / (levels - 1);
		}
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				int colorR = (image[row][col] & 0x00ff0000) >> 16;
				int colorG = (image[row][col] & 0x0000ff00) >> 8;
				int colorB = image[row][col] & 0x000000ff;
				int result = 0x00000000;

				for (int i = 0; i < levelsValues.length; i++) {
					int upperLimit = levelsValues[i] + diff;
					if (colorR <= upperLimit) {
						result = result | (levelsValues[i] << 16);
						break;
					}

				}
				for (int i = 0; i < levelsValues.length; i++) {
					int upperLimit = levelsValues[i] + diff;
					if (colorG <= upperLimit) {
						result = result | (levelsValues[i] << 8);
						break;
					}

				}
				for (int i = 0; i < levelsValues.length; i++) {
					int upperLimit = levelsValues[i] + diff;
					if (colorB <= upperLimit) {
						result = result | levelsValues[i];
						break;
					}

				}

				int upperLimit = 256 - diff;
				if (colorR > upperLimit) {
					result = result | (levelsValues[levelsValues.length - 1] << 16);
				}
				if (colorG > upperLimit) {
					result = result | (levelsValues[levelsValues.length - 1] << 8);
				}
				if (colorB > upperLimit) {
					result = result | levelsValues[levelsValues.length - 1];
				}
				image[row][col] = result;
			}
		}
	}

	private static int quantizeValue(int color, int levels) {
		if (levels < 2) {
			throw new IllegalArgumentException("Cannot quantize to less than 2 levels");
		}

		int[] levelsValues = new int[levels];
		int diff = 256 / ((levels - 1) * 2);
		for (int i = 0; i < levels; i++) {
			levelsValues[i] = (i * 255) / (levels - 1);
		}

		int result = 0x00000000;

		for (int i = 0; i < levelsValues.length; i++) {
			int upperLimit = levelsValues[i] + diff;
			if (color <= upperLimit) {
				result = levelsValues[i];
				break;
			}
		}

		int upperLimit = 256 - diff;
		if (color > upperLimit) {
			result = levelsValues[levelsValues.length - 1];
		}

		return result;
	}

	public void rgbToGray() {
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int color = image[row][col];
				int red = (color & 0xff0000) >> 16;
				int green = (color & 0x00ff00) >> 8;
				int blue = color & 0x0000ff;
				int newColor = (int) (0.3 * red + 0.59 * green + 0.11 * blue);
				if (newColor > 255) {
					newColor = 255;
				} else if (newColor < 0) {
					newColor = 0;
				}
				int bw = 0xff000000;
				bw |= (newColor << 16) | (newColor << 8) | newColor;
				image[row][col] = bw;
			}
		}
	}

	/**
	 * @param levels
	 *            - quantization levels
	 */
	public void fsDither(int levels) {
		System.out.println("Floyd-Steinberg dither...");
		int[][][] matrix = new int[h][w][3];

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int pixel = image[row][col];
				matrix[row][col][0] = (pixel & 0x00ff0000) >> 16;
				matrix[row][col][1] = (pixel & 0x0000ff00) >> 8;
				matrix[row][col][2] = (pixel & 0x000000ff) >> 0;
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				int oldpixelR = matrix[row][col][0];
				int oldpixelG = matrix[row][col][1];
				int oldpixelB = matrix[row][col][2];

				int newpixelR = quantizeValue(oldpixelR, levels);
				int newpixelG = quantizeValue(oldpixelG, levels);
				int newpixelB = quantizeValue(oldpixelB, levels);

				matrix[row][col][0] = newpixelR;
				matrix[row][col][1] = newpixelG;
				matrix[row][col][2] = newpixelB;

				int quantErrorR = oldpixelR - newpixelR;
				int quantErrorG = oldpixelG - newpixelG;
				int quantErrorB = oldpixelB - newpixelB;

				// 7/16
				if ((col + 1) < w) {
					matrix[row][col + 1][0] += new Double(quantErrorR * 7.0 / 16.0).intValue();
					matrix[row][col + 1][1] += new Double(quantErrorG * 7.0 / 16.0).intValue();
					matrix[row][col + 1][2] += new Double(quantErrorB * 7.0 / 16.0).intValue();
				}

				// 3/16
				if ((row + 1) < h && (col - 1) >= 0) {
					matrix[row + 1][col - 1][0] += ((double) quantErrorR * 3.0 / 16.0);
					matrix[row + 1][col - 1][1] += ((double) quantErrorG * 3.0 / 16.0);
					matrix[row + 1][col - 1][2] += ((double) quantErrorB * 3.0 / 16.0);
				}

				// 5/16
				if ((row + 1) < h) {
					matrix[row + 1][col][0] += ((double) quantErrorR * 5.0 / 16.0);
					matrix[row + 1][col][1] += ((double) quantErrorG * 5.0 / 16.0);
					matrix[row + 1][col][2] += (double) (quantErrorB * 5.0 / 16.0);
				}

				// 1/16
				if ((row + 1) < h && (col + 1) < w) {
					matrix[row + 1][col + 1][0] += ((double) quantErrorR * 1.0 / 16.0);
					matrix[row + 1][col + 1][1] += ((double) quantErrorG * 1.0 / 16.0);
					matrix[row + 1][col + 1][2] += ((double) quantErrorB * 1.0 / 16.0);
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				for (int color = 0; color < 3; color++) {
					if (matrix[row][col][color] < 0) {
						matrix[row][col][color] = 0;
					}
					if (matrix[row][col][color] > 255) {
						matrix[row][col][color] = 255;
					}
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				image[row][col] = matrix[row][col][0] << 16 | matrix[row][col][1] << 8 | matrix[row][col][2];
			}
		}

	}

	/**
	 * @param levels
	 *            - quantization levels
	 */
	public void jjnDither(int levels) {
		System.out.println("Jarvis-Judice-Ninke dither...");
		int[][][] matrix = new int[h][w][3];
		double divider = 48.0;

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int pixel = image[row][col];
				matrix[row][col][0] = (pixel & 0x00ff0000) >> 16;
				matrix[row][col][1] = (pixel & 0x0000ff00) >> 8;
				matrix[row][col][2] = (pixel & 0x000000ff) >> 0;
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				int oldpixelR = matrix[row][col][0];
				int oldpixelG = matrix[row][col][1];
				int oldpixelB = matrix[row][col][2];

				int newpixelR = quantizeValue(oldpixelR, levels);
				int newpixelG = quantizeValue(oldpixelG, levels);
				int newpixelB = quantizeValue(oldpixelB, levels);

				matrix[row][col][0] = newpixelR;
				matrix[row][col][1] = newpixelG;
				matrix[row][col][2] = newpixelB;

				int quantErrorR = oldpixelR - newpixelR;
				int quantErrorG = oldpixelG - newpixelG;
				int quantErrorB = oldpixelB - newpixelB;

				// 7/48
				if ((col + 1) < w) {
					matrix[row][col + 1][0] += new Double(quantErrorR * 7.0 / divider).intValue();
					matrix[row][col + 1][1] += new Double(quantErrorG * 7.0 / divider).intValue();
					matrix[row][col + 1][2] += new Double(quantErrorB * 7.0 / divider).intValue();
				}

				// 5/48
				if ((col + 2) < w) {
					matrix[row][col + 2][0] += new Double(quantErrorR * 5.0 / divider).intValue();
					matrix[row][col + 2][1] += new Double(quantErrorG * 5.0 / divider).intValue();
					matrix[row][col + 2][2] += new Double(quantErrorB * 5.0 / divider).intValue();
				}

				// 3/48
				if ((row + 1) < h && (col - 2) >= 0) {
					matrix[row + 1][col - 2][0] += ((double) quantErrorR * 3.0 / divider);
					matrix[row + 1][col - 2][1] += ((double) quantErrorG * 3.0 / divider);
					matrix[row + 1][col - 2][2] += ((double) quantErrorB * 3.0 / divider);
				}

				// 5/48
				if ((row + 1) < h && (col - 1) >= 0) {
					matrix[row + 1][col - 1][0] += ((double) quantErrorR * 5.0 / divider);
					matrix[row + 1][col - 1][1] += ((double) quantErrorG * 5.0 / divider);
					matrix[row + 1][col - 1][2] += (double) (quantErrorB * 5.0 / divider);
				}

				// 7/48
				if ((row + 1) < h) {
					matrix[row + 1][col][0] += ((double) quantErrorR * 7.0 / divider);
					matrix[row + 1][col][1] += ((double) quantErrorG * 7.0 / divider);
					matrix[row + 1][col][2] += ((double) quantErrorB * 7.0 / divider);
				}

				// 5/48
				if ((row + 1) < h && (col + 1) < w) {
					matrix[row + 1][col + 1][0] += ((double) quantErrorR * 5.0 / divider);
					matrix[row + 1][col + 1][1] += ((double) quantErrorG * 5.0 / divider);
					matrix[row + 1][col + 1][2] += ((double) quantErrorB * 5.0 / divider);
				}

				// 3/48
				if ((row + 1) < h && (col + 2) < w) {
					matrix[row + 1][col + 2][0] += ((double) quantErrorR * 3.0 / divider);
					matrix[row + 1][col + 2][1] += ((double) quantErrorG * 3.0 / divider);
					matrix[row + 1][col + 2][2] += ((double) quantErrorB * 3.0 / divider);
				}

				// 1/48
				if ((row + 2) < h && (col - 2) >= 0) {
					matrix[row + 2][col - 2][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 2][col - 2][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 2][col - 2][2] += ((double) quantErrorB * 1.0 / divider);
				}

				// 3/48
				if ((row + 2) < h && (col - 1) >= 0) {
					matrix[row + 2][col - 1][0] += ((double) quantErrorR * 3.0 / divider);
					matrix[row + 2][col - 1][1] += ((double) quantErrorG * 3.0 / divider);
					matrix[row + 2][col - 1][2] += ((double) quantErrorB * 3.0 / divider);
				}

				// 5/48
				if ((row + 2) < h) {
					matrix[row + 2][col][0] += ((double) quantErrorR * 5.0 / divider);
					matrix[row + 2][col][1] += ((double) quantErrorG * 5.0 / divider);
					matrix[row + 2][col][2] += ((double) quantErrorB * 5.0 / divider);
				}

				// 3/48
				if ((row + 2) < h && (col + 1) < w) {
					matrix[row + 2][col + 1][0] += ((double) quantErrorR * 3.0 / divider);
					matrix[row + 2][col + 1][1] += ((double) quantErrorG * 3.0 / divider);
					matrix[row + 2][col + 1][2] += ((double) quantErrorB * 3.0 / divider);
				}

				// 1/48
				if ((row + 2) < h && (col + 2) < w) {
					matrix[row + 2][col + 2][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 2][col + 2][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 2][col + 2][2] += ((double) quantErrorB * 1.0 / divider);
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				for (int color = 0; color < 3; color++) {
					if (matrix[row][col][color] < 0) {
						matrix[row][col][color] = 0;
					}
					if (matrix[row][col][color] > 255) {
						matrix[row][col][color] = 255;
					}
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				image[row][col] = matrix[row][col][0] << 16 | matrix[row][col][1] << 8 | matrix[row][col][2];
			}
		}
	}

	/**
	 * @param levels
	 *            - quantization levels
	 */
	public void stuckiDither(int levels) {

		System.out.println("Stucki dither...");
		int[][][] matrix = new int[h][w][3];
		double divider = 42.0;

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int pixel = image[row][col];
				matrix[row][col][0] = (pixel & 0x00ff0000) >> 16;
				matrix[row][col][1] = (pixel & 0x0000ff00) >> 8;
				matrix[row][col][2] = (pixel & 0x000000ff) >> 0;
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				int oldpixelR = matrix[row][col][0];
				int oldpixelG = matrix[row][col][1];
				int oldpixelB = matrix[row][col][2];

				int newpixelR = quantizeValue(oldpixelR, levels);
				int newpixelG = quantizeValue(oldpixelG, levels);
				int newpixelB = quantizeValue(oldpixelB, levels);

				matrix[row][col][0] = newpixelR;
				matrix[row][col][1] = newpixelG;
				matrix[row][col][2] = newpixelB;

				int quantErrorR = oldpixelR - newpixelR;
				int quantErrorG = oldpixelG - newpixelG;
				int quantErrorB = oldpixelB - newpixelB;

				// 8/42
				if ((col + 1) < w) {
					matrix[row][col + 1][0] += new Double(quantErrorR * 8.0 / divider).intValue();
					matrix[row][col + 1][1] += new Double(quantErrorG * 8.0 / divider).intValue();
					matrix[row][col + 1][2] += new Double(quantErrorB * 8.0 / divider).intValue();
				}

				// 4/42
				if ((col + 2) < w) {
					matrix[row][col + 2][0] += new Double(quantErrorR * 4.0 / divider).intValue();
					matrix[row][col + 2][1] += new Double(quantErrorG * 4.0 / divider).intValue();
					matrix[row][col + 2][2] += new Double(quantErrorB * 4.0 / divider).intValue();
				}

				// 2/42
				if ((row + 1) < h && (col - 2) >= 0) {
					matrix[row + 1][col - 2][0] += ((double) quantErrorR * 2.0 / divider);
					matrix[row + 1][col - 2][1] += ((double) quantErrorG * 2.0 / divider);
					matrix[row + 1][col - 2][2] += ((double) quantErrorB * 2.0 / divider);
				}

				// 4/42
				if ((row + 1) < h && (col - 1) >= 0) {
					matrix[row + 1][col - 1][0] += ((double) quantErrorR * 4.0 / divider);
					matrix[row + 1][col - 1][1] += ((double) quantErrorG * 4.0 / divider);
					matrix[row + 1][col - 1][2] += (double) (quantErrorB * 4.0 / divider);
				}

				// 8/42
				if ((row + 1) < h) {
					matrix[row + 1][col][0] += ((double) quantErrorR * 8.0 / divider);
					matrix[row + 1][col][1] += ((double) quantErrorG * 8.0 / divider);
					matrix[row + 1][col][2] += ((double) quantErrorB * 8.0 / divider);
				}

				// 4/42
				if ((row + 1) < h && (col + 1) < w) {
					matrix[row + 1][col + 1][0] += ((double) quantErrorR * 4.0 / divider);
					matrix[row + 1][col + 1][1] += ((double) quantErrorG * 4.0 / divider);
					matrix[row + 1][col + 1][2] += ((double) quantErrorB * 4.0 / divider);
				}

				// 2/42
				if ((row + 1) < h && (col + 2) < w) {
					matrix[row + 1][col + 2][0] += ((double) quantErrorR * 2.0 / divider);
					matrix[row + 1][col + 2][1] += ((double) quantErrorG * 2.0 / divider);
					matrix[row + 1][col + 2][2] += ((double) quantErrorB * 2.0 / divider);
				}

				// 1/42
				if ((row + 2) < h && (col - 2) >= 0) {
					matrix[row + 2][col - 2][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 2][col - 2][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 2][col - 2][2] += ((double) quantErrorB * 1.0 / divider);
				}

				// 2/42
				if ((row + 2) < h && (col - 1) >= 0) {
					matrix[row + 2][col - 1][0] += ((double) quantErrorR * 2.0 / divider);
					matrix[row + 2][col - 1][1] += ((double) quantErrorG * 2.0 / divider);
					matrix[row + 2][col - 1][2] += ((double) quantErrorB * 2.0 / divider);
				}

				// 4/42
				if ((row + 2) < h) {
					matrix[row + 2][col][0] += ((double) quantErrorR * 4.0 / divider);
					matrix[row + 2][col][1] += ((double) quantErrorG * 4.0 / divider);
					matrix[row + 2][col][2] += ((double) quantErrorB * 4.0 / divider);
				}

				// 2/42
				if ((row + 2) < h && (col + 1) < w) {
					matrix[row + 2][col + 1][0] += ((double) quantErrorR * 2.0 / divider);
					matrix[row + 2][col + 1][1] += ((double) quantErrorG * 2.0 / divider);
					matrix[row + 2][col + 1][2] += ((double) quantErrorB * 2.0 / divider);
				}

				// 1/42
				if ((row + 2) < h && (col + 2) < w) {
					matrix[row + 2][col + 2][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 2][col + 2][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 2][col + 2][2] += ((double) quantErrorB * 1.0 / divider);
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				for (int color = 0; color < 3; color++) {
					if (matrix[row][col][color] < 0) {
						matrix[row][col][color] = 0;
					}
					if (matrix[row][col][color] > 255) {
						matrix[row][col][color] = 255;
					}
				}
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				image[row][col] = matrix[row][col][0] << 16 | matrix[row][col][1] << 8 | matrix[row][col][2];
			}
		}
	}

	/**
	 * @param levels
	 *            - quantization levels
	 */
	public void atkinsonDither(int levels) {

		System.out.println("Atkinson dither...");
		int[][][] matrix = new int[h][w][3];
		double divider = 8.0;
		this.completeRows = 0;

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int pixel = image[row][col];
				matrix[row][col][0] = (pixel & 0x00ff0000) >> 16;
				matrix[row][col][1] = (pixel & 0x0000ff00) >> 8;
				matrix[row][col][2] = (pixel & 0x000000ff) >> 0;
			}
		}

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				int oldpixelR = matrix[row][col][0];
				int oldpixelG = matrix[row][col][1];
				int oldpixelB = matrix[row][col][2];

				int newpixelR = quantizeValue(oldpixelR, levels);
				int newpixelG = quantizeValue(oldpixelG, levels);
				int newpixelB = quantizeValue(oldpixelB, levels);

				matrix[row][col][0] = newpixelR;
				matrix[row][col][1] = newpixelG;
				matrix[row][col][2] = newpixelB;

				int quantErrorR = oldpixelR - newpixelR;
				int quantErrorG = oldpixelG - newpixelG;
				int quantErrorB = oldpixelB - newpixelB;

				// 1/8
				if ((col + 1) < w) {
					matrix[row][col + 1][0] += new Double(quantErrorR * 1.0 / divider).intValue();
					matrix[row][col + 1][1] += new Double(quantErrorG * 1.0 / divider).intValue();
					matrix[row][col + 1][2] += new Double(quantErrorB * 1.0 / divider).intValue();
				}

				// 1/8
				if ((col + 2) < w) {
					matrix[row][col + 2][0] += new Double(quantErrorR * 1.0 / divider).intValue();
					matrix[row][col + 2][1] += new Double(quantErrorG * 1.0 / divider).intValue();
					matrix[row][col + 2][2] += new Double(quantErrorB * 1.0 / divider).intValue();
				}

				// 1/8
				if ((row + 1) < h && (col - 1) >= 0) {
					matrix[row + 1][col - 1][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 1][col - 1][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 1][col - 1][2] += (double) (quantErrorB * 1.0 / divider);
				}

				// 1/8
				if ((row + 1) < h) {
					matrix[row + 1][col][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 1][col][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 1][col][2] += ((double) quantErrorB * 1.0 / divider);
				}

				// 1/8
				if ((row + 1) < h && (col + 1) < w) {
					matrix[row + 1][col + 1][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 1][col + 1][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 1][col + 1][2] += ((double) quantErrorB * 1.0 / divider);
				}

				// 1/8
				if ((row + 2) < h) {
					matrix[row + 2][col][0] += ((double) quantErrorR * 1.0 / divider);
					matrix[row + 2][col][1] += ((double) quantErrorG * 1.0 / divider);
					matrix[row + 2][col][2] += ((double) quantErrorB * 1.0 / divider);
				}
				image[row][col] = matrix[row][col][0] << 16 | matrix[row][col][1] << 8 | matrix[row][col][2];
			}

			System.out.println(String.format("[Atkinson] %.2f%%", (double) (this.completeRows++ * 100) / h));
		}

		this.completeRows = 0;
		System.out.println("[Atkinson] complete.");
	}
}