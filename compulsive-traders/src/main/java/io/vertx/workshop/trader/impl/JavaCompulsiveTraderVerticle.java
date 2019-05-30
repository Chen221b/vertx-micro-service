package io.vertx.workshop.trader.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.MessageSource;
import io.vertx.workshop.common.MicroServiceVerticle;
import io.vertx.workshop.portfolio.PortfolioService;

/**
 * A compulsive trader...
 */
public class JavaCompulsiveTraderVerticle extends MicroServiceVerticle {

    @Override
    public void start(Future<Void> future) {
        super.start();

        //TODO
        //----
        String company = TraderUtils.pickACompany();
        int numberOfShares = TraderUtils.pickANumber();
        System.out.println("Java compulsive trader configured for company " + company + " and shares: " + numberOfShares);


        Future<MessageConsumer<JsonObject>> marketFuture = Future.future();
        Future<PortfolioService> portfolioFuture = Future.future();

        MessageSource.getConsumer(discovery, new JsonObject().put("name", "market-data"), marketFuture.completer());
        EventBusService.getProxy(discovery, PortfolioService.class, portfolioFuture.completer());

        CompositeFuture.all(marketFuture, portfolioFuture).setHandler(ar -> {
            if (ar.failed()) {
                future.fail("One of the required service cannot " +
                        "be retrieved: " + ar.cause());
            } else {
                PortfolioService portfolio = portfolioFuture.result();
                MessageConsumer<JsonObject> marketConsumer = marketFuture.result();

                marketConsumer.handler(message -> {
                    JsonObject quote = message.body();
                    TraderUtils.dumbTradingLogic(company, numberOfShares, portfolio, quote);
                });

                future.complete();
            }
        });
        // ----
    }


}
