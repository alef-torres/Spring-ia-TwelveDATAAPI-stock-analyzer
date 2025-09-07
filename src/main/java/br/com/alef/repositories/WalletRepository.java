package br.com.alef.repositories;

import br.com.alef.entitites.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Share, Long> {
}
