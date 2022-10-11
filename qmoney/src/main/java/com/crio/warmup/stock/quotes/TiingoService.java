
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.RuntimeErrorException;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {

    //Check whther from is less than to
    if(from.isAfter(to)) {
      throw new RuntimeErrorException(null);
    }

    //Calling API and storing the data
    /* (alternate way)
        ResponseEntity<List<TiingoCandle>> candlesHelper = restTemplate.exchange(
              buildUri(symbol, from, to), HttpMethod.GET, null, 
              new ParameterizedTypeReference<List<TiingoCandle>>() {});
        List<TiingoCandle> candles = candlesHelper.getBody();
     */
    String response = restTemplate.getForObject(buildUri(symbol, from, to), String.class);
    ObjectMapper mapper = getObjectMapper();
    
    TiingoCandle[] candlesHelper = mapper.readValue(response, TiingoCandle[].class);
    //TiingoCandle[] candlesHelper = 
    //  restTemplate.getForObject(buildUri(symbol, from, to), TiingoCandle[].class);

    List<Candle> candles = new ArrayList<>(); 

    //Application will crash if candlesHelper is null
    if (candlesHelper == null) {
      return new ArrayList<Candle>();
    }
    
    candles = Arrays.asList(candlesHelper);    

    //Sorting the list
    candles.sort((a,b) -> { return a.getDate().compareTo(b.getDate());});

    return candles;

  }

  public static ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    return mapper;
  }

  //uri builder
  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+ symbol +"/prices?"
        + "startDate=" + startDate + "&endDate=" + endDate + "&token=65275bf240aea4c348da5b20ef7a38f309c31289";
         
    return uriTemplate;
}

}
