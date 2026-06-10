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
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;

public class HelloFX extends Application {

    static String[][] board = new String[8][8]; // Acsii board to keep track of game logic
    static StackPane[][] tiles; // Easy access to specific tiles
    static HashMap<String, Image> pieces = new HashMap<>();
    static GridPane grid = new GridPane(); // UI board
    static Label statusLabel;

    // Position of selected piece
    static int selectedRow;
    static int selectedCol;

    static boolean whiteTurn;

    // Keep track of kings positions for checking logic
    static int whiteKingRow;
    static int blackKingRow;
    static int whiteKingCol;
    static int blackKingCol;
    static boolean whiteInCheck;
    static boolean blackInCheck;

    // Directions of the king to check for checks
    static int[][] directions = {
            { -1, 0 }, // up
            { 1, 0 }, // down
            { 0, -1 }, // left
            { 0, 1 }, // right

            { -1, -1 }, // up-left
            { -1, 1 }, // up-right
            { 1, -1 }, // down-left
            { 1, 1 } // down-right
    };

    static int[][] knightDirections = {
            { 2, 1 },
            { 2, -1 },
            { -2, 1 },
            { -2, -1 },
            { 1, 2 },
            { 1, -2 },
            { -1, 2 },
            { -1, -2 }
    };

    static HashMap<String, Boolean> hasMoved = new HashMap<>();

    static {
        hasMoved.put("whiteKing", false);
        hasMoved.put("whiteRookA", false);
        hasMoved.put("whiteRookH", false);

        hasMoved.put("blackKing", false);
        hasMoved.put("blackRookA", false);
        hasMoved.put("blackRookH", false);
    }

    // Keep track of oppritunities for en passent
    static int enPassantRow = -1;
    static int enPassantCol = -1;
    static boolean lastMoveWasDoublePawn = false;

    @Override
    public void start(Stage stage) {

        stage.setTitle("2-Player Chess");

        // Load pieces
        pieces.put("whiteKing", new Image(getClass().getResourceAsStream("/icons/white-king.png")));
        pieces.put("whiteQueen", new Image(getClass().getResourceAsStream("/icons/white-queen.png")));
        pieces.put("whiteRook", new Image(getClass().getResourceAsStream("/icons/white-rook.png")));
        pieces.put("whiteBishop", new Image(getClass().getResourceAsStream("/icons/white-bishop.png")));
        pieces.put("whiteKnight", new Image(getClass().getResourceAsStream("/icons/white-knight.png")));
        pieces.put("whitePawn", new Image(getClass().getResourceAsStream("/icons/white-pawn.png")));

        pieces.put("blackKing", new Image(getClass().getResourceAsStream("/icons/black-king.png")));
        pieces.put("blackQueen", new Image(getClass().getResourceAsStream("/icons/black-queen.png")));
        pieces.put("blackRook", new Image(getClass().getResourceAsStream("/icons/black-rook.png")));
        pieces.put("blackBishop", new Image(getClass().getResourceAsStream("/icons/black-bishop.png")));
        pieces.put("blackKnight", new Image(getClass().getResourceAsStream("/icons/black-knight.png")));
        pieces.put("blackPawn", new Image(getClass().getResourceAsStream("/icons/black-pawn.png")));

        statusLabel = new Label("White to move");

        // MENU SCENE
        Label title = new Label("2-Player Chess");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button playButton = new Button("Play");
        Button quitButton = new Button("Quit");

        VBox menu = new VBox(20, title, playButton, quitButton);
        menu.setStyle("-fx-alignment: center; -fx-padding: 40;");

        Scene menuScene = new Scene(menu, 400, 300);

        // GAME SETUP (but not shown yet)
        setUpGame(); // builds grid

        Button surrenderButton = new Button("Surrender");
        Button drawButton = new Button("Offer Draw");

        surrenderButton.setOnAction(e -> {
            String winner = "WHITE WINS BY SURRENDER!";
            if (whiteTurn) {
                winner = "BLACK WINS BY SURRENDER!";
            }

            playAgain(winner, "Do you want to play again?");
        });

        drawButton.setOnAction(e -> {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Draw Offer");
            confirm.setHeaderText("Opponent: Do you accept the draw?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    playAgain("DRAW!", "Do you want to play again?");
                }
            });
        });

        HBox controls = new HBox(10, surrenderButton, drawButton);
        controls.setAlignment(Pos.CENTER);

        VBox rightPanel = new VBox(15, statusLabel, controls);
        rightPanel.setAlignment(Pos.TOP_CENTER);

        VBox boardContainer = new VBox(grid);
        boardContainer.setAlignment(Pos.CENTER);

        HBox gameLayout = new HBox(20, boardContainer, rightPanel);
        gameLayout.setAlignment(Pos.CENTER);

        Scene gameScene = new Scene(gameLayout);

        // BUTTON ACTIONS
        playButton.setOnAction(e -> {
            setUpGame();              // reset board
            stage.setScene(gameScene);
        });

        quitButton.setOnAction(e -> stage.close());

        // start on menu
        stage.setScene(menuScene);
        stage.show();
    }

    public static void setUpGame() {

        tiles = new StackPane[8][8];

        // Position of selected piece
        selectedRow = -1;
        selectedCol = -1;

        whiteTurn = true;

        // Keep track of kings positions for checking logic
        whiteKingRow = 7;
        blackKingRow = 0;
        whiteKingCol = 4;
        blackKingCol = 4;
        whiteInCheck = false;
        blackInCheck = false;

        boardSetup();
        grid.getChildren().clear();

        // Setup UI board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = new StackPane();
                Rectangle colour = new Rectangle(70, 70);

                // Checker pattern
                if ((row + col) % 2 == 0) {
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
                        rect.setFill(Color.LIGHTBLUE); // Indication of selected tile

                    } else if (selectedRow != -1) {

                        // Second click: attempt move
                        movePiece(selectedRow, selectedCol, tileRow, tileCol);
                        StackPane selectedTile = tiles[selectedRow][selectedCol];
                        Rectangle rect = (Rectangle) selectedTile.getChildren().get(0);

                        // Remove indication of selected tile
                        if ((selectedRow + selectedCol) % 2 == 0) {
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
        statusLabel.setText("White to move");
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

        if (tileIsVisible(whiteKingRow, whiteKingCol, false)) {
            highlightCheckKing(whiteKingRow, whiteKingCol);
        }

        if (tileIsVisible(blackKingRow, blackKingCol, true)) {
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

        if (validateMove(fromRow, fromCol, toRow, toCol, pieceKey, false)
                && !putsOwnKingInCheck(fromRow, fromCol, toRow, toCol)) {

            // Move the piece in the ascii board (logic)
            updateSquares(fromRow, fromCol, toRow, toCol, pieceKey);
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = "--";

            if (pieceKey.equals("whitePawn") && toRow == 0) {
                handlePromotion(toRow, toCol, true);
            }

            if (pieceKey.equals("blackPawn") && toRow == 7) {
                handlePromotion(toRow, toCol, false);
            }

            whiteTurn = !whiteTurn;
            if (whiteTurn) {
                statusLabel.setText("white to move");
            } else {
                statusLabel.setText("black to move");
            }

            // Keep track of king positions
            if (pieceKey.equals("whiteKing")) {
                hasMoved.put("whiteKing", true);
                whiteKingRow = toRow;
                whiteKingCol = toCol;
            } else if (pieceKey.equals("blackKing")) {
                hasMoved.put("blackKing", true);
                blackKingRow = toRow;
                blackKingCol = toCol;
            }

            if (pieceKey.equals("whiteRook") && fromCol == 0)
                hasMoved.put("whiteRookA", true);
            if (pieceKey.equals("whiteRook") && fromCol == 7)
                hasMoved.put("whiteRookH", true);
            if (pieceKey.equals("blackRook") && fromCol == 0)
                hasMoved.put("blackRookA", true);
            if (pieceKey.equals("blackRook") && fromCol == 7)
                hasMoved.put("blackRookH", true);

            // WHITE CASTLING
            if (pieceKey.equals("whiteKing") && fromRow == 7 && fromCol == 4) {

                // king side
                if (toRow == 7 && toCol == 6) {
                    System.out.println("CASTLE!");
                    board[7][5] = "whiteRook";
                    board[7][7] = "--";
                    castleRookMove(7, 7, 7, 5, "whiteRook");
                }

                // queen side
                if (toRow == 7 && toCol == 2) {
                    System.out.println("CASTLE!");
                    board[7][3] = "whiteRook";
                    board[7][0] = "--";
                    castleRookMove(7, 0, 7, 3, "whiteRook");
                }
            }

            // BLACK CASTLING
            if (pieceKey.equals("blackKing") && fromRow == 0 && fromCol == 4) {

                // king side
                if (toRow == 0 && toCol == 6) {
                    board[0][5] = "blackRook";
                    board[0][7] = "--";
                    castleRookMove(0, 7, 0, 5, "blackRook");
                }

                // queen side
                if (toRow == 0 && toCol == 2) {
                    board[0][3] = "blackRook";
                    board[0][0] = "--";
                    castleRookMove(0, 0, 0, 3, "blackRook");
                }
            }

            // En Passant logic
            if (pieceKey.equals("whitePawn")
            && toRow == enPassantRow
            && toCol == enPassantCol
            && fromRow == 3) {
                System.out.println("Attempt enPassent");

                board[3][toCol] = "--";
                tiles[3][toCol].getChildren().remove(tiles[3][toCol].getChildren().size() - 1);
            }

            if (pieceKey.equals("blackPawn")
                    && toRow == enPassantRow
                    && toCol == enPassantCol
                    && fromRow == 4) {
                
                System.out.println("Attempt enPassent");
                board[4][toCol] = "--";
                tiles[4][toCol].getChildren().remove(tiles[4][toCol].getChildren().size() - 1);
            }

            if (kingInCheckmateOrStalemate(whiteKingRow, whiteKingCol, false).equals("CHECKMATE")) {
                playAgain("BLACK WINS BY CHECKMATE!", "Do you want to play again?");

            } else if(kingInCheckmateOrStalemate(blackKingRow, blackKingCol, true).equals("CHECKMATE")){
                playAgain("WHITE WINS BY CHECKMATE!", "Do you want to play again?");
            } else if (kingInCheckmateOrStalemate(blackKingRow, blackKingCol, true).equals("STALEMATE")
                       || kingInCheckmateOrStalemate(whiteKingRow, whiteKingCol, false).equals("STALEMATE")) {
                playAgain("STALEMATE!", "Do you want to play again?");
            }

            updateCheckIndicators();
        }

        lastMoveWasDoublePawn = false;
        enPassantRow = -1;
        enPassantCol = -1;
    }

    public static void playAgain(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        ButtonType playAgain = new ButtonType("Play Again?");
        alert.getButtonTypes().setAll(playAgain);
        ButtonType choice = alert.showAndWait().orElse(playAgain);
        if (choice == playAgain) {
            setUpGame();
        }
    }

    public static void handlePromotion(int row, int col, boolean white) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pawn Promotion");
        alert.setHeaderText("Choose a piece to promote to:");

        ButtonType queen = new ButtonType("Queen");
        ButtonType rook = new ButtonType("Rook");
        ButtonType bishop = new ButtonType("Bishop");
        ButtonType knight = new ButtonType("Knight");

        alert.getButtonTypes().setAll(queen, rook, bishop, knight);

        ButtonType choice = alert.showAndWait().orElse(queen);

        String color = "black";
        if (white) {
            color = "white";
        }
        String newPiece = color;

        if (choice == queen) {
            newPiece += "Queen";
        } else if (choice == rook) {
            newPiece += "Rook";
        } else if (choice == bishop) {
            newPiece += "Bishop";
        } else if (choice == knight) {
            newPiece += "Knight";
        }

        // update logic board
        board[row][col] = newPiece;

        // update UI
        ImageView piece = new ImageView(pieces.get(newPiece));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setPreserveRatio(true);

        StackPane tile = tiles[row][col];

        // remove old piece image
        tile.getChildren().remove(tile.getChildren().size() - 1);

        tile.getChildren().add(piece);
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

    public static void castleRookMove(int rookFromRow, int rookFromCol, int rookToRow, int rookToCol, String rookKey) {

        StackPane startTile = tiles[rookFromRow][rookFromCol];
        StackPane endTile = tiles[rookToRow][rookToCol];

        // update board already handled outside

        ImageView rook = new ImageView(pieces.get(rookKey));
        rook.setFitWidth(60);
        rook.setFitHeight(60);
        rook.setPreserveRatio(true);

        // remove rook image from original square
        startTile.getChildren().remove(startTile.getChildren().size() - 1);

        // place rook on new square
        endTile.getChildren().add(rook);
    }

    public static boolean validateMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey,
            boolean checkingPossibleMove) {
        if (isFriendlyPiece(board[fromRow][fromCol], board[toRow][toCol])) {
            return false;
        } else if (pieceKey.contains("Pawn")) {
            return pawnMove(fromRow, fromCol, toRow, toCol, pieceKey);
        } else if (pieceKey.endsWith("Knight")) {
            return knightMove(fromRow, fromCol, toRow, toCol);
        } else if (pieceKey.endsWith("Bishop")) {
            return bishopMove(fromRow, fromCol, toRow, toCol);
        } else if (pieceKey.contains("Rook")) {
            return rookMove(fromRow, fromCol, toRow, toCol);
        } else if (pieceKey.endsWith("Queen")) {
            return rookMove(fromRow, fromCol, toRow, toCol)
                    || bishopMove(fromRow, fromCol, toRow, toCol);
        } else if (pieceKey.endsWith("King")) {
            return kingMove(fromRow, fromCol, toRow, toCol, checkingPossibleMove);
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
            inCheck = tileIsVisible(whiteKingRow, whiteKingCol, false);
        } else {
            inCheck = tileIsVisible(blackKingRow, blackKingCol, true);
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

        // normal forward move
        if (board[toRow][toCol].equals("--")) {

            if (toCol == fromCol
                    && ((pieceKey.equals("whitePawn") && fromRow == 6)
                    || (pieceKey.equals("blackPawn") && fromRow == 1))
                    && toRow == fromRow + 2 * direction
                    && board[(toRow + fromRow) / 2][toCol].equals("--")) {

                enPassantCol = toCol;
                enPassantRow = toRow + direction;
                lastMoveWasDoublePawn = true;
                return true;
            }

            return fromCol == toCol && toRow == fromRow + direction;
        }

        // EN PASSANT
        if (Math.abs(fromCol - toCol) == 1
                && toRow == fromRow + direction
                && board[toRow][toCol].equals("--")) {

            System.out.println("About to check it lands next to an enpassent ROW/COL");

            if (toRow == enPassantRow && toCol == enPassantCol) {
                return true;
            }
        }

        // normal diagonal capture
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction) {
            return true;
        }

        return false;
    }

    public static boolean knightMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean senario1 = Math.abs(fromCol - toCol) == 2 && Math.abs(fromRow - toRow) == 1;
        boolean senario2 = Math.abs(fromCol - toCol) == 1 && Math.abs(fromRow - toRow) == 2;
        return senario1 || senario2;
    }

    public static boolean bishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toCol - fromCol) == Math.abs(toRow - fromRow)
                && noPiecesDiagonal(fromCol, toCol, toRow, fromRow);
    }

    public static boolean rookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (toCol == fromCol && noPiecesHorizontal(fromCol, toRow, fromRow)
                || toRow == fromRow && noPiecesVertical(fromRow, toCol, fromCol));
    }

    public static boolean kingMove(int fromRow, int fromCol, int toRow, int toCol, boolean checkingPossibleMove) {
        int colDiff = toCol - fromCol;
        int rowDiff = Math.abs(toRow - fromRow);

        // normal king move
        if (Math.abs(colDiff) <= 1 && rowDiff <= 1) {
            return true;
        }

        if (checkingPossibleMove) {
            return false;
        }

        // CASTLING ONLY (same row)
        if (rowDiff == 0 && Math.abs(colDiff) == 2) {

            // WHITE
            if (fromRow == 7 && !hasMoved.get("whiteKing")) {

                // king side
                if (board[7][7].equals("whiteRook") && toCol == 6 && !hasMoved.get("whiteRookH") &&
                        board[7][5].equals("--") && !tileIsVisible(7, 5, false) &&
                        board[7][6].equals("--") && !tileIsVisible(7, 6, false)) {
                    return true;
                }

                // queen side
                if (board[7][0].equals("whiteRook") && toCol == 2 && !hasMoved.get("whiteRookA") &&
                        board[7][1].equals("--") && !tileIsVisible(7, 1, false) &&
                        board[7][2].equals("--") && !tileIsVisible(7, 2, false) &&
                        board[7][3].equals("--") && !tileIsVisible(7, 3, false)) {
                    return true;
                }
            }

            // BLACK
            if (fromRow == 0 && !hasMoved.get("blackKing")) {

                // king side
                if (board[0][7].equals("blackRook") && toCol == 6 && !hasMoved.get("blackRookH") &&
                        board[0][5].equals("--") && !tileIsVisible(0, 5, true) &&
                        board[0][6].equals("--") && !tileIsVisible(0, 6, true)) {
                    return true;
                }

                // queen side
                if (board[0][0].equals("blackRook") && toCol == 2 && !hasMoved.get("blackRookA") &&
                        board[0][1].equals("--") && !tileIsVisible(0, 1, true) &&
                        board[0][2].equals("--") && !tileIsVisible(0, 2, true) &&
                        board[0][3].equals("--") && !tileIsVisible(0, 3, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isWhite(String piece) {
        return piece.startsWith("white");
    }

    public static boolean isBlack(String piece) {
        return piece.startsWith("black");
    }

    public static boolean isFriendlyPiece(String source, String destination) {

        if (destination.equals("--") || source.equals("--")) {
            return false;
        }

        return (isWhite(source) && isWhite(destination))
                || (isBlack(source) && isBlack(destination));
    }

    public static boolean noPiecesVertical(int fromRow, int toCol, int fromCol) {
        int start = Math.min(fromCol, toCol);
        int end = Math.max(fromCol, toCol);

        for (int col = start + 1; col < end; col++) {
            if (!board[fromRow][col].equals("--")) {
                return false;
            }
        }
        return true;
    }

    public static boolean noPiecesHorizontal(int fromCol, int toRow, int fromRow) {
        int start = Math.min(fromRow, toRow);
        int end = Math.max(fromRow, toRow);

        for (int row = start + 1; row < end; row++) {
            if (!board[row][fromCol].equals("--")) {
                return false;
            }
        }
        return true;
    }

    public static boolean noPiecesDiagonal(int fromCol, int toCol, int toRow, int fromRow) {
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
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

    public static boolean tileIsVisible(int tileRow, int tileCol, boolean byWhite) {

        System.out.println("Now looking for attackers");

        for (int[] dir : directions) {

            int row = tileRow + dir[0];
            int col = tileCol + dir[1];

            // Check if there is a piece in the direction being checked
            while (row >= 0 && row < 8 && col >= 0 && col < 8 && board[row][col].equals("--")) {
                row += dir[0];
                col += dir[1];
            }

            // Confrim that a piece was found and that the iteration did not go out of
            // bounds
            if (row >= 0 && row < 8 &&
                    col >= 0 && col < 8) {
                String piece = board[row][col];

                // Check if the piece can move to that tile
                if (validateMove(row, col, tileRow, tileCol, piece, true)
                    && ((byWhite && isWhite(piece)) || (!byWhite && isBlack(piece)))) {

                    System.out.println(
                            "Attacker found: " + piece +
                            " at (" + row + "," + col + ")" +
                            " attacking (" + tileRow + "," + tileCol + ")");
                    return true;
                }
            }
        }

        for (int[] dir : knightDirections) {
            int row = tileRow + dir[0];
            int col = tileCol + dir[1];

            boolean withinBoard = false;
            String piece = "";

            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                withinBoard = true;
                piece = board[row][col];
            }

            if (withinBoard && piece.endsWith("Knight") && validateMove(row, col, tileRow, tileCol, piece, true)
                && ((byWhite && isWhite(piece)) || (!byWhite && isBlack(piece)))) {

                    return true;
            }

        }

        return false;
    }

    public static String kingInCheckmateOrStalemate(int tileRow, int tileCol, boolean byWhite) {
        if (!currentPlayerHasLegalMoves()) {
            if (tileIsVisible(tileRow, tileCol, byWhite)) {
                return "CHECKMATE";
            } else {
                return "STALEMATE";
            }
        }
        return "NVM";
    }

    public static boolean currentPlayerHasLegalMoves() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];

                if ((whiteTurn && isWhite(piece) 
                    || !whiteTurn && isBlack(piece)) 
                    &&  hasLegalMoves(row, col, piece)) {
                    return true;

                }
            }
        }
        return false;
    }

    public static boolean hasLegalMoves(int pieceRow, int pieceCol, String piece) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (validateMove(pieceRow, pieceCol, row, col, piece, true) && !putsOwnKingInCheck(pieceRow, pieceCol, row, col)) {
                    return true;
                }
            }
        }
        return false;
    }



//garbage pieceCode
//System.out.println("This runs");

        // boolean hasEscapeMove = false;

        // String kingPiece = board[tileRow][tileCol];

        // // Try king moves
        // for (int[] dir : directions) {

        //     int row = tileRow + dir[0];
        //     int col = tileCol + dir[1];

        //     if (row >= 0 && row < 8 && col >= 0 && col < 8) {

        //         if (validateMove(tileRow, tileCol, row, col, kingPiece, true)
        //                 && !putsOwnKingInCheck(tileRow, tileCol, row, col)) {

        //             hasEscapeMove = true;
        //         }
        //     }
        // }

        // if (hasEscapeMove) {
        //     return "Ongoing";
        // }

        // boolean inCheck = tileIsVisible(tileRow, tileCol, "enemy");

        // if (inCheck) {
        //     if (whiteTurn) {
        //         statusLabel.setText("Black Wins!");
        //     } else {
        //         statusLabel.setText("White Wins!");
        //     }
        //     return "Checkmate";
        // } else {
        //     statusLabel.setText("Stalemate!");
        //     return "Stalemate";
        // }
        // public static boolean repelAttack(int tileRow, int tileCol) {

        // System.out.println("lets check if the attack can be repelled");

        // int row = tileRow + attackerDir[0];
        // int col = tileCol + attackerDir[1];
        // while (row != attackerRow + attackerDir[0]) {

        //     boolean withinBoard = row >= 0 && row < 8 && col >= 0 && col < 8;

        //     if (withinBoard && tileIsVisible(row, col, "friend")) {

        //         System.out.println("attacker info reset");
        //         attacker = "";
        //         attackerDir = null;
        //         attackerRow = -1;
        //         attackerCol = -1;
        //         additionalAttackers = false;

        //         return true;
        //     }
        //     System.out.println("Checking block square: " + row + "," + col);
        //     System.out.println("Seen by friend: " + tileIsVisible(row, col, "friend"));

        //     row += attackerDir[0];
        //     col += attackerDir[1];
        // }

        // System.out.println("the attack cannot be repelled, king has to move");
        // return false;

    }
