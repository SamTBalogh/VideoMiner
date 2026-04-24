package aiss.videominer.repository;

import aiss.videominer.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    boolean existsByTokenHash(String tokenHash);

    Optional<Token> findByTokenHash(String tokenHash);
}
