package com.impressivehen.server.service;

import com.impressivehen.model.Stock;
import com.impressivehen.model.StockQuote;
import com.impressivehen.model.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@GrpcService
public class StockService extends StockServiceGrpc.StockServiceImplBase {

    /*
    Server-Side Streaming: single request -> several response
     */
    @Override
    public void serverSideStreamingGetListStockQuotes(Stock request, StreamObserver<StockQuote> responseObserver) {
        for (int i = 1; i <= 5; i++) {
            StockQuote stockQuote = StockQuote.newBuilder()
                    .setPrice(fetchStockPriceBid(request))
                    .setOfferNumber(i)
                    .setDescription("Quote description -" + request.getTickerSymbol())
                    .build();
            responseObserver.onNext(stockQuote);
        }
        responseObserver.onCompleted();
    }

    /*
    Client-Side Streaming: several request -> single response
     */
    @Override
    public StreamObserver<Stock> clientSideStreamingGetStatisticsOfStocks(StreamObserver<StockQuote> responseObserver) {
        return new StreamObserver<Stock>() {
            int count;
            double price = 0.0;
            StringBuffer sb = new StringBuffer();

            /*
            onNext() called on each request
             */
            @Override
            public void onNext(Stock stock) {
                count++;
                price += fetchStockPriceBid(stock);
                sb.append(":").append(stock.getTickerSymbol());
            }

            /*
            onCompleted() called after all requests are sent
             */
            @Override
            public void onCompleted() {
                responseObserver.onNext(StockQuote.newBuilder()
                        .setPrice(price / count)
                        .setDescription("Statistics-" + sb.toString())
                        .build());

                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                log.error("{}", t.getMessage());
            }
        };
    }

    /*
    Bidirectional-Side Streaming: several request -> single response
     */
    @Override
    public StreamObserver<Stock> bidirectionalStreamingGetListsStockQuotes(StreamObserver<StockQuote> responseObserver) {
        return new StreamObserver<Stock>() {
            @Override
            public void onNext(Stock request) {
                for (int i = 1; i <= 5; i++) {
                    StockQuote stockQuote = StockQuote.newBuilder()
                            .setPrice(fetchStockPriceBid(request))
                            .setOfferNumber(i)
                            .setDescription("Quote description -" + request.getDescription())
                            .build();

                    responseObserver.onNext(stockQuote);
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable t) {
                log.error("{}", t.getMessage());
            }
        };
    }

    private static double fetchStockPriceBid(Stock stock) {

        return stock.getTickerSymbol()
                .length()
                + ThreadLocalRandom.current()
                .nextDouble(-0.1d, 0.1d);
    }
}
