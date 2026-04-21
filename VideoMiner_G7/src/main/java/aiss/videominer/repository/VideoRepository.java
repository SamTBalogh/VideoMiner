package aiss.videominer.repository;

import aiss.videominer.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Page<Video> findByName(String name, Pageable pageable);
    Page<Video> findById(String id, Pageable pageable);
    Page<Video> findByDescriptionContaining(String description, Pageable pageable);
    Page<Video> findByReleaseTimeContaining(String releaseTime, Pageable pageable);
}
