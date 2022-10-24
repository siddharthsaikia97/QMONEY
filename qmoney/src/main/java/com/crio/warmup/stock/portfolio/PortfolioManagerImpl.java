
package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.client.RestClientException;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PortfolioManagerImpl implements PortfolioManager {


  private StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  // @Deprecated
  // protected PortfolioManagerImpl(RestTemplate restTemplate) {
  //   this.restTemplate = restTemplate;
  // }

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }

  //CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws RestClientException, URISyntaxException, StockQuoteServiceException {


    List<AnnualizedReturn> annualizedReturnList = new ArrayList<>();
    AnnualizedReturn annualizedReturn;

    for (PortfolioTrade trade : portfolioTrades) {
      
      //Fetching annualized return for each trade
      annualizedReturn = getAnnualizedReturn(trade, endDate);

      //Appending each annualized return to the return list
      annualizedReturnList.add(annualizedReturn);
    }

    //Sorting in descending order of annualized return
    Collections.sort(annualizedReturnList, getComparator());

    return annualizedReturnList;

  }
  

  //Calculating annualized return for each stock
  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) throws RestClientException, URISyntaxException, StockQuoteServiceException {
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol();

    try {
      List<Candle> stockStartToLast;

      //Call to get the stock details from startDate to EndDate
      stockStartToLast = getStockQuote(symbol, startDate, endDate);

      //Extracting buy price and sell price from stockStartToLast
      double buyPrice = stockStartToLast.get(0).getOpen();
      double sellPrice = stockStartToLast.get(stockStartToLast.size() - 1).getClose();

      //Calculating years
      double yearsBetween = (double) ChronoUnit.DAYS.between(startDate, endDate) / 365.24;

      //Calculating totalReturn
      double totalReturn = (sellPrice - buyPrice) / buyPrice;

      //Calculating annualized return
      double annualizedReturn = Math.pow((1 + totalReturn), (1 / yearsBetween)) - 1;

      // annualizedReturnList.add(new AnnualizedReturn(symbol, annualizedReturn, totalReturns));
      return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
    } 
    catch (JsonProcessingException e) {
      return new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
  
  }



  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  //  CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, RestClientException, URISyntaxException, StockQuoteServiceException {

        return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?"
           + "startDate=" + startDate + "&endDate=" + endDate + "&token=65275bf240aea4c348da5b20ef7a38f309c31289";
            
       return uriTemplate;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {
        
        List<Future<AnnualizedReturn>> futureReturnList = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for(int i=0; i<portfolioTrades.size(); i++) {
          PortfolioTrade trade = portfolioTrades.get(i);
          Callable<AnnualizedReturn> callableTask = () -> {
            return getAnnualizedReturn(trade, endDate);
          };
          Future<AnnualizedReturn> futureReturn = executor.submit(callableTask);
          futureReturnList.add(futureReturn);
        }

        List<AnnualizedReturn> annualizedReturns = new ArrayList<>();

        for(int i=0; i<futureReturnList.size(); i++) {
          Future<AnnualizedReturn> futureReturn = futureReturnList.get(i);

          try {
            AnnualizedReturn returns = futureReturn.get();
            annualizedReturns.add(returns);
          }
          catch (ExecutionException e) {
            throw new StockQuoteServiceException("API response error", e);
          }
        }

        Collections.sort(annualizedReturns, getComparator());

    return annualizedReturns;
  }


  //  CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
