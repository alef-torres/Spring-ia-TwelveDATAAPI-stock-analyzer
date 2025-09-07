package br.com.alef.config;

import br.com.alef.api.StockRequest;
import br.com.alef.api.StockResponse;
import br.com.alef.api.WalletResponse;
import br.com.alef.repositories.WalletRepository;
import br.com.alef.services.StockService;
import br.com.alef.services.WalletService;
import br.com.alef.tools.StockTools;
import br.com.alef.tools.WalletTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class WalletConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Description("Number of shares for each company in my portfolio")
    public Supplier<WalletResponse> numberOfShares(WalletRepository repository) {
        return new WalletService(repository);
    }

    @Bean
    @Description("Latest Stock Prices")
    public Function<StockRequest, StockResponse> latestStockPrices() {
        return new StockService(restTemplate());
    }

    @Bean
    public WalletTools walletTools(WalletRepository walletRepository) {
        return new WalletTools(walletRepository);
    }

    @Bean
    public StockTools stockTools() {
        return new StockTools(restTemplate());
    }
}
