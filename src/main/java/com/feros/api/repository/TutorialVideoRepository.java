package com.feros.api.repository;

import com.feros.api.entity.TutorialVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TutorialVideoRepository extends JpaRepository<TutorialVideo, Long> {

    // mobile: returns videos for the user's role + ALL, filtered by language, active only
    @Query("SELECT t FROM TutorialVideo t WHERE (t.role = :role OR t.role = 'ALL') AND t.language = :language AND t.isActive = true ORDER BY t.sortOrder ASC")
    List<TutorialVideo> findForRole(@Param("role") String role, @Param("language") String language);

    // SA: all videos, optionally filtered
    List<TutorialVideo> findAllByOrderBySortOrderAsc();
}
