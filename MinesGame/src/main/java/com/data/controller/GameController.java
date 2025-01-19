package com.data.controller;

 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.data.entity.CashoutResponse;
import com.data.entity.Game;
import com.data.entity.GameRequest;
import com.data.entity.MoveRequest;
import com.data.service.GameService;

@RestController
@RequestMapping("/rooms/{roomCode}/games")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Enable CORS for all origins and headers

public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * Starts a game in the specified room.
     * @param roomCode the code of the room
     * @param request the game request containing bet amount and number of mines
     * @return the started game
     */
    @CrossOrigin
    @PostMapping("/start")
    public Game startGame(@PathVariable String roomCode, @RequestBody GameRequest request) {
        return gameService.startGameInRoom(roomCode, request);
    }

    /**
     * Makes a move in a game.
     * @param request the move request containing gameId and move details
     * @return the updated game state
     */
    @CrossOrigin
    @PostMapping("/move")
    public Game makeMove(@RequestBody MoveRequest request) {
        return gameService.makeMove(request);
    }

    /**
     * Cashes out from a game.
     * @param gameId the ID of the game
     * @return the cashout response
     */
    @CrossOrigin
    @PostMapping("/{gameId}/cashout")
    public CashoutResponse cashout(@PathVariable String gameId) {
        return gameService.cashout(gameId);
    }
}
