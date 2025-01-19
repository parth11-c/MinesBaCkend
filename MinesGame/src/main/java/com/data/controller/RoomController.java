package com.data.controller;

import com.data.entity.Room;
import com.data.service.RoomService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Enable CORS for all origins and headers
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("/admin/create-room")
    public ResponseEntity<Room> createRoom(@RequestParam int timeoutMinutes) {
        try {
            Room room = roomService.createRoom(timeoutMinutes);
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null); // Adjust as per business logic
        }
    }

    @PostMapping("/join-room")
    public ResponseEntity<Room> joinRoom(@RequestParam String code, @RequestParam String name) {
        try {
            Room room = roomService.joinRoom(code, name);
            return ResponseEntity.ok(room);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Adjust based on exception type
        }
    }

    @GetMapping("/{roomCode}/game-cashouts")
    public ResponseEntity<?> getGameCashouts(@PathVariable String roomCode) {
        try {
            Map<String, Double> cashouts = roomService.getGameCashouts(roomCode);
            return ResponseEntity.ok(cashouts);
        } catch (RuntimeException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
