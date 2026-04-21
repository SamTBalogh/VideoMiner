package aiss.videominer.repository;

import aiss.videominer.model.Channel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, String> {

    Page<Channel> findByIdContaining(String id, Pageable pageable);
    Page<Channel> findByNameContaining(String name, Pageable pageable);
    Page<Channel> findByDescriptionContaining(String description, Pageable pageable);
    Page<Channel> findByCreatedTimeContaining(String createdTime, Pageable pageable);

}
