package com.lifeos.controllers;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lifeos.model.Category;
import com.lifeos.model.User;
import com.lifeos.payload.request.CategoryRequest;
import com.lifeos.repository.CategoryRepository;
import com.lifeos.repository.UserRepository;
import com.lifeos.service.UserService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    CategoryRepository categoryRepository;

    // ── HELPER DE SEGURIDAD ──────────────────────────────────────────────────────
    // Devuelve true si el usuario es dueño O colaborador de la categoría.
    // Esto corrige la vulnerabilidad IDOR que solo comprobaba al owner.
    private boolean hasAccess(Category category, User user) {
        return category.getOwner().getId().equals(user.getId()) ||
               category.getCollaborators().stream().anyMatch(c -> c.getId().equals(user.getId()));
    }

    // ── POST /api/categories — Crear categoría ───────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest request) {

        // 1. Sacamos el username del Token (la muralla)
        User owner = userService.getCurrentUser();

        // 2. Creamos la categoría usando Setters (más seguro si usas Lombok)
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setHasTimeTracking(request.isHasTimeTracking());
        category.setColor(request.getColor());
        
        // 3. Le asignamos el dueño
        category.setOwner(owner);

        // 4. Guardamos y obtenemos el resultado con el ID generado
        Category savedCategory = categoryRepository.save(category);

        // 5. Devolvemos el JSON de la categoría recién creada
        return ResponseEntity.ok(savedCategory);
    }

    // El endpoint para LEER (GET)
    @GetMapping
    public ResponseEntity<?> getMyCategories() {
        // 1. Sacamos quién es el usuario que hace la petición
        User currentUser = userService.getCurrentUser();

        // 2. Usamos tu método personalizado pasándole el ID
        List<Category> myCategories = categoryRepository.findMyCategoriesAndShared(currentUser.getId());

        // 3. Devolvemos la lista entera. ¡Si no hay ninguna, devolverá un [] vacío (lo cual es correcto)!
        return ResponseEntity.ok(myCategories);
    }

    // ── GET /api/categories/{id} ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // IDOR fix: owner Y colaboradores pueden ver la categoría
        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: No tienes permiso para ver esta categoría");
        }

        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // IDOR fix: owner Y colaboradores pueden editar
        if (!hasAccess(category, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: No tienes permiso para editar esta categoría");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setHasTimeTracking(request.isHasTimeTracking());
        category.setColor(request.getColor());

        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // ── DELETE /api/categories/{id} ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        User currentUser = userService.getCurrentUser();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // Solo el owner puede eliminar el workspace completo
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: Solo el propietario puede eliminar esta categoría");
        }

        categoryRepository.delete(category);
        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}