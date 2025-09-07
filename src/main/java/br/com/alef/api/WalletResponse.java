package br.com.alef.api;

import br.com.alef.entitites.Share;

import java.util.List;

public record WalletResponse(List<Share> shares) {
}
