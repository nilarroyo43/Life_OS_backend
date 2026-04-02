package com.lifeos.controllers;

import com.lifeos.model.Category;
import com.lifeos.model.Project;
import com.lifeos.model.User;
import com.lifeos.payload.request.ProjectRequest;
import com.lifeos.repository.CategoryRepository;
import com.lifeos.repository.ProjectRepository;
import com.lifeos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserService userService;

    // ── HELPER DE SEGURIDAD ──────────────────────────────────────────────────────
    // Devuelve true si el usuario es dueño O colaborador de la categoría.
    private boolean hasAccess(Category category, User user) {
        return category.getOwner().getId().equals(user.getId()) ||
               category.getCollaborators().stream().anyMatch(c -> c.getId().equals(user.getId()));
    }

    // ── POST /api/projects — Crear proyecto ──────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createProject(@Valid @RequestBody ProjectRequest request) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + request.getCategoryId()));

        // IDOR fix: owner Y colaboradores pueden crear proyectos en la categoría
        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta categoría");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCategory(category);

        return ResponseEntity.ok(projectRepository.save(project));
    }

    // ── GET /api/projects/category/{categoryId} ───────────────────────────────────
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProjectsByCategory(@PathVariable("categoryId") Long categoryId) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoryId));

        // IDOR fix: owner Y colaboradores pueden listar proyectos
        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes acceso a esta categoría");
        }

        List<Project> projects = projectRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(projects);
    }

    // ── DELETE /api/projects/{id} ─────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));

        // IDOR fix: owner Y colaboradores pueden borrar proyectos
        if (!hasAccess(project.getCategory(), currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No puedes borrar este proyecto");
        }

        projectRepository.delete(project);
        return ResponseEntity.ok("Proyecto eliminado correctamente");
    }
}

