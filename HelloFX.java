import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.HashMap;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {

        String[][] board = new String[8][8];

        // Black pieces
        board[0][0] = "br";
        board[0][1] = "bn";
        board[0][2] = "bb";
        board[0][3] = "bq";
        board[0][4] = "bk";
        board[0][5] = "bb";
        board[0][6] = "bn";
        board[0][7] = "br";

        // Black pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = "bp";
        }

        // Empty squares
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = "--";
            }
        }

        // White pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = "wp";
        }

        // White pieces
        board[7][0] = "wr";
        board[7][1] = "wn";
        board[7][2] = "wb";
        board[7][3] = "wq";
        board[7][4] = "wk";
        board[7][5] = "wb";
        board[7][6] = "wn";
        board[7][7] = "wr";

        HashMap<String, String> pieces = new HashMap<>();

        pieces.put("wk", "♔");
        pieces.put("wq", "♕");
        pieces.put("wr", "♖");
        pieces.put("wb", "♗");
        pieces.put("wn", "♘");
        pieces.put("wp", "♙");

        pieces.put("bk", "♚");
        pieces.put("bq", "♛");
        pieces.put("br", "♜");
        pieces.put("bb", "♝");
        pieces.put("bn", "♞");
        pieces.put("bp", "♟");

        Image image = new Image(
    ""
        );  

        GridPane grid = new GridPane();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle colour = new Rectangle(70, 70);
                if ((row+col)%2==0) {
                    colour.setFill(Color.WHITE);
                } else {
                    colour.setFill(Color.BLACK);
                }
                tile.getChildren().add(colour);

                // Add a piece if there is one
                String pieceCode = board[col][row];
                if (!pieceCode.equals("--")) {

                    Label piece = new Label(
                        pieces.get(pieceCode)
                    );

                    piece.setStyle("-fx-font-size: 40;");
                    tile.getChildren().add(piece);
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

}