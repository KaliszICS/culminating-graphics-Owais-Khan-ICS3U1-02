/**

    * File: Culminating

    * Author: Owais Ali Khan

    * Date Created: June 1, 2026

    * Date Last Modified: June 10, 2026

    */

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
import java.util.ArrayList;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;

public class HelloFX extends Application {

    static String[][] board = new String[8][8]; // ASCII board for game logic
    static StackPane[][] tiles; // UI tiles for easy access
    static HashMap<String, Image> pieces = new HashMap<>();
    static GridPane grid = new GridPane(); // chess board UI
    static Label statusLabel;  // shows turn

    // selected piece position
    static int selectedRow;
    static int selectedCol;

    static boolean whiteTurn;

    // king positions for check logic
    static int whiteKingRow;
    static int blackKingRow;
    static int whiteKingCol;
    static int blackKingCol;
    static boolean whiteInCheck;
    static boolean blackInCheck;

    // directions used for king/line-of-sight checking
    static int[][] directions = {
            { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
            { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
    };

    // knight move patterns
    static int[][] knightDirections = {
            { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
            { 1, 2 }, { 1, -2 }, { -1, 2 }, { -1, -2 }
    };

    // track moved pieces for castling rules
    static HashMap<String, Boolean> hasMoved = new HashMap<>();
    static {
        hasMoved.put("whiteKing", false);
        hasMoved.put("whiteRookA", false);
        hasMoved.put("whiteRookH", false);

        hasMoved.put("blackKing", false);
        hasMoved.put("blackRookA", false);
        hasMoved.put("blackRookH", false);
    }

    // en passant tracking
    static int enPassantRow = -1;
    static int enPassantCol = -1;
    static boolean lastMoveWasDoublePawn = false;

    // remaining pieces (used for insufficient material check)
    static ArrayList<String> remainingPieces = new ArrayList<>();

    @Override
    public void start(Stage stage) {

        stage.setTitle("2-Player Chess");

        // load piece images
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

        // menu UI
        Label title = new Label("2-Player Chess");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button playButton = new Button("Play");
        Button quitButton = new Button("Quit");

        VBox menu = new VBox(20, title, playButton, quitButton);
        menu.setStyle("-fx-alignment: center; -fx-padding: 40;");

        Scene menuScene = new Scene(menu, 400, 300);

        // setup board but don't show yet
        setUpGame();

        Button surrenderButton = new Button("Surrender");
        Button drawButton = new Button("Offer Draw");

        // surrender ends game immediately
        surrenderButton.setOnAction(e -> {
            String winner = "WHITE WINS BY SURRENDER!";
            if (whiteTurn) {
                winner = "BLACK WINS BY SURRENDER!";
            }
            playAgain(winner, "Do you want to play again?");
        });

        // draw offer confirmation
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

        // start game
        playButton.setOnAction(e -> {
            setUpGame();
            stage.setScene(gameScene);
        });

        quitButton.setOnAction(e -> stage.close());

        stage.setScene(menuScene);
        stage.show();
    }

    // reset board + variables
    public static void setUpGame() {

        tiles = new StackPane[8][8];

        selectedRow = -1;
        selectedCol = -1;

        whiteTurn = true;

        // initial king positions
        whiteKingRow = 7;
        blackKingRow = 0;
        whiteKingCol = 4;
        blackKingCol = 4;

        whiteInCheck = false;
        blackInCheck = false;

        // reset en passant
        lastMoveWasDoublePawn = false;
        enPassantRow = -1;
        enPassantCol = -1;

        boardSetup();
        grid.getChildren().clear();

        remainingPieces.clear();

        // track remaining pieces for draw detection
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!board[row][col].equals("--")) {
                    remainingPieces.add(board[row][col]);
                }
            }
        }

        // build UI board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                StackPane tile = new StackPane();
                Rectangle colour = new Rectangle(70, 70);

                // chess board colors
                if ((row + col) % 2 == 0) {
                    colour.setFill(Color.BEIGE);
                } else {
                    colour.setFill(Color.SADDLEBROWN);
                }

                tile.getChildren().add(colour);

                final int tileRow = row;
                final int tileCol = col;

                tile.setOnMouseClicked(event -> {

                    // first click selects piece
                    if (selectedRow == -1 && !board[tileRow][tileCol].equals("--")
                            && (whiteTurn && isWhite(board[tileRow][tileCol])
                            || !whiteTurn && isBlack(board[tileRow][tileCol]))) {

                        selectedRow = tileRow;
                        selectedCol = tileCol;

                        Rectangle rect = (Rectangle) tile.getChildren().get(0);
                        rect.setFill(Color.LIGHTBLUE);

                    } 
                    // second click tries move
                    else if (selectedRow != -1) {

                        movePiece(selectedRow, selectedCol, tileRow, tileCol);

                        StackPane selectedTile = tiles[selectedRow][selectedCol];
                        Rectangle rect = (Rectangle) selectedTile.getChildren().get(0);

                        // reset tile color
                        if ((selectedRow + selectedCol) % 2 == 0) {
                            rect.setFill(Color.BEIGE);
                        } else {
                            rect.setFill(Color.SADDLEBROWN);
                        }

                        selectedRow = -1;
                        selectedCol = -1;
                    }
                });

                // add piece image if present
                String pieceCode = board[row][col];
                if (!pieceCode.equals("--")) {
                    ImageView piece = new ImageView(pieces.get(pieceCode));
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

    // resets a tile color back to normal
    public static void resetTileColor(int row, int col) {
        Rectangle rect = (Rectangle) tiles[row][col].getChildren().get(0);

        if ((row + col) % 2 == 0) {
            rect.setFill(Color.BEIGE);
        } else {
            rect.setFill(Color.SADDLEBROWN);
        }
    }

    // highlights king in check
    public static void highlightCheckKing(int row, int col) {
        Rectangle rect = (Rectangle) tiles[row][col].getChildren().get(0);
        rect.setFill(Color.RED);
    }

    // updates check indicators for both kings
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

    // initial board setup
    public static void boardSetup() {

        // black pieces
        board[0][0] = "blackRook";
        board[0][1] = "blackKnight";
        board[0][2] = "blackBishop";
        board[0][3] = "blackQueen";
        board[0][4] = "blackKing";
        board[0][5] = "blackBishop";
        board[0][6] = "blackKnight";
        board[0][7] = "blackRook";

        // black pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = "blackPawn";
        }

        // empty middle
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = "--";
            }
        }

        // white pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = "whitePawn";
        }

        // white pieces
        board[7][0] = "whiteRook";
        board[7][1] = "whiteKnight";
        board[7][2] = "whiteBishop";
        board[7][3] = "whiteQueen";
        board[7][4] = "whiteKing";
        board[7][5] = "whiteBishop";
        board[7][6] = "whiteKnight";
        board[7][7] = "whiteRook";
    }

    // handles moving a piece and game rules
    public static void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
                String pieceKey = board[fromRow][fromCol];

        // only move if valid and doesn't put own king in check
        if (validateMove(fromRow, fromCol, toRow, toCol, pieceKey, false)
                && !putsOwnKingInCheck(fromRow, fromCol, toRow, toCol)) {

            // update UI + logic board
            updateSquares(fromRow, fromCol, toRow, toCol, pieceKey);
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = "--";

            // pawn promotion (white)
            if (pieceKey.equals("whitePawn") && toRow == 0) {
                handlePromotion(toRow, toCol, true);
            }

            // pawn promotion (black)
            if (pieceKey.equals("blackPawn") && toRow == 7) {
                handlePromotion(toRow, toCol, false);
            }

            // switch turns
            whiteTurn = !whiteTurn;

            if (whiteTurn) {
                statusLabel.setText("white to move");
            } else {
                statusLabel.setText("black to move");
            }

            // update king position tracking
            if (pieceKey.equals("whiteKing")) {
                hasMoved.put("whiteKing", true);
                whiteKingRow = toRow;
                whiteKingCol = toCol;
            } else if (pieceKey.equals("blackKing")) {
                hasMoved.put("blackKing", true);
                blackKingRow = toRow;
                blackKingCol = toCol;
            }

            // track rook movement for castling
            if (pieceKey.equals("whiteRook") && fromCol == 0)
                hasMoved.put("whiteRookA", true);
            if (pieceKey.equals("whiteRook") && fromCol == 7)
                hasMoved.put("whiteRookH", true);
            if (pieceKey.equals("blackRook") && fromCol == 0)
                hasMoved.put("blackRookA", true);
            if (pieceKey.equals("blackRook") && fromCol == 7)
                hasMoved.put("blackRookH", true);

            // white castling logic
            if (pieceKey.equals("whiteKing") && fromRow == 7 && fromCol == 4) {

                // king side castling
                if (toRow == 7 && toCol == 6) {
                    board[7][5] = "whiteRook";
                    board[7][7] = "--";
                    castleRookMove(7, 7, 7, 5, "whiteRook");
                }

                // queen side castling
                if (toRow == 7 && toCol == 2) {
                    board[7][3] = "whiteRook";
                    board[7][0] = "--";
                    castleRookMove(7, 0, 7, 3, "whiteRook");
                }
            }

            // black castling logic
            if (pieceKey.equals("blackKing") && fromRow == 0 && fromCol == 4) {

                // king side castling
                if (toRow == 0 && toCol == 6) {
                    board[0][5] = "blackRook";
                    board[0][7] = "--";
                    castleRookMove(0, 7, 0, 5, "blackRook");
                }

                // queen side castling
                if (toRow == 0 && toCol == 2) {
                    board[0][3] = "blackRook";
                    board[0][0] = "--";
                    castleRookMove(0, 0, 0, 3, "blackRook");
                }
            }

            // en passant (white capture)
            if (pieceKey.equals("whitePawn")
            && toRow == enPassantRow
            && toCol == enPassantCol
            && fromRow == 3) {

                board[3][toCol] = "--";
                tiles[3][toCol].getChildren().remove(tiles[3][toCol].getChildren().size() - 1);
            }

            // en passant (black capture)
            if (pieceKey.equals("blackPawn")
                    && toRow == enPassantRow
                    && toCol == enPassantCol
                    && fromRow == 4) {

                board[4][toCol] = "--";
                tiles[4][toCol].getChildren().remove(tiles[4][toCol].getChildren().size() - 1);
            }

            // check game state after move
            String whiteGameState = kingInCheckmateOrStalemate(whiteKingRow, whiteKingCol, false);
            String blackGameState = kingInCheckmateOrStalemate(blackKingRow, blackKingCol, true);

            // checkmate / stalemate / draw conditions
            if (whiteGameState.equals("CHECKMATE")) {
                playAgain("BLACK WINS BY CHECKMATE!", "Do you want to play again?");

            } else if (blackGameState.equals("CHECKMATE")) {
                playAgain("WHITE WINS BY CHECKMATE!", "Do you want to play again?");

            } else if (blackGameState.equals("STALEMATE")
                    || whiteGameState.equals("STALEMATE")) {
                playAgain("STALEMATE!", "Do you want to play again?");

            } else if (whiteGameState.equals("INSUFFICIENT-MATERIAL")
                    || blackGameState.equals("INSUFFICIENT-MATERIAL")) {
                playAgain("STALEMATE BY INSUFFICIENT MATERIAL!", "Do you want to play again?");
            }

            // update check highlights
            updateCheckIndicators();

            // reset en passant state
            lastMoveWasDoublePawn = false;
            enPassantRow = -1;
            enPassantCol = -1;
        }
    }

    // restart prompt
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

    // pawn promotion menu
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

        // update UI piece
        ImageView piece = new ImageView(pieces.get(newPiece));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setPreserveRatio(true);

        StackPane tile = tiles[row][col];

        // remove old piece image
        tile.getChildren().remove(tile.getChildren().size() - 1);

        tile.getChildren().add(piece);

        // update remaining pieces
        remainingPieces.remove(color + "Pawn");
        remainingPieces.add(newPiece);
    }

    // updates UI after a move
    public static void updateSquares(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {

        StackPane startTile = tiles[fromRow][fromCol];
        StackPane endTile = tiles[toRow][toCol];
        String destination = board[toRow][toCol];

        ImageView piece = new ImageView(pieces.get(pieceKey));
        piece.setFitWidth(60);
        piece.setFitHeight(60);
        piece.setPreserveRatio(true);

        // remove piece from start tile
        if (!startTile.getChildren().isEmpty()) {
            startTile.getChildren().remove(startTile.getChildren().size() - 1);
        }

        // remove captured piece if present
        if (destination != null && !destination.equals("--")) {
            if (!endTile.getChildren().isEmpty()) {
                endTile.getChildren().remove(endTile.getChildren().size() - 1);
            }
        }

        // place piece on new tile
        endTile.getChildren().add(piece);

        // reset en passant state
        enPassantRow = -1;
        enPassantCol = -1;
        lastMoveWasDoublePawn = false;

        // detect double pawn move for en passant
        if (pieceKey.equals("whitePawn") || pieceKey.equals("blackPawn")) {

            if (Math.abs(toRow - fromRow) == 2) {
                enPassantRow = (fromRow + toRow) / 2;
                enPassantCol = fromCol;
                lastMoveWasDoublePawn = true;
            }
        }
    };

    // move rook during castling
    public static void castleRookMove(int rookFromRow, int rookFromCol, int rookToRow, int rookToCol, String rookKey) {

        StackPane startTile = tiles[rookFromRow][rookFromCol];
        StackPane endTile = tiles[rookToRow][rookToCol];

        ImageView rook = new ImageView(pieces.get(rookKey));
        rook.setFitWidth(60);
        rook.setFitHeight(60);
        rook.setPreserveRatio(true);

        // remove rook from old position
        startTile.getChildren().remove(startTile.getChildren().size() - 1);

        // place rook in new position
        endTile.getChildren().add(rook);
    }

    // checks if move is valid for a piece type
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

    // checks if move leaves king in check
    public static boolean putsOwnKingInCheck(int fromRow, int fromCol, int toRow, int toCol) {

        String movingPiece = board[fromRow][fromCol];
        String capturedPiece = board[toRow][toCol];

        int oldWhiteRow = whiteKingRow;
        int oldWhiteCol = whiteKingCol;
        int oldBlackRow = blackKingRow;
        int oldBlackCol = blackKingCol;

        // simulate move
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = "--";

        // update king position if needed
        if (movingPiece.equals("whiteKing")) {
            whiteKingRow = toRow;
            whiteKingCol = toCol;
        } else if (movingPiece.equals("blackKing")) {
            blackKingRow = toRow;
            blackKingCol = toCol;
        }

        boolean inCheck;

        // check opponent attack on king
        if (whiteTurn) {
            inCheck = tileIsVisible(whiteKingRow, whiteKingCol, false);
        } else {
            inCheck = tileIsVisible(blackKingRow, blackKingCol, true);
        }

        // undo simulation
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = capturedPiece;

        whiteKingRow = oldWhiteRow;
        whiteKingCol = oldWhiteCol;
        blackKingRow = oldBlackRow;
        blackKingCol = oldBlackCol;

        return inCheck;
    }

    // pawn movement rules
    public static boolean pawnMove(int fromRow, int fromCol, int toRow, int toCol, String pieceKey) {

        int direction;
        if (pieceKey.equals("whitePawn")) {
            direction = -1;
        } else {
            direction = 1;
        }

        // en passant capture
        if (Math.abs(fromCol - toCol) == 1
                && toRow == fromRow + direction
                && board[toRow][toCol].equals("--")) {

            if (toRow == enPassantRow && toCol == enPassantCol) {
                return true;
            }
        }

        // forward move
        if (board[toRow][toCol].equals("--")) {

            // double pawn move from starting position
            if (toCol == fromCol
                    && ((pieceKey.equals("whitePawn") && fromRow == 6)
                    || (pieceKey.equals("blackPawn") && fromRow == 1))
                    && toRow == fromRow + 2 * direction
                    && board[(toRow + fromRow) / 2][toCol].equals("--")) {

                return true;
            }

            return fromCol == toCol && toRow == fromRow + direction;
        }

        // diagonal capture
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction) {
            return true;
        }

        return false;
    }

    // knight move check
    public static boolean knightMove(int fromRow, int fromCol, int toRow, int toCol) {
        boolean senario1 = Math.abs(fromCol - toCol) == 2 && Math.abs(fromRow - toRow) == 1;
        boolean senario2 = Math.abs(fromCol - toCol) == 1 && Math.abs(fromRow - toRow) == 2;
        return senario1 || senario2;
    }

    // bishop move check
    public static boolean bishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toCol - fromCol) == Math.abs(toRow - fromRow)
                && noPiecesDiagonal(fromCol, toCol, toRow, fromRow);
    }

    // rook move check
    public static boolean rookMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (toCol == fromCol && noPiecesHorizontal(fromCol, toRow, fromRow)
                || toRow == fromRow && noPiecesVertical(fromRow, toCol, fromCol));
    }

    // king move + castling
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

        // castling
        if (rowDiff == 0 && Math.abs(colDiff) == 2) {

            // white castling
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

            // black castling
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

    // check if piece is white
    public static boolean isWhite(String piece) {
        return piece.startsWith("white");
    }

    // check if piece is black
    public static boolean isBlack(String piece) {
        return piece.startsWith("black");
    }

    // check if two pieces are same color
    public static boolean isFriendlyPiece(String source, String destination) {

        if (destination.equals("--") || source.equals("--")) {
            return false;
        }

        return (isWhite(source) && isWhite(destination))
                || (isBlack(source) && isBlack(destination));
    }

    // check clear vertical path
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

    // check clear horizontal path
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

    // check clear diagonal path
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

    // checks if a tile is attacked
    public static boolean tileIsVisible(int tileRow, int tileCol, boolean byWhite) {

        // check sliding pieces (rook/bishop/queen paths)
        for (int[] dir : directions) {

            int row = tileRow + dir[0];
            int col = tileCol + dir[1];

            // move until hit piece or edge
            while (row >= 0 && row < 8 && col >= 0 && col < 8 && board[row][col].equals("--")) {
                row += dir[0];
                col += dir[1];
            }

            // check first non-empty square
            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                String piece = board[row][col];

                if (validateMove(row, col, tileRow, tileCol, piece, true)
                        && ((byWhite && isWhite(piece)) || (!byWhite && isBlack(piece)))) {
                    return true;
                }
            }
        }

        // check knight attacks
        for (int[] dir : knightDirections) {
            int row = tileRow + dir[0];
            int col = tileCol + dir[1];

            if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                String piece = board[row][col];

                if (piece.endsWith("Knight")
                        && validateMove(row, col, tileRow, tileCol, piece, true)
                        && ((byWhite && isWhite(piece)) || (!byWhite && isBlack(piece)))) {
                    return true;
                }
            }
        }

        return false;
    }

    // check checkmate or stalemate
    public static String kingInCheckmateOrStalemate(int tileRow, int tileCol, boolean byWhite) {

        if (!currentPlayerHasLegalMoves()) {

            if (tileIsVisible(tileRow, tileCol, byWhite)) {
                return "CHECKMATE";
            }

            return "STALEMATE";
        } else if (insufficientMaterial()) {
            return "INSUFFICIENT-MATERIAL";
        }

        return "";
    }

    // checks if current player has any legal move
    public static boolean currentPlayerHasLegalMoves() {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                String piece = board[row][col];

                if ((whiteTurn && isWhite(piece)
                        || !whiteTurn && isBlack(piece))
                        && hasLegalMoves(row, col, piece)) {
                    return true;
                }
            }
        }

        return false;
    }

    // checks if a piece has any valid move
    public static boolean hasLegalMoves(int pieceRow, int pieceCol, String piece) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                if (validateMove(pieceRow, pieceCol, row, col, piece, true)
                        && !putsOwnKingInCheck(pieceRow, pieceCol, row, col)) {
                    return true;
                }
            }
        }

        return false;
    }

    // checks insufficient material draw
    public static boolean insufficientMaterial() {

        ArrayList<String> nonKings = new ArrayList<>();

        for (String piece : remainingPieces) {
            if (!piece.contains("King")) {
                nonKings.add(piece);
            }
        }

        if (nonKings.isEmpty()) {
            return true; // king vs king
        }

        if (nonKings.size() == 1 &&
            (nonKings.get(0).contains("Bishop")
            || nonKings.get(0).contains("Knight"))) {
            return true;
        }

        return false;
    }
}