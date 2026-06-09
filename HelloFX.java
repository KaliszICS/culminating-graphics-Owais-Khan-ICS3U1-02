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
import java.util.ArrayList;
import javafx.scene.layout.VBox;

public class HelloFX extends Application {

    static String[][] board = new String[8][8];  // Acsii board to keep track of game logic
    static StackPane[][] tiles = new StackPane[8][8];  // Easy access to specific tiles
    static HashMap<String, Image> pieces = new HashMap<>();
    static GridPane grid = new GridPane();  // UI board
    static Label statusLabel;

    // Position of selected piece
    static int selectedRow = -1;
    static int selectedCol = -1;

    static boolean whiteTurn = true;

    // Keep track of kings positions for checking logic
    static int whiteKingRow = 7;
    static int blackKingRow = 0;
    static int whiteKingCol = 4;
    static int blackKingCol = 4;
    static boolean whiteInCheck = false;
    static boolean blackInCheck = false; 

    // Directions of the king to check for checks
    static int[][] directions = {
        {-1, 0}, // up
        { 1, 0}, // down
        { 0,-1}, // left
        { 0, 1}, // right

        {-1,-1}, // up-left
        {-1, 1}, // up-right
        { 1,-1}, // down-left
        { 1, 1}  // down-right
    };

    static int[][] knightDirections = {
        {2, 1},
        {2, -1},
        {-2, 1},
        {-2, -1},
        {1, 2},
        {1, -1},
        {-1, 2},
        {-1, -2}
    };


    static String attacker = "";
    static int[] attackerDir = {1,1};
    static int attackerRow = -1;
    static int attackerCol = -1;
    static boolean additionalAttackers = false;

    @Override
    public void start(Stage stage) {

        boardSetup();

        // White pieces
        pieces.put("whiteKing", new Image(getClass().getResourceAsStream("/icons/white-king.png")));
        pieces.put("whiteQueen", new Image(getClass().getResourceAsStream("/icons/white-queen.png")));
        pieces.put("whiteRook", new Image(getClass().getResourceAsStream("/icons/white-rook.png")));
        pieces.put("whiteBishop", new Image(getClass().getResourceAsStream("/icons/white-bishop.png")));
        pieces.put("whiteKnight", new Image(getClass().getResourceAsStream("/icons/white-knight.png")));
        pieces.put("whitePawn", new Image(getClass().getResourceAsStream("/icons/white-pawn.png")));

        // Black pieces
        pieces.put("blackKing", new Image(getClass().getResourceAsStream("/icons/black-king.png")));
        pieces.put("blackQueen", new Image(getClass().getResourceAsStream("/icons/black-queen.png")));
        pieces.put("blackRook", new Image(getClass().getResourceAsStream("/icons/black-rook.png")));
        pieces.put("blackBishop", new Image(getClass().getResourceAsStream("/icons/black-bishop.png")));
        pieces.put("blackKnight", new Image(getClass().getResourceAsStream("/icons/black-knight.png")));
        pieces.put("blackPawn", new Image(getClass().getResourceAsStream("/icons/black-pawn.png")));

        // Setup UI board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle colour = new Rectangle(70, 70);

                // Checker pattern
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

                // Piece selection logic
                if (selectedRow == -1 && !board[tileRow][tileCol].equals("--")
                    && (whiteTurn && isWhite(board[tileRow][tileCol])
                    || !whiteTurn && isBlack(board[tileRow][tileCol]))) {

                    // First click: select piece
                    selectedRow = tileRow;
                    selectedCol = tileCol;
                    Rectangle rect = (Rectangle) tile.getChildren().get(0);
                    rect.setFill(Color.LIGHTBLUE);  // Indication of selected tile
             
                } else if (selectedRow != -1) {

                    // Second click: attempt move
                    movePiece(selectedRow, selectedCol, tileRow, tileCol);
                    StackPane selectedTile = tiles[selectedRow][selectedCol];
                    Rectangle rect = (Rectangle) selectedTile.getChildren().get(0);

                    // Remove indication of selected tile
                    if ((selectedRow+selectedCol)%2==0) {
                        rect.setFill(Color.BEIGE);
                    } else {
                        rect.setFill(Color.SADDLEBROWN); 
                    }
                    
                    // Unselect piece
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
        statusLabel = new Label("White to move");

        VBox root = new VBox();
        root.getChildren().addAll(statusLabel, grid);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void resetTileColor(int row, int col) {
        Rectangle rect = (Rectangle) tiles[row][col].getChildren().get(0);

        if ((row + col) % 2 == 0) {
            rect.setFill(Color.BEIGE);
        } else {
            rect.setFill(Color.SADDLEBROWN);
        }
    }

    public static void highlightCheckKing(int row, int col) {
        Rectangle rect = (Rectangle) tiles[row][col].getChildren().get(0);
        rect.setFill(Color.RED);
    }

    public static void updateCheckIndicators() {

        resetTileColor(whiteKingRow, whiteKingCol);
        resetTileColor(blackKingRow, blackKingCol);

        if (tileIsSeen(whiteKingRow, whiteKingCol, "enemy")) {
            highlightCheckKing(whiteKingRow, whiteKingCol);
        }

        if (tileIsSeen(blackKingRow, blackKingCol, "enemy")) {
            highlightCheckKing(blackKingRow, blackKingCol);
        }
    }

    public static void boardSetup() {
        // Black pieces
        board[0][0] = "blackRook";
        board[0][1] = "blackKnight";
        board[0][2] = "blackBishop";
        board[0][3] = "blackQueen";
        board[0][4] = "blackKing";
        board[0][5] = "blackBishop";
        board[0][6] = "blackKnight";
        board[0][7] = "blackRook";

        // Black pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = "blackPawn";
        }

        // Empty squares
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = "--";
            }
        }

        // White pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = "whitePawn";
        }

        // White pieces
        board[7][0] = "whiteRook";
        board[7][1] = "whiteKnight";
        board[7][2] = "whiteBishop";
        board[7][3] = "whiteQueen";
        board[7][4] = "whiteKing";
        board[7][5] = "whiteBishop";
        board[7][6] = "whiteKnight";
        board[7][7] = "whiteRook";
        
    }

    public static void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        String pieceKey = board[fromRow][fromCol];

        if (validateMove(fromRow, fromCol, toRow, toCol, pieceKey) 
            && !putsOwnKingInCheck(fromRow, fromCol, toRow, toCol)) {

            // Move the piece in the ascii board (logic)
            updateSquares(fromRow, fromCol, toRow, toCol, pieceKey);
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = "--";

            whiteTurn = !whiteTurn;
            if (whiteTurn) {
                statusLabel.setText("white to move");
            } else {
            statusLabel.setText("black to move");
            }

            // Keep track of king positions
            if (pieceKey.equals("whiteKing")) {
                whiteKingRow = toRow;
                whiteKingCol = toCol;
            } else if (pieceKey.equals("blackKing")) {
                blackKingRow = toRow;
                blackKingCol = toCol;
            }

            if (whiteTurn) {
                kingInCheckmateOrStalemate(whiteKingRow, whiteKingCol);
            } else {
                kingInCheckmateOrStalemate(blackKingRow, blackKingCol);
            }
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

        // Check if the landing square contains an enemy piece
        if (destination != null && !destination.equals("--")) {
            if (!endTile.getChildren().isEmpty()) {
                endTile.getChildren().remove(endTile.getChildren().size() - 1);
            }
        }

        // place piece
        endTile.getChildren().add(piece);
    };

    public static boolean validateMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {
        if (isFriendlyPiece(board[fromRow][fromCol], board[toRow][toCol])){
            return false;
        }
        else if (pieceKey.contains("Pawn")) {
            return pawnMove(fromRow, fromCol, toRow, toCol, pieceKey);
        }
        else if (pieceKey.endsWith("Knight")) {
            return knightMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.endsWith("Bishop")) {
            return bishopMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.contains("Rook")) {
            return rookMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.endsWith("Queen")) {
            return rookMove(fromRow, fromCol, toRow, toCol)
                || bishopMove(fromRow, fromCol, toRow, toCol);
        }
        else if (pieceKey.endsWith("King")) {
            return kingMove(fromRow, fromCol, toRow, toCol);
        }

        return false;
    }

    public static boolean putsOwnKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {

        String movingPiece = board[fromRow][fromCol];
        String capturedPiece = board[toRow][toCol];

        int oldWhiteRow = whiteKingRow;
        int oldWhiteCol = whiteKingCol;
        int oldBlackRow = blackKingRow;
        int oldBlackCol = blackKingCol;

        // temporary move
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = "--";

        if (movingPiece.equals("whiteKing")) {
            whiteKingRow = toRow;
            whiteKingCol = toCol;
        } else if (movingPiece.equals("blackKing")) {
            blackKingRow = toRow;
            blackKingCol = toCol;
        }

        boolean inCheck;

        if (whiteTurn) {
            inCheck = tileIsSeen(whiteKingRow, whiteKingCol, "enemy");
        } else {
            inCheck = tileIsSeen(blackKingRow, blackKingCol, "enemy");
        }

        // undo move
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;

        whiteKingRow = oldWhiteRow;
        whiteKingCol = oldWhiteCol;
        blackKingRow = oldBlackRow;
        blackKingCol = oldBlackCol;

        return inCheck;
    }

    public static boolean pawnMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {
        int direction;
        if (pieceKey.equals("whitePawn")) {
            direction = -1;

        } else {
            direction = 1;
        }

        if (board[toRow][toCol].equals("--")) {
            
            // Allow pawns to move 2 square for their first move, checking if there are any pieces in between 
            if (toCol == fromCol && ((pieceKey.equals("whitePawn") && fromRow == 6) 
                || (pieceKey.equals("blackPawn") && fromRow == 1)) 
                && toRow == fromRow + 2 * direction 
                && board[(toRow+fromRow)/2][toCol].equals("--")) {
                return true;
            } 

            return fromCol == toCol && toRow == fromRow+direction;
        }

        return Math.abs(fromCol-toCol)==1 && toRow == fromRow+direction;  // Diagonal capture
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
        return piece.startsWith("white");
    }

    public static boolean isBlack(String piece) {
        return piece.startsWith("black");
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

    public static boolean tileIsSeen(int kingRow, int kingCol, String player) {
        findAttackers(kingRow, kingCol, player);
        return !attacker.isEmpty();
    }

    public static String findAttackers(int kingRow, int kingCol, String seenBy) {

        System.out.println("Now looking for attackers");

        for (int[] dir : directions) {

            int row = kingRow + dir[0];
            int col = kingCol + dir[1];
            

            // Check if there is a piece in the direction being checked
            while (row >= 0 && row < 8 && col >= 0 && col < 8 && board[row][col].equals("--")) {
                row += dir[0];
                col += dir[1];
            }

            // Confrim that a piece was found and that the iteration did not go out of bounds
            if (row >= 0 && row < 8 &&
                col >= 0 && col < 8) {
                String piece = board[row][col];

                // Check if the piece can move to that tile
                if (validateMove(row, col, kingRow, kingCol, piece) 
                    && (!isFriendlyPiece(piece, board[kingRow][kingCol]) && seenBy.equals("enemy")
                    || isFriendlyPiece(piece, board[kingRow][kingCol]) && seenBy.equals("friend"))) {
                    //---------
                    
                    if (seenBy.equals("enemy")) {
                        System.out.print("attacker info gained: ");
                        if (!attacker.isEmpty()) {
                            additionalAttackers = true;
                        }
                        attacker = piece;
                        attackerRow = row;
                        attackerCol = col;
                        attackerDir = dir;
                        System.out.println(board[attackerRow][attackerCol]);

                        if (board[kingRow][kingCol].endsWith("King")) {
                            Rectangle rect = (Rectangle) tiles[kingRow][kingCol].getChildren().get(0);
                            rect.setFill(Color.RED);
                        }
                    };
                };
            }
        }


        System.out.println(attacker);
        return attacker;


    }
    
    public static boolean repelAttack(int kingRow, int kingCol) {

        System.out.println("lets check if the attack can be repelled");

        int row = kingRow+attackerDir[0];
        int col = kingCol+attackerDir[1];
        while (row != attackerRow+attackerDir[0]) {
            row += attackerDir[0];
            col += attackerDir[1];
            if (tileIsSeen(row, col, "friend")) {
                
                System.out.println("attacker info reset");
                attacker = "";
                attackerDir = null;
                attackerRow = -1;
                attackerCol = -1;
                additionalAttackers = false;
                
                return true;
            }
        }

        System.out.println("the attack cannot be repelled, king has to move");
        return false;

    }

    public static String kingInCheckmateOrStalemate(int kingRow, int kingCol) {

        System.out.println("This runs");

        for (int[] dir : directions) {

            int row = kingRow + dir[0];
            int col = kingCol + dir[1];
            boolean withinBoard = row >= 0 && row < 8 && col >= 0 && col < 8;

            if (withinBoard && validateMove(kingRow, kingCol, row, col, board[kingRow][kingCol]) 
                && !putsOwnKingInCheck(kingRow, kingCol, row, col)) {
                return "False";
            }
        } 

        System.out.println("We are checking for checkmate here");

        if (additionalAttackers || tileIsSeen(kingRow, kingCol, "enemy") && !repelAttack(kingRow, kingCol)) {
            
            System.out.println("Someone won");
            
            if (whiteTurn) {
                System.out.println("White Wins!");
                statusLabel.setText("White Wins!");
            } else {
                System.out.println("Black Wins!");
                statusLabel.setText("Black Wins!");
            }

            return "Checkmate";
        }

        //statusLabel.setText("Stalemate!");
        return "Stalemate";
    
    }   

    public static boolean insufficientMaterial() {
        return false;
    }
} 