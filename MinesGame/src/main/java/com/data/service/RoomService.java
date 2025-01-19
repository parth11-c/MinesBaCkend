package com.data.service;

 import com.data.entity.Game;
import com.data.entity.Room;
import com.data.entity.User;
import com.data.repository.GameRepository;
import com.data.repository.RoomRepository;
import com.data.repository.URepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
 
@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private URepo userRepository;
   
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    
    
    public Room createRoom(int timeoutMinutes) {
        Room room = new Room();
        room.setCode(generateRoomCode());
        room.setCreatedAt(LocalDateTime.now());
        room.setTimeout(timeoutMinutes);
        room.setClosed(false);

        Room savedRoom = roomRepository.save(room);

        // Schedule the room to be closed after the timeout
        scheduleRoomClosure(savedRoom.getId(), timeoutMinutes);

        return savedRoom;
    }

    public Room joinRoom(String code, String name) {
        Room room = roomRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.isClosed()) {
            throw new IllegalStateException("Room is closed. No more users can join.");
        }

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (room.getUsers().stream().noneMatch(u -> u.getName().equals(name))) {
            room.getUsers().add(user);
            roomRepository.save(room);
        }

        return room;
    }

    private String generateRoomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void scheduleRoomClosure(String roomId, int timeoutMinutes) {
        long delayMillis = Duration.ofMinutes(timeoutMinutes).toMillis();

        taskScheduler.schedule(() -> closeRoom(roomId), new Date(System.currentTimeMillis() + delayMillis));
    }

    private void closeRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setClosed(true);
        roomRepository.save(room);
    }
 
  
    
    public Map<String, Double> getGameCashouts(String roomCode) {
        // Fetch the room by its code
        Room room = roomRepository.findByCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check if the room has games
        if (room.getGames() == null || room.getGames().isEmpty()) {
            throw new RuntimeException("No games found in the specified room.");
        }

        // Create a map to store gameId -> cashoutAmount
        Map<String, Double> gameCashouts = new HashMap<>();

        // Iterate over each game in the room
        for (Game game : room.getGames()) {
            // Calculate or fetch cashout amount for the game
            double cashoutAmount = calculateCashout(game);
            gameCashouts.put(game.getId(), cashoutAmount);
        }

        // Sort the map in descending order of cashoutAmount
        return gameCashouts.entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sort by value (descending)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here)
                        LinkedHashMap::new // Use LinkedHashMap to preserve order
                ));
    }

    /**
     * Calculates the cashout amount for a specific game.
     * Modify this logic based on how cashout is determined.
     * @param game the game object
     * @return the cashout amount
     */
    private double calculateCashout(Game game) {
        if ("WON".equals(game.getGameState())) {
            return game.getBetAmount() * game.getMultiplier();
        } else {
            return game.getBetAmount() * game.getMultiplier(); // No cashout for games that are not won
        }
    }
    
    
    
    
    
    
    
}
    
    
    
    

