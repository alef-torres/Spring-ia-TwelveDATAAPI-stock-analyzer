package br.com.alef.tools;

import br.com.alef.api.DailyShareQuote;
import br.com.alef.api.DailyStockData;
import br.com.alef.api.StockData;
import br.com.alef.api.StockResponse;
import br.com.alef.services.StockService;
import br.com.alef.settings.APISettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class StockTools {

    private static final Logger logger = LoggerFactory.getLogger(StockTools.class);

    private RestTemplate restTemplate;

    public StockTools(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${TWELVE_DATA_API_KEY:none}")
    String apiKey;

    @Tool(description = "Últimos preços das ações")
    public StockResponse getLatestStockPrices(@ToolParam(description = "Name of Company") String company) {

        logger.info("Pega o valor da ação da companhia: {}", company);

        StockData data = restTemplate.getForObject(
                APISettings.TWELVE_DATA_BASE_URL + "?symbol={0}&interval=1day&outputsize=1&apikey={1}",
                StockData.class,
                company,
                apiKey);

        DailyStockData latestData = data.getValues().get(0);
        return new StockResponse(Float.parseFloat(latestData.getClose()));
    }

    @Tool(description = "Histórico preços das ações")
    public List<DailyShareQuote> getHistoricalStockPrices(
            @ToolParam(description = "Name of Company") String company,
            @ToolParam(description = "Search period in days") int days) {

        logger.info("Pega o histórico de valores da {} em número de dias: {}", company, days);

        StockData data = restTemplate.getForObject(
                APISettings.TWELVE_DATA_BASE_URL + "?symbol={0}&interval=1day&outputsize={1}&apikey={2}",
                StockData.class,
                company,
                days,
                apiKey);

        return data.getValues().stream()
                .map((DailyStockData d) -> new DailyShareQuote(company, Float.parseFloat(d.getClose()), d.getDatetime()))
                .toList();
    }
}
