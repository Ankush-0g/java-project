import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads chess piece PNGs using a direct relative file path (not classpath resources).
 *
 * Requirements implemented:
 *  1) Use relative path "src/resources/" + piece asset filename.
 *  2) Check existence with file.exists().
 *  3) If exists -> load into ImageIcon and scale smoothly.
 *  4) If not exists -> print file.getAbsolutePath() so you see exactly where it searches.
 */
public class PieceImageLoader {

    private final Map<String, BufferedImage> cacheOriginal = new HashMap<>();
    private final Map<String, ImageIcon> cacheScaled = new HashMap<>();

    /**
     * @param piece notation char from your board: 'P','R','N','B','Q','K' or lowercase for black; ' ' for empty.
     * @param targetSize size in pixels for the square (e.g., 60, or dynamic).
     */
    public Icon getPieceIcon(char piece, int targetSize) {
        if (piece == ' ') return null;

        boolean isWhite = Character.isUpperCase(piece);
        char lower = Character.toLowerCase(piece);

        String filename = toAssetFilename(isWhite, lower);
        if (filename == null) return null;

        String scaledKey = filename + "@" + targetSize;
        ImageIcon cached = cacheScaled.get(scaledKey);
        if (cached != null) return cached;

        BufferedImage original = loadOriginal(filename);
        if (original == null) return null;

        Image scaled = original.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);
        cacheScaled.put(scaledKey, icon);
        return icon;
    }

    private String toAssetFilename(boolean isWhite, char lowerPiece) {
        // Mapping based on your current notation
        return switch (lowerPiece) {
            case 'p' -> (isWhite ? "w_pawn.png" : "b_pawn.png");
            case 'r' -> (isWhite ? "w_rook.png" : "b_rook.png");
            case 'n' -> (isWhite ? "w_knight.png" : "b_knight.png");
            case 'b' -> (isWhite ? "w_bishop.png" : "b_bishop.png");
            case 'q' -> (isWhite ? "w_queen.png" : "b_queen.png");
            case 'k' -> (isWhite ? "w_king.png" : "b_king.png");
            default -> null;
        };
    }

    private BufferedImage loadOriginal(String filename) {
        BufferedImage cached = cacheOriginal.get(filename);
        if (cached != null) return cached;

        // Relative to current working directory (where you run: java ChessGameUI)
        File file = new File("src/resources/" + filename);

        if (!file.exists()) {
            System.err.println("[PieceImageLoader] Missing file: " + file.getAbsolutePath());
            return null;
        }

        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                cacheOriginal.put(filename, img);
            }
            return img;
        } catch (IOException e) {
            System.err.println("[PieceImageLoader] Failed loading file: " + file.getAbsolutePath() + " : " + e.getMessage());
            return null;
        }
    }
}

