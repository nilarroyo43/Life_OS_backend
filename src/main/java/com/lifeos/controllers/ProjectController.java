package com.lifeos.controllers;

import com.lifeos.model.Category;
import com.lifeos.model.Project;
import com.lifeos.model.User;
import com.lifeos.payload.request.ProjectRequest;
import com.lifeos.repository.CategoryRepository;
import com.lifeos.repository.ProjectRepository;
import com.lifeos.service.UserService;
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

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectRequest request) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a esta categoría");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCategory(category);

        return ResponseEntity.ok(projectRepository.save(project));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProjectsByCategory(@PathVariable("categoryId") Long categoryId) {
        User currentUser = userService.getCurrentUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso");
        }

        List<Project> projects = projectRepository.findByCategoryId(categoryId);
        return ResponseEntity.ok(projects);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        if (!project.getCategory().getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puedes borrar este proyecto");
        }

        projectRepository.delete(project);
        return ResponseEntity.ok("Proyecto eliminado correctamente");
    }
}