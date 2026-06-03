import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.HashMap;
import javafx.scene.input.MouseButton;

public class HelloFX extends Application {

    static String[][] board = new String[8][8];
    static StackPane[][] tiles = new StackPane[8][8];
    static HashMap<String, Image> pieces = new HashMap<>();
    static GridPane grid = new GridPane();
    static int selectedRow = -1;
    static int selectedCol = -1;
    static boolean whiteTurn = true;


    @Override
    public void start(Stage stage) {

        boardSetup();

        pieces.put("♔", new Image(getClass().getResourceAsStream("/icons/white-king.png")));
        pieces.put("♕", new Image(getClass().getResourceAsStream("/icons/white-queen.png")));
        pieces.put("♖", new Image(getClass().getResourceAsStream("/icons/white-rook.png")));
        pieces.put("♗", new Image(getClass().getResourceAsStream("/icons/white-bishop.png")));
        pieces.put("♘", new Image(getClass().getResourceAsStream("/icons/white-knight.png")));
        pieces.put("♙", new Image(getClass().getResourceAsStream("/icons/white-pawn.png")));

        pieces.put("♚", new Image(getClass().getResourceAsStream("/icons/black-king.png")));
        pieces.put("♛", new Image(getClass().getResourceAsStream("/icons/black-queen.png")));
        pieces.put("♜", new Image(getClass().getResourceAsStream("/icons/black-rook.png")));
        pieces.put("♝", new Image(getClass().getResourceAsStream("/icons/black-bishop.png")));
        pieces.put("♞", new Image(getClass().getResourceAsStream("/icons/black-knight.png")));
        pieces.put("♟", new Image(getClass().getResourceAsStream("/icons/black-pawn.png")));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle colour = new Rectangle(70, 70);
                if ((row+col)%2==0) {
                    colour.setFill(Color.BEIGE);
                } else {
                    colour.setFill(Color.SADDLEBROWN);
                }
                tile.getChildren().add(colour);

                final int tileRow = row;
                final int tileCol = col;

                tile.setOnMouseClicked(event -> {
                System.out.println("Clicked: " + tileRow + ", " + tileCol);
                if (selectedRow == -1 && !board[tileRow][tileCol].equals("--")) {
                    // First click: select piece
                    selectedRow = tileRow;
                    selectedCol = tileCol;
             
                } else if (selectedRow != -1) {
                    // Second click: attempt move
                    movePiece(selectedRow, selectedCol, tileRow, tileCol);

                    selectedRow = -1;
                    selectedCol = -1;
                }

                });

                // Add a piece if there is one
                String pieceCode = board[row][col];
                if (!pieceCode.equals("--")) {

                    ImageView piece = new ImageView(pieces.get(pieceCode));
                    piece.setUserData("piece");

                    piece.setFitWidth(60);
                    piece.setFitHeight(60);
                    piece.setPreserveRatio(true);

                    tile.getChildren().add(piece);

                }
                
                tiles[row][col] = tile;
                grid.add(tile, col, row);
            }
        }
        Scene scene = new Scene(grid);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void boardSetup() {
        // Black pieces
        board[0][0] = "♜";
        board[0][1] = "♞";
        board[0][2] = "♝";
        board[0][3] = "♛";
        board[0][4] = "♚";
        board[0][5] = "♝";
        board[0][6] = "♞";
        board[0][7] = "♜";

        // Black pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = "♟";
        }

        // Empty squares
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = "--";
            }
        }

        // White pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = "♙";
        }

        // White pieces
        board[7][0] = "♖";
        board[7][1] = "♘";
        board[7][2] = "♗";
        board[7][3] = "♕";
        board[7][4] = "♔";
        board[7][5] = "♗";
        board[7][6] = "♘";
        board[7][7] = "♖";
        
    }

    public static void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        String pieceKey = board[fromRow][fromCol];
        if (validateMove(fromRow, fromCol, toRow, toCol, pieceKey)) {
            updateSquares(fromRow, fromCol, toRow, toCol, pieceKey);
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = "--";
            whiteTurn = !whiteTurn;
        }
    }

    public static void updateSquares(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {
        StackPane startTile = tiles[fromRow][fromCol];
        StackPane endTile = tiles[toRow][toCol];

        String destination = board[toRow][toCol];

        ImageView piece = new ImageView(pieces.get(pieceKey));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setPreserveRatio(true);

        // remove moving piece from start (UI)
        if (!startTile.getChildren().isEmpty()) {
            startTile.getChildren().remove(startTile.getChildren().size() - 1);
        }

        // capture handling (UI)
        if (destination != null && !destination.equals("--")) {
            if (!endTile.getChildren().isEmpty()) {
                endTile.getChildren().remove(endTile.getChildren().size() - 1);
            }
        }

        // place piece
        endTile.getChildren().add(piece);
    };

    public static boolean validateMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {
        if (isFriendlyPiece(board[fromRow][fromCol], board[toRow][toCol])
            || whiteTurn && !isWhite(pieceKey)
            || !whiteTurn && !isBlack(pieceKey)){
            return false;
        }
        else if (pieceKey.equals("♙") || pieceKey.equals("♟")) {
            return pawnMove(fromRow, fromCol, toRow, toCol, pieceKey);
        }
        else if (pieceKey.equals("♘") || pieceKey.equals("♞")) {
            return knightMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.equals("♗") || pieceKey.equals("♝")) {
            return bishopMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.equals("♖") || pieceKey.equals("♜")) {
            return rookMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.equals("♕") || pieceKey.equals("♛")) {
            return rookMove(fromRow, fromCol, toRow, toCol) || bishopMove(fromRow, fromCol, toRow, toCol);
        } else if (pieceKey.equals("♔") || pieceKey.equals("♚")) {
            return kingMove(fromRow, fromCol, toRow, toCol);
        }
        return false;
    }

    public static boolean pawnMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {
        int direction;
        if (pieceKey.equals("♙")) {
            direction = -1;

        } else {
            direction = 1;
        }

        if (board[toRow][toCol].equals("--")) {
            
            if (toCol == fromCol && ((pieceKey.equals("♙") && fromRow == 6) || (pieceKey.equals("♟") && fromRow == 1)) && toRow == fromRow + 2 * direction && board[(toRow+fromRow)/2][toCol].equals("--")) {
                return true;
            } 

            return fromCol == toCol && toRow == fromRow+direction;
        }

        return Math.abs(fromCol-toCol)==1 && toRow == fromRow+direction;
    }

    public static boolean knightMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean senario1 = Math.abs(fromCol - toCol) == 2 && Math.abs(fromRow - toRow) == 1;
        boolean senario2 = Math.abs(fromCol - toCol) == 1 && Math.abs(fromRow - toRow) == 2;
        return senario1 || senario2;
    }

    public static boolean bishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toCol-fromCol) == Math.abs(toRow-fromRow) && noPiecesDiagonal(fromCol, toCol, toRow, fromRow);
    }

    public static boolean rookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (toCol==fromCol && noPiecesHorizontal(fromCol, toRow, fromRow) || toRow==fromRow && noPiecesVertical(fromRow, toCol, fromCol));
    }
    
    public static boolean kingMove(int fromRow, int fromCol, int toRow, int toCol) {
        int colDiff = Math.abs(toCol-fromCol);
        int rowDiff = Math.abs(toRow-fromRow);
        return colDiff <= 1 && rowDiff <=1;
    }

    public static boolean isWhite(String piece) {
        return "♔♕♖♗♘♙".contains(piece);
    }
    public static boolean isBlack(String piece) {
        return "♚♛♜♝♞♟".contains(piece);
    }

    public static boolean isFriendlyPiece(String source, String destination) {

        if (destination.equals("--")) {
            return false;
        }

        return (isWhite(source) && isWhite(destination))
            || (isBlack(source) && isBlack(destination));
    }

    public static boolean noPiecesVertical(int fromRow, int toCol, int fromCol) {
        int start = Math.min(fromCol, toCol);
        int end = Math.max(fromCol, toCol);

        for (int col = start+1; col < end; col++) {
            if (!board[fromRow][col].equals("--")) {
                return false;
            }
        }
        return true;
    }

    public static boolean noPiecesHorizontal(int fromCol, int toRow, int fromRow) {
        int start = Math.min(fromRow, toRow);
        int end = Math.max(fromRow, toRow);

        for (int row = start+1; row < end; row++) {
            if (!board[row][fromCol].equals("--")) {
                return false;
            }
        }
        return true;
    }

    public static boolean noPiecesDiagonal(int fromCol, int toCol, int toRow, int fromRow) {
        int rowDiff = toRow-fromRow;
        int colDiff = toCol-fromCol;
        int rowStep = 1;
        int colStep = 1;
        if (rowDiff < 0) {
            rowStep = -1;
        } 
        if (colDiff < 0) {
            colStep = -1;
        }

        fromRow += rowStep;
        fromCol += colStep;

        while (fromRow != toRow && fromCol != toCol) {
            if (!board[fromRow][fromCol].equals("--")) {
                return false;
            }
            fromCol += colStep;
            fromRow += rowStep;
        }
        return true;
    }

    
}