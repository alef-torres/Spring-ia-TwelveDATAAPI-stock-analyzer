package br.com.alef.tools;

import br.com.alef.entitites.Share;
import br.com.alef.repositories.WalletRepository;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class WalletTools {

    private WalletRepository walletRepository;

    public WalletTools(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Tool(description = "Numero de ações de cada empresa na minha carteira")
    public List<Share> getNumberOfShares() {
        return walletRepository.findAll();
    }
}
