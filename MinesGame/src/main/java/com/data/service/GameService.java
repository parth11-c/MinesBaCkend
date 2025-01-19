package com.data.service;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.data.entity.CashoutResponse;
import com.data.entity.Game;
import com.data.entity.GameRequest;
import com.data.entity.MoveRequest;
import com.data.entity.MultiplierUtil;
import com.data.entity.Room;
import com.data.entity.User;
import com.data.repository.GameRepository;
import com.data.repository.RoomRepository;
import com.data.repository.URepo;

@Service
public class GameService {

    private final GameRepository gameRepository;
    
    
    @Autowired
    private RoomRepository roomRepository;

     
    @Autowired
    private URepo repo;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public CashoutResponse cashout(String gameId) {
        // Fetch the game
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // Check if the game is in progress
        if (!"IN_PROGRESS".equals(game.getGameState())) {
            throw new IllegalStateException("Game is not in progress. Cashout is not allowed.");
        }

        // Calculate cashout amount
        double cashoutAmount = game.getBetAmount() * game.getMultiplier();

        // Update game state to "CASHED_OUT"
        game.setGameState("CASHED_OUT");
        gameRepository.save(game);

        // Prepare the response
        CashoutResponse response = new CashoutResponse();
        response.setGameId(game.getId());
        response.setBetAmount(game.getBetAmount());
        response.setCashoutAmount(cashoutAmount);
        response.setGameState(game.getGameState());

        return response;
    }

    public Game startGame(GameRequest request) {
        Game game = new Game();
        game.setBetAmount(request.getBetAmount());
        game.setMines(generateMines(request.getNumMines()));
        game.setRevealed(new HashSet<>()); // Initialize revealed cells
        game.setGameState("IN_PROGRESS");
        game.setMultiplier(1.0); // Initial multiplier is 1.0
        gameRepository.save(game);
        return game;
    }

    public Game makeMove(MoveRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (!"IN_PROGRESS".equals(game.getGameState())) {
            throw new IllegalStateException("Game is not in progress");
        }

        // Check if the move has already been revealed
        if (game.getRevealed().contains(request.getMove())) {
            throw new IllegalStateException("This move has already been made");
        }

        // If the move hits a mine, the game is lost
        if (game.getMines().contains(request.getMove())) {
            game.setGameState("LOST");
            gameRepository.save(game);
            return game;
        }

        // Add the move to the revealed cells
        game.getRevealed().add(request.getMove());
        int revealedCount = game.getRevealed().size();
        int numMines = game.getMines().size();

        // Validate revealed count
        if (revealedCount > (25 - numMines)) {
            throw new IllegalStateException("Revealed cells exceed allowable range");
        }

        // Calculate the new multiplier based on revealed cells and mines
        double multiplier = MultiplierUtil.getMultiplier(numMines, revealedCount);
        game.setMultiplier(multiplier);

        // If all non-mine cells are revealed, the player wins
        if (revealedCount == 25 - numMines) {
            game.setGameState("WON");
        }

        // Save the updated game state
        gameRepository.save(game);
        return game;
    }


    private Set<Integer> generateMines(int numMines) {
        Set<Integer> mines = new HashSet<>();
        Random random = new Random();
        while (mines.size() < numMines) {
            mines.add(random.nextInt(25)); // 5x5 grid
        }
        return mines;
    }
 
    
    public Game startGameInRoom(String roomCode, GameRequest request) {
        // Fetch the room by its code
        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Create a new game
        Game game = new Game();
        game.setBetAmount(request.getBetAmount());
        game.setMines(generateMines(request.getNumMines()));
        game.setRevealed(new HashSet<>()); // Initialize revealed cells
        game.setGameState("IN_PROGRESS");
        game.setMultiplier(1.0); // Initial multiplier is 1.0

        // Save the game and add it to the room
        gameRepository.save(game);
        room.getGames().add(game);
        roomRepository.save(room);

        return game;
    }
    
    
}
