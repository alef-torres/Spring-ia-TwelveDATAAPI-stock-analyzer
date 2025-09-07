package br.com.alef.services;

import br.com.alef.api.WalletResponse;
import br.com.alef.repositories.WalletRepository;

import java.util.function.Supplier;

public class WalletService implements Supplier<WalletResponse> {

    private WalletRepository wallRepository;

    public WalletService(WalletRepository wallRepository) {
        this.wallRepository = wallRepository;
    }

    @Override
    public WalletResponse get() {
        var shares = wallRepository.findAll();
        return new WalletResponse(shares);
    }
}
