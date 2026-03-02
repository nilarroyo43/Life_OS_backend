package com.lifeos.repository;

import com.lifeos.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 1. EL MÉTODO ARREGLADO: Ahora busca por "Owner" en lugar de "User"
    List<Category> findByOwnerId(Long ownerId);

    // 2. EL MÉTODO COMPARTIDO (El que arreglamos en el paso anterior y ya funciona)
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN c.collaborators col WHERE c.owner.id = :userId OR col.id = :userId")
    List<Category> findMyCategoriesAndShared(@Param("userId") Long userId);
}