package com.lifeos.repository;

import com.lifeos.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card>{
    // Para sacar las tarjetas de una categoría concreta
    List<Card> findByCategoryId(Long categoryId);

    // Para ver las sub-tarjetas de una tarjeta padre
    List<Card> findByParentCardId(Long parentCardId);
}
