package com.lifeos.controllers;

import com.lifeos.model.Card;
import com.lifeos.model.CardStatus;
import com.lifeos.model.Category;
import com.lifeos.model.User;
import com.lifeos.payload.request.CardRequest;
import com.lifeos.repository.CardRepository;
import com.lifeos.repository.CategoryRepository;
import com.lifeos.repository.UserRepository;
import com.lifeos.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    CardRepository cardRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;
    

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardRequest request) {
        User currentUser = userService.getCurrentUser();

        // Buscamos la categoría donde quiere meter la tarjeta
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Seguridad: ¿Es suya la categoría?
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes añadir tarjetas a una categoría que no es tuya");
        }

        Card card = new Card();
        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        card.setStatus(CardStatus.valueOf(request.getStatus().name())); 
        card.setStartDate(request.getStartDate());
        card.setEndDate(request.getEndDate());
        
        card.setCategory(category);
        card.setUser(currentUser); // El dueño de la tarjeta

        Card savedCard = cardRepository.save(card);
        return ResponseEntity.ok(savedCard);
    }


    // URL: /api/cards/category/1
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getCardsByCategory(@PathVariable("categoryId") Long categoryId) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Seguridad
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a esta categoría");
        }

        List<Card> cards = cardRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(cards);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable("id") Long id, @RequestBody CardRequest request) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        // Seguridad: ¿Es el dueño de la tarjeta?
        if (!card.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes editar esta tarjeta");
        }

        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        card.setStartDate(request.getStartDate());
        card.setEndDate(request.getEndDate());
        card.setStatus(CardStatus.valueOf(request.getStatus().name()));

        Card updatedCard = cardRepository.save(card);
        return ResponseEntity.ok(updatedCard);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));

        if (!card.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes borrar esta tarjeta");
        }

        cardRepository.delete(card);
        return ResponseEntity.ok("Tarjeta eliminada correctamente");
    }
}