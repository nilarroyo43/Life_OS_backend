package com.lifeos.controllers;

import com.lifeos.model.Card;
import com.lifeos.model.Category;
import com.lifeos.model.Tag;
import com.lifeos.model.User;
import com.lifeos.payload.request.TagRequest;
import com.lifeos.repository.CardRepository;
import com.lifeos.repository.CategoryRepository;
import com.lifeos.repository.TagRepository;
import com.lifeos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    UserService userService;

    // ── HELPER DE SEGURIDAD ──────────────────────────────────────────────────────
    // Verifica si el usuario es dueño O colaborador de la categoría (IDOR fix)
    private boolean hasAccess(Category category, User user) {
        return category.getOwner().getId().equals(user.getId()) ||
               category.getCollaborators().stream().anyMatch(c -> c.getId().equals(user.getId()));
    }

    // ── POST /api/tags — Crear una etiqueta en una categoría ────────────────────
    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody TagRequest request) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta categoría");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setCategory(category);

        return ResponseEntity.ok(tagRepository.save(tag));
    }

    // ── GET /api/tags/category/{categoryId} — Listar tags por categoría ─────────
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getTagsByCategory(@PathVariable("categoryId") Long categoryId) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoryId));

        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta categoría");
        }

        List<Tag> tags = tagRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(tags);
    }

    // ── POST /api/tags/card/{cardId}/tag/{tagId} — Añadir tag a una tarjeta ─────
    @PostMapping("/card/{cardId}/tag/{tagId}")
    public ResponseEntity<?> addTagToCard(@PathVariable("cardId") Long cardId,
                                          @PathVariable("tagId") Long tagId) {
        User currentUser = userService.getCurrentUser();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada con ID: " + cardId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada con ID: " + tagId));

        Category cardCategory = card.getProject().getCategory();

        if (!hasAccess(cardCategory, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta tarjeta");
        }

        // Validar que la etiqueta pertenece a la misma categoría que la tarjeta
        if (!tag.getCategory().getId().equals(cardCategory.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La etiqueta no pertenece al mismo Workspace que la tarjeta");
        }

        card.getTags().add(tag);
        return ResponseEntity.ok(cardRepository.save(card));
    }

    // ── DELETE /api/tags/card/{cardId}/tag/{tagId} — Quitar tag de tarjeta ──────
    @DeleteMapping("/card/{cardId}/tag/{tagId}")
    public ResponseEntity<?> removeTagFromCard(@PathVariable("cardId") Long cardId,
                                               @PathVariable("tagId") Long tagId) {
        User currentUser = userService.getCurrentUser();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada con ID: " + cardId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada con ID: " + tagId));

        if (!hasAccess(card.getProject().getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta tarjeta");
        }

        card.getTags().remove(tag);
        return ResponseEntity.ok(cardRepository.save(card));
    }
}

