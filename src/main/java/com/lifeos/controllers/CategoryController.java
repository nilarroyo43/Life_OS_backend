package com.lifeos.controllers;

import java.util.List;
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

    // 1. ¿Qué dos repositorios necesitas inyectar aquí con @Autowired?
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    CategoryRepository categoryRepository;

    // 2. El endpoint para CREAR (POST)
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {

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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable("id") Long id, @RequestBody CategoryRequest request){

        // 1. Sacamos quién es el usuario que hace la petición
        User currentUser = userService.getCurrentUser();

        // 2. Buscamos la categoría concreta en la BBDD por su ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 3. LA MURALLA DE SEGURIDAD: ¿Es este usuario el dueño de la categoría?
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: No tienes permiso para editar esta categoría");
        }

        // 4. Si ha pasado la seguridad, actualizamos los datos
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setHasTimeTracking(request.isHasTimeTracking());
        category.setColor(request.getColor());

        // 5. Guardamos y devolvemos la categoría actualizada
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // La URL será /api/categories/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {

        // 1. Sacamos quién es el usuario actual
        User currentUser = userService.getCurrentUser();

        // 2. Buscamos la categoría en la BBDD
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        // 3. LA MURALLA DE SEGURIDAD: ¿Es el dueño?
        if (!category.getOwner().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: No tienes permiso para borrar esta categoría");
        }

        // 4. Si es el dueño, la borramos
        categoryRepository.delete(category);

        // 5. Devolvemos un mensaje de éxito
        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}