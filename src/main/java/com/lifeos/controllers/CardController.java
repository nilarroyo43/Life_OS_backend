package com.lifeos.controllers;

import com.lifeos.model.Card;
import com.lifeos.model.CardStatus;
import com.lifeos.model.Project;
import com.lifeos.model.User;
import com.lifeos.payload.request.CardRequest;
import com.lifeos.repository.CardRepository;
import com.lifeos.repository.ProjectRepository;
import com.lifeos.service.UserService;
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
    ProjectRepository projectRepository; // Cambiado CategoryRepository por ProjectRepository

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<?> createCard(@RequestBody CardRequest request) {
        User currentUser = userService.getCurrentUser();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        // Seguridad verificando la categoría a través del proyecto
        if (!project.getCategory().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes añadir tarjetas a un proyecto que no es tuyo");
        }

        Card card = new Card();
        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        card.setStatus(CardStatus.valueOf(request.getStatus().name()));
        card.setStartDate(request.getStartDate());
        card.setEndDate(request.getEndDate());
        card.setProject(project); // Setear Proyecto en vez de Categoría
        card.setUser(currentUser); 

        return ResponseEntity.ok(cardRepository.save(card));
    }

    // URL cambiada de /category/id a /project/id
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getCardsByProject(@PathVariable("projectId") Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        if (!project.getCategory().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso");
        }

        List<Card> cards = cardRepository.findByProjectId(projectId);
        return ResponseEntity.ok(cards);
    }

    // El PUT (updateCard) y DELETE (deleteCard) se mantienen idénticos a como los tenías, 
    // porque solo modifican o borran la propia Card sin cambiarla de proyecto.
    // (Pega aquí tus métodos PUT y DELETE de tu antiguo CardController).


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

        //  Actualizamos el estado
        CardStatus newStatus = request.getStatus();
        card.setStatus(newStatus);

        //  Lógica Híbrida para START DATE
        if (request.getStartDate() != null) {
            // Opción A: El usuario lo ha "hardcodeado" 
            card.setStartDate(request.getStartDate());
        } else if (newStatus == CardStatus.IN_PROGRESS && card.getStartDate() == null) {
            // Opción B: Modo Automático (ha pasado a IN_PROGRESS y estaba vacío)
            card.setStartDate(LocalDate.now());
        }

        //  Lógica Híbrida para END DATE
        if (request.getEndDate() != null) {
            // Opción A: El usuario lo ha puesto a mano
            card.setEndDate(request.getEndDate());
        } else if (newStatus == CardStatus.DONE && card.getEndDate() == null) {
            // Opción B: Modo Automático (se ha marcado como DONE y estaba vacío)
            card.setEndDate(LocalDate.now());
        }

        // Guardamos y devolvemos
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