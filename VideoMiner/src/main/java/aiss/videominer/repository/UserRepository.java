package aiss.videominer.repository;

import aiss.videominer.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>{
    Page<User> findByName(String name, Pageable pageable);
    Page<User> findById(Long id, Pageable pageable);
    Page<User> findByUserLinkContaining(String user_link, Pageable pageable);
    Page<User> findByPictureLinkContaining(String picture_link, Pageable pageable);
}
