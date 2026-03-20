package com.lifeos.repository;

import com.lifeos.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCategoryId(Long categoryId);
}