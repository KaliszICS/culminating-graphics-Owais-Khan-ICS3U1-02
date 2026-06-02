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
    static HashMap<String, Image> pieces = new HashMap<>();
    static GridPane grid = new GridPane();
    static int selectedRow = -1;
    static int selectedCol = -1;


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

                // Add a piece if there is one
                String pieceCode = board[col][row];
                if (!pieceCode.equals("--")) {

                    ImageView piece = new ImageView(pieces.get(pieceCode));

                    piece.setFitWidth(60);
                    piece.setFitHeight(60);
                    piece.setPreserveRatio(true);

                    tile.getChildren().add(piece);

                    final int tileRow = row;
                    final int tileCol = col;

                    tile.setOnMouseClicked(event -> {
                        System.out.println("Clicked: " + tileRow + ", " + tileCol);
                        if (selectedRow == -1) {
                        // First click: select piece
                        selectedRow = tileRow;
                        selectedCol = tileCol;
                    } else {
                        // Second click: attempt move
                        movePiece(selectedRow, selectedCol, tileRow, tileCol);

                        selectedRow = -1;
                        selectedCol = -1;
                    }

                    });
                }

                grid.add(tile, row, col);
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
        int x;
    }

}