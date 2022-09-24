
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.management.RuntimeErrorException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
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
      LocalDate endDate) {


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
  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) {
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

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {

        //if to date before from, then throwing error
        if (from.compareTo(to) > 0) {
          throw new RuntimeErrorException(null);
        }
    
        //Calling api to get stock data for each day
        TiingoCandle[] candlesHelper =
            restTemplate.getForObject(buildUri(symbol, from, to), TiingoCandle[].class);
        
        //Application will crash if we perform Arrays.asList when candlesHelper is null.
        //This might be the case when Tiigo server is down.
        if (candlesHelper == null) {
          return new ArrayList<Candle>();
        }
        else {
          List<Candle> candles = Arrays.asList(candlesHelper);
          return candles;
        }
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?"
           + "startDate=" + startDate + "&endDate=" + endDate + "&token=65275bf240aea4c348da5b20ef7a38f309c31289";
            
       return uriTemplate;
  }
}