package com.lifeos.controllers;

import com.lifeos.model.Card;
import com.lifeos.model.CardStatus;
import com.lifeos.model.Category;
import com.lifeos.model.Project;
import com.lifeos.model.User;
import com.lifeos.payload.request.CardRequest;
import com.lifeos.repository.CardRepository;
import com.lifeos.repository.ProjectRepository;
import com.lifeos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    CardRepository cardRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserService userService;

    // ── HELPER DE SEGURIDAD ──────────────────────────────────────────────────────
    // Devuelve true si el usuario es dueño O colaborador de la categoría.
    // Aplica a todos los endpoints para corregir la vulnerabilidad IDOR.
    private boolean hasAccess(Category category, User user) {
        return category.getOwner().getId().equals(user.getId()) ||
               category.getCollaborators().stream().anyMatch(c -> c.getId().equals(user.getId()));
    }

    // ── POST /api/cards — Crear tarjeta (o subtarea) ─────────────────────────────
    @PostMapping
    public ResponseEntity<?> createCard(@Valid @RequestBody CardRequest request) {
        User currentUser = userService.getCurrentUser();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + request.getProjectId()));

        // IDOR fix: owner Y colaboradores pueden crear tarjetas
        if (!hasAccess(project.getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a este proyecto");
        }

        Card card = new Card();
        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        // Si no viene status en el request, @PrePersist lo inicializa a PENDING
        if (request.getStatus() != null) {
            card.setStatus(request.getStatus());
        }
        card.setStartDate(request.getStartDate());
        card.setEndDate(request.getEndDate());
        card.setProject(project);
        card.setUser(currentUser);

        // ── LÓGICA DE SUBTAREAS ──────────────────────────────────────────────────
        // Si se proporciona parentCardId, esta tarjeta es una subtarea
        if (request.getParentCardId() != null) {
            Card parentCard = cardRepository.findById(request.getParentCardId())
                    .orElseThrow(() -> new RuntimeException(
                            "Tarjeta padre no encontrada con ID: " + request.getParentCardId()));

            // Verificación estricta: ambas tarjetas deben pertenecer al mismo proyecto
            if (!parentCard.getProject().getId().equals(project.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La tarjeta padre y la subtarea deben pertenecer al mismo proyecto");
            }

            card.setParentCard(parentCard);
        }

        return ResponseEntity.ok(cardRepository.save(card));
    }

    // ── GET /api/cards/project/{projectId} — Listar tarjetas de un proyecto ──────
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getCardsByProject(@PathVariable("projectId") Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + projectId));

        // IDOR fix: owner Y colaboradores pueden ver las tarjetas
        if (!hasAccess(project.getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a este proyecto");
        }

        List<Card> cards = cardRepository.findByProjectId(projectId);
        return ResponseEntity.ok(cards);
    }

    // ── PUT /api/cards/{id} — Actualizar tarjeta ─────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCard(@PathVariable("id") Long id, @Valid @RequestBody CardRequest request) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada con ID: " + id));

        // IDOR fix: cualquier miembro del workspace puede editar una tarjeta
        if (!hasAccess(card.getProject().getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a esta tarjeta");
        }

        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());

        CardStatus newStatus = request.getStatus();
        if (newStatus != null) {
            card.setStatus(newStatus);

            // Lógica híbrida START DATE
            if (request.getStartDate() != null) {
                card.setStartDate(request.getStartDate());
            } else if (newStatus == CardStatus.IN_PROGRESS && card.getStartDate() == null) {
                card.setStartDate(LocalDate.now()); // Automático al pasar a IN_PROGRESS
            }

            // Lógica híbrida END DATE
            if (request.getEndDate() != null) {
                card.setEndDate(request.getEndDate());
            } else if (newStatus == CardStatus.DONE && card.getEndDate() == null) {
                card.setEndDate(LocalDate.now()); // Automático al marcar como DONE
            }
        }

        return ResponseEntity.ok(cardRepository.save(card));
    }

    // ── DELETE /api/cards/{id} — Eliminar tarjeta ────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada con ID: " + id));

        // IDOR fix: cualquier miembro del workspace puede borrar tarjetas
        if (!hasAccess(card.getProject().getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a esta tarjeta");
        }

        cardRepository.delete(card);
        return ResponseEntity.ok("Tarjeta eliminada correctamente");
    }
}

