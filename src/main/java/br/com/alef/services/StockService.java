package br.com.alef.services;

import br.com.alef.api.DailyStockData;
import br.com.alef.api.StockData;
import br.com.alef.api.StockRequest;
import br.com.alef.api.StockResponse;
import br.com.alef.settings.APISettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;


public class StockService implements Function<StockRequest, StockResponse> {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private RestTemplate restTemplate;

    public StockService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${TWELVE_DATA_API_KEY:none}")
    String apiKey;

    @Override
    public StockResponse apply(StockRequest stockRequest) {
        StockData data = restTemplate.getForObject(
                APISettings.TWELVE_DATA_BASE_URL + "?symbol={0}&interval=1day&outputsize=1&apikey={1}",
                StockData.class,
                stockRequest.company(),
                apiKey);

        DailyStockData latestData = data.getValues().get(0);
        return new StockResponse(Float.parseFloat(latestData.getClose()));
    }
}
