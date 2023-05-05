import vdp.VDPFrame;
import vdp.VectorGraphicsRenderer;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;

public class Convert extends VDPFrame implements Runnable {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
            new Convert(args)
        );
    }

    private static final int SCREEN_TABLE_BASE = 0xA854;
    private static final int SCREEN_TABLE_END = 0xA97E;
    private static final int SCREEN_TYPES_DATA_BASE = 0xA982;
    private static final int SCREEN_BACKGROUND_ITEM_START_BASE = 0x757D;
    private static final int SCREEN_BACKGROUND_ITEMS_BASE = 0x76A9;
    private static final int BACKGROUND_ITEMS_BASE = 0x645D;
    private static final int BACKGROUND_GRAPHICS_ATTRIBUTE_TABLE_BASE = 0xA64E;
    private static final int BACKGROUND_GRAPHICS_TABLE_BASE = 0xA600;
    private static final int SPRITES_GRAPHICS_TABLE_BASE = 0xA4BE;

    private static final int[] TI_PAL = {1,4,6,13,12,7,10,14,1,5,8,13,2,7,11,15};

    byte[] ram;

    public Convert(String[] args) {
        super(args);
    }

    public void run() {
        try {
            FileInputStream fis = new FileInputStream("Atic Atac.bin");
            ram = new byte[0x10000];
            int result = fis.read(ram);
            System.out.println(result + " bytes read.");
            fis.close();
            super.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawFrame(VectorGraphicsRenderer renderer, int frameNo) {
        if (frameNo > 0) {
            int screenNo = frameNo - 1;
            int screenTableAddress = SCREEN_TABLE_BASE + (screenNo << 1);
            if (screenTableAddress < SCREEN_TABLE_END) {
                System.out.println("Screen " + screenNo);
                int screenAttribute = getByte(screenTableAddress);
                System.out.println("Screen attribute: " + screenAttribute);
                int screenType = getByte(screenTableAddress + 1);
                System.out.println("Screen type: " + screenType);
                int screenTypesDataAddress = SCREEN_TYPES_DATA_BASE + screenType * 6;
                int width = getByte(screenTypesDataAddress);
                int marginX = 0; // (192 - width) / 2;
                int height = getByte(screenTypesDataAddress + 1);
                int marginY = 0; // (192 - height) / 2;
                System.out.println("Width: " + width + " Height: " + height);
                // Draw room shape
                int pointsAddress = getWord(screenTypesDataAddress + 2);
                int linesAddress = getWord(screenTypesDataAddress + 4);
                int point1Index = getByte(linesAddress++);
                while (point1Index != 255) {
                    int point1Address = pointsAddress + (point1Index << 1);
                    int point1X = getByte(point1Address);
                    int point1Y = getByte(point1Address + 1);
                    int point2Index = getByte(linesAddress++);
                    while (point2Index != 255) {
                        int point2Address = pointsAddress + (point2Index << 1);
                        int point2X = getByte(point2Address);
                        int point2Y = getByte(point2Address + 1);
                        renderer.drawLine(point1X + marginX, point1Y + marginY, point2X + marginX, point2Y + marginY, getTIColor(screenAttribute) >> 4);
                        point2Index = getByte(linesAddress++);
                    }
                    point1Index = getByte(linesAddress++);
                }
                // Draw background items
                int screenBackgroundItemsAddress = getWord(SCREEN_BACKGROUND_ITEM_START_BASE + (screenNo << 1));
                int backgroundItemAddress = getWord(screenBackgroundItemsAddress);
                while (backgroundItemAddress != 0) {
                    // Get background item
                    int graphicsType = getByte(backgroundItemAddress);
                    System.out.println("Graphics type: " + graphicsType);
                    int itemScreenNo = getByte(backgroundItemAddress + 1);
                    int itemX = getByte(backgroundItemAddress + 3);
                    int itemY = getByte(backgroundItemAddress + 4);
                    System.out.println("Item position (" + itemX + "," + itemY + ")");
                    int itemFlags = getByte(backgroundItemAddress + 5);
                    // Draw graphics
                    int graphicsAddress = getWord(BACKGROUND_GRAPHICS_TABLE_BASE + ((graphicsType - 1) << 1));
                    int graphicsWidth = getByte(graphicsAddress);
                    int graphicsHeight = getByte(graphicsAddress + 1);
                    int pixelWidth = graphicsWidth * 8;
                    int pixelHeight = graphicsHeight;
                    System.out.println("Size: " + pixelWidth + " x " + pixelHeight);
                    int[][] grid = new int[pixelHeight][pixelWidth];
                    int addr = graphicsAddress + 2;
                    for (int y = 0; y < graphicsHeight; y++) {
                        for (int x = 0; x < graphicsWidth; x++) {
                            int patternByte = getByte(addr++);
                            int mask = 0x80;
                            for (int p = 0; p < 8; p++) {
                                grid[y][(x << 3) + p] = (patternByte & mask) != 0 ? 1 : 0;
                                mask >>= 1;
                            }
                        }
                    }
                    int graphicsAttributesAddress = getWord(BACKGROUND_GRAPHICS_ATTRIBUTE_TABLE_BASE + ((graphicsType - 1) << 1));
                    addr = graphicsAttributesAddress + 2;
                    for (int y = 0; y < graphicsHeight; y += 8) {
                        for (int x = 0; x < graphicsWidth; x++) {
                            int attribute = getByte(addr++);
                            int tiColor = getTIColor(attribute) & 0xF0;
                            for (int y1 = 0; y1 < 8; y1++) {
                                if (y + y1 < graphicsHeight) {
                                    for (int x1 = 0; x1 < 8; x1++) {
                                        if (grid[y + y1][(x << 3) + x1] != 0) {
                                            grid[y + y1][(x << 3) + x1] = tiColor >> 4;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    int rotation = itemFlags & 0xC0;
                    if (screenNo != itemScreenNo) {
                        System.out.println("Item belongs to screen " + itemScreenNo);
                        if (rotation == 0x00) {
                            rotation = 0x80;
                            itemY = 190 + pixelHeight - itemY;
                        }
                        else if (rotation == 0x80) {
                            rotation = 0x00;
                            itemY = 190 + pixelHeight - itemY;
                        }
                        else if (rotation == 0x40) {
                            rotation = 0xC0;
                            itemX = 192 - pixelHeight - itemX;
                        }
                        else if (rotation == 0xC0) {
                            rotation = 0x40;
                            itemX = 192 - pixelHeight - itemX;
                        }
                    }
                    if (rotation == 0x00) {
                        System.out.println("Rotate top");
                        int[][] newGrid = new int[pixelHeight][pixelWidth];
                        for (int y = 0; y < pixelHeight; y++) {
                            for (int x = 0; x < pixelWidth; x++) {
                                newGrid[y][x] = grid[pixelHeight - 1 - y][x];
                            }
                        }
                        grid = newGrid;
                    }
                    else if (rotation == 0x80) {
                        System.out.println("Rotate bottom");
                    }
                    else if (rotation == 0x40) {
                        System.out.println("Rotate right");
                        int[][] newGrid = new int[pixelWidth][pixelHeight];
                        for (int y = 0; y < pixelHeight; y++) {
                            for (int x = 0; x < pixelWidth; x++) {
                                newGrid[x][y] = grid[y][pixelWidth - 1 - x];
                            }
                        }
                        grid = newGrid;
                    }
                    else if (rotation == 0xC0) {
                        System.out.println("Rotate left");
                        int[][] newGrid = new int[pixelWidth][pixelHeight];
                        for (int y = 0; y < pixelHeight; y++) {
                            for (int x = 0; x < pixelWidth; x++) {
                                newGrid[x][y] = grid[pixelHeight - 1 - y][x];
                            }
                        }
                        grid = newGrid;
                    }
//                    else if (rotation == 0x20) {
//                        System.out.println("Flip vertical");
//                        int[][] newGrid = new int[pixelHeight][pixelWidth];
//                        for (int y = 0; y < pixelHeight; y++) {
//                            for (int x = 0; x < pixelWidth; x++) {
//                                newGrid[y][x] = grid[y][pixelWidth - 1 - x];
//                            }
//                        }
//                        grid = newGrid;
//                    }
//                    else if (rotation == 0x10) {
//                        // Same as rotate top
//                        System.out.println("Flip horizontal");
//                        int[][] newGrid = new int[pixelHeight][pixelWidth];
//                        for (int y = 0; y < pixelHeight; y++) {
//                            for (int x = 0; x < pixelWidth; x++) {
//                                newGrid[y][x] = grid[pixelHeight - 1 - y][x];
//                            }
//                        }
//                        grid = newGrid;
//                    }
                    // vdpCanvas.bitmap(itemX, itemY - graphicsHeight, graphicsWidth, graphicsHeight, patterns, colors);
                    bitmap(itemX, itemY - grid.length + 1, grid[0].length, grid.length, grid);
                    if (screenNo != itemScreenNo) {
                        vdpCanvas.plot(itemX, 191 - itemY, 4);
                    }
                    // Next item
                    screenBackgroundItemsAddress += 2;
                    backgroundItemAddress = getWord(screenBackgroundItemsAddress);
                }
                System.out.println();
                paused = true;
            }
        }
    }

    public void bitmap(int x, int y, int width, int height, int[][] grid) {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                vdpCanvas.plot(x + i, 191 - (y + j), grid[j][i]);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        paused = false;
    }

    private int getWord(int address) {
        return getByte(address) + (getByte(address + 1) << 8);
    }

    private int getByte(int address) {
        return ram[address] & 0xFF;
    }

    private int getTIColor(int attribute) {
        return (TI_PAL[((attribute & 0x40) >> 3) | (attribute & 0x07)] << 4) | TI_PAL[(attribute & 0x78) >> 3];
    }
}
