package com.medkit.backend.controller;

import com.medkit.backend.dto.response.FavoriteResponse;
import com.medkit.backend.service.FavoriteService;
import com.medkit.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<FavoriteResponse> toggleFavorite(@PathVariable Integer doctorId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        FavoriteResponse response = favoriteService.toggleFavorite(userEmail, doctorId);
        if (response == null) {
            // Removed from favorites
            return ResponseEntity.noContent().build();
        } else {
            // Added to favorites
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
    }

    @DeleteMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> removeFavorite(@PathVariable Integer doctorId) {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        favoriteService.removeFavorite(userEmail, doctorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites() {
        String userEmail = SecurityUtils.getCurrentUserEmail();
        List<FavoriteResponse> favorites = favoriteService.getMyFavorites(userEmail);
        return ResponseEntity.ok(favorites);
    }
}
