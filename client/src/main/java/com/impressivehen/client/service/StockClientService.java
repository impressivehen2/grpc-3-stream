package com.impressivehen.client.service;

import com.impressivehen.model.Stock;
import com.impressivehen.model.StockQuote;
import com.impressivehen.model.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class StockClientService {
    @GrpcClient("grpc-stock-server")
    private StockServiceGrpc.StockServiceBlockingStub blockingStub;

    @GrpcClient("grpc-stock-server")
    private StockServiceGrpc.StockServiceStub nonBlockingStub;

    private static List<Stock> stocks = Arrays.asList(
            Stock.newBuilder()
                    .setTickerSymbol("F")
                    .setCompanyName("Facebook")
                    .setDescription("Meta previous name")
                    .build(),
            Stock.newBuilder()
                    .setTickerSymbol("YT")
                    .setCompanyName("Youtube")
                    .setDescription("Google streaming")
                    .build()
    );

    /*
    Server-Side - Client Call
     */
    public void serverSideStreamingListOfStockPrices() {
        Stock stock = Stock.newBuilder()
                .setTickerSymbol("AU")
                .setCompanyName("Austich")
                .setDescription("server streaming example")
                .build();

        Iterator<StockQuote> stockQuotes;
        try {
            stockQuotes = blockingStub.serverSideStreamingGetListStockQuotes(stock);

            for (int i = 1; stockQuotes.hasNext(); i++) {
                StockQuote stockQuote = stockQuotes.next();
                log.info("RESPONSE - Price #" + i + ": {}", stockQuote.getPrice());
            }
        } catch (Exception e) {
            log.error("RPC failed: {}", e.getMessage());
        }
    }

    /*
    Client-Side - Client Call
     */
    public void clientSideStreamingGetStatisticsOfStocks() throws InterruptedException {
        StreamObserver<StockQuote> responseObserver = new StreamObserver<StockQuote>() {

            @Override
            public void onNext(StockQuote summary) {
                log.info("RESPONSE, got stock statistics - Average Price: {}, description: {}", summary.getPrice(), summary.getDescription());
            }

            @Override
            public void onCompleted() {
                log.info("Finished clientSideStreamingGetStatisticsOfStocks");
            }

            @Override
            public void onError(Throwable t) {
                log.error("{}", t.getMessage());
            }
        };

        StreamObserver<Stock> requestObserver = nonBlockingStub.clientSideStreamingGetStatisticsOfStocks(responseObserver);
        try {
            for (Stock stock : stocks) {
                log.info("REQUEST: {}, {}", stock.getTickerSymbol(), stock.getCompanyName());
                requestObserver.onNext(stock);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();
    }

    /*
    Bidirectional-Side - Client Call
     */
    public void bidirectionalStreamingGetListsStockQuotes() throws InterruptedException{
        StreamObserver<StockQuote> responseObserver = new StreamObserver<StockQuote>() {
            @Override
            public void onNext(StockQuote stockQuote) {
                log.info("RESPONSE offerNumber: {}, price: {}, description: {}", stockQuote.getOfferNumber(), stockQuote.getPrice(), stockQuote.getDescription());
            }

            @Override
            public void onCompleted() {
                log.info("Finished bidirectionalStreamingGetListsStockQuotes");
            }

            @Override
            public void onError(Throwable t) {
                log.error("{}", t.getMessage());
            }
        };

        StreamObserver<Stock> requestObserver = nonBlockingStub.bidirectionalStreamingGetListsStockQuotes(responseObserver);
        try {
            for (Stock stock : stocks) {
                log.info("REQUEST: {}, {}", stock.getTickerSymbol(), stock.getCompanyName());
                requestObserver.onNext(stock);
                Thread.sleep(200);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }
        requestObserver.onCompleted();
    }
}
