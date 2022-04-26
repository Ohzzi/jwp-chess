package chess.service;

import chess.controller.dto.request.MoveRequest;
import chess.controller.dto.response.ChessGameResponse;
import chess.controller.dto.response.EndResponse;
import chess.controller.dto.response.GameIdsResponse;
import chess.controller.dto.response.PieceResponse;
import chess.controller.dto.response.StatusResponse;
import chess.dao.GameDao;
import chess.dao.PieceDao;
import chess.domain.ChessGame;
import chess.domain.GameState;
import chess.domain.board.Board;
import chess.domain.board.Column;
import chess.domain.board.Position;
import chess.domain.board.Row;
import chess.domain.board.strategy.CreateCompleteBoardStrategy;
import chess.domain.piece.Piece;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChessService {

    private static final int ROW_INDEX = 0;
    private static final int COLUMN_INDEX = 1;

    private static final String NOT_HAVE_GAME = "해당하는 게임이 없습니다.";

    private final GameDao gameDao;
    private final PieceDao pieceDao;

    public ChessService(GameDao gameDao, PieceDao pieceDao) {
        this.gameDao = gameDao;
        this.pieceDao = pieceDao;
    }

    public ChessGameResponse createGame(long gameId) {
        ChessGame chessGame = new ChessGame(new Board(new CreateCompleteBoardStrategy()));
        gameDao.save(gameId);
        saveBoard(gameId, chessGame.getBoard());
        return new ChessGameResponse(chessGame);
    }

    public void saveBoard(long gameId, Map<Position, Piece> board) {
        board.forEach((position, piece) -> savePiece(gameId, position, piece));
    }

    private void savePiece(long gameId, Position position, Piece piece) {
        if (pieceDao.find(gameId, position).isEmpty()) {
            pieceDao.save(gameId, position, piece);
        }
    }

    public GameIdsResponse findAllGameIds() {
        return new GameIdsResponse(gameDao.findAllIds());
    }

    public ChessGameResponse loadGame(long gameId) {
        return new ChessGameResponse(createChessGameObject(gameId));
    }

    private ChessGame createChessGameObject(long gameId) {
        Optional<GameState> maybeGameState = gameDao.load(gameId);
        GameState gameState = maybeGameState.orElseThrow(NoSuchElementException::new);
        Board board = createBoard(gameId);
        return new ChessGame(board, gameState);
    }

    private Board createBoard(long gameId) {
        Map<Position, Piece> pieces = new HashMap<>();
        for (PieceResponse pieceResponse : pieceDao.findAll(gameId)) {
            Position position = parseStringToPosition(pieceResponse.getPosition());
            Piece piece = pieceResponse.toPiece();
            pieces.put(position, piece);
        }
        return new Board(() -> pieces);
    }

    public ChessGameResponse startGame(long gameId) {
        gameDao.updateState(gameId, GameState.WHITE_RUNNING);
        return loadGame(gameId);
    }

    public ChessGameResponse resetGame(long gameId) {
        gameDao.delete(gameId);
        return createGame(gameId);
    }

    public ChessGameResponse move(long gameId, MoveRequest moveRequest) {
        ChessGame chessGame = createChessGameObject(gameId);
        Position start = parseStringToPosition(moveRequest.getStart());
        Position target = parseStringToPosition(moveRequest.getTarget());
        chessGame.move(start, target);
        if (pieceDao.find(gameId, target).isPresent()) {
            pieceDao.delete(gameId, target);
        }
        pieceDao.updatePosition(gameId, start, target);
        gameDao.updateState(gameId, chessGame.getGameState());
        return new ChessGameResponse(chessGame);
    }

    public StatusResponse status(long gameId) {
        ChessGame chessGame = createChessGameObject(gameId);
        return new StatusResponse(chessGame.createStatus());
    }

    public EndResponse endGame(long gameId) {
        gameDao.delete(gameId);
        return new EndResponse("게임이 종료되었습니다.");
    }

    public void deleteGame(long gameId) {
        gameDao.delete(gameId);
    }

    private Position parseStringToPosition(final String rawPosition) {
        final String[] separatedPosition = rawPosition.split("");
        final Column column = Column.from(separatedPosition[ROW_INDEX]);
        final Row row = Row.from(separatedPosition[COLUMN_INDEX]);
        return new Position(column, row);
    }
}
