package com.lifeos.repository;

import com.lifeos.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Obtener todas las etiquetas de una categoría (Workspace)
    List<Tag> findByCategoryId(Long categoryId);
}

