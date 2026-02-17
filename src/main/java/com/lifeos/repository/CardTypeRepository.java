package com.lifeos.repository;

import com.lifeos.model.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardTypeRepository extends JpaRepository<CardType, Long> {
    // Ver las opciones que define una tarjeta padre
    List<CardType> findByDefinedInCardId(Long cardId);
}