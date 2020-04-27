package spring.controller;

import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import spring.chess.game.ChessGame;
import spring.dto.LocationDto;
import spring.service.ChessService;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class ChessWebController {
    private static final HandlebarsTemplateEngine handlebarsTemplateEngine = new HandlebarsTemplateEngine();
    private static final String STATIC_FILES_LOCATION = "templates";

    private final ChessService chessService;

    public ChessWebController(ChessService chessService) {
        this.chessService = chessService;
        this.run();
    }

    private void run() {
        staticFiles.location(STATIC_FILES_LOCATION);

        getMain();

        getStart();

        getBoards();

        getBoard();

        postMove();

        getWinner();

        postEnd();

        postNewGame();
    }

    private void postNewGame() {
        post("/start/new/game", (req, res) -> {
            int gameId = Integer.parseInt(req.queryParams("game_id"));
            ChessGame chessGame = new ChessGame();
            chessService.resetChessGame(chessGame, gameId);
            return chessService.findBoard(gameId);
        });
    }

    private void postEnd() {
        post("/end", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "start.html");
        });
    }

    private void getWinner() {
        get("/start/winner", (req, res) -> {
            int gameId = Integer.parseInt(req.queryParams("game_id"));
            ChessGame chessGame = chessService.makeGameByDB(gameId);
            return chessService.findWinner(chessGame);
        });
    }

    private void postMove() {
        post("/start/move", (req, res) -> {
            LocationDto nowDto = new LocationDto(req.queryParams("now"));
            LocationDto destinationDto = new LocationDto(req.queryParams("des"));
            int gameId = Integer.parseInt(req.queryParams("game_id"));

            return chessService.move(nowDto, destinationDto, gameId);
        });
    }

    private void getBoard() {
        get("/start/board", (req, res) -> {
            int boardId = Integer.parseInt(req.queryParams("id"));
            ChessGame chessGame = chessService.makeGameByDB(boardId);
            return chessService.findGame(chessGame);
        });
    }

    private void getBoards() {
        get("/start/boards", (req, res) -> chessService.findAllBoards());
    }

    private void getStart() {
        get("/start", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "index.html");
        });
    }

    private void getMain() {
        get("/main", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return render(model, "start.html");
        });
    }

    private static String render(Map<String, Object> model, String templatePath) {
        return handlebarsTemplateEngine.render(new ModelAndView(model, templatePath));
    }
}