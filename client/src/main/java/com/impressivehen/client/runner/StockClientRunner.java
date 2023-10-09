package com.impressivehen.client.runner;

import com.impressivehen.client.service.StockClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockClientRunner implements CommandLineRunner {
    @Autowired
    private StockClientService stockClientService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Server-Side call start");
        stockClientService.serverSideStreamingListOfStockPrices();

        log.info("Client-Side call start");
        stockClientService.clientSideStreamingGetStatisticsOfStocks();

        log.info("Bidirectional-Side call start");
        stockClientService.bidirectionalStreamingGetListsStockQuotes();
    }
}
