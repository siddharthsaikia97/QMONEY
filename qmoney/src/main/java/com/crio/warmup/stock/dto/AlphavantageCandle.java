// CRIO_SOLUTION_START_MODULE_ADDITIONAL_REFACTOR

package com.crio.warmup.stock.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// CRIO_SOLUTION_END_MODULE_ADDITIONAL_REFACTOR


//  CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
//  Implement the Candle interface in such a way that it matches the parameters returned
//  inside Json response from Alphavantage service.

// CRIO_UNCOMMENT_START_MODULE_ADDITIONAL_REFACTOR
// // Reference - https://www.baeldung.com/jackson-ignore-properties-on-serialization
// // Reference - https://www.baeldung.com/jackson-name-of-property
//public class AlphavantageCandle implements Candle {
//  @JsonProperty("1. open")
//  private Double open;
//  private Double close;
//  private Double high;
//  private Double low;
//  private Date date;
//}
// CRIO_UNCOMMENT_END_MODULE_ADDITIONAL_REFACTOR

// 
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphavantageCandle implements Candle {

  @JsonProperty("1. open")
  private Double open;
  @JsonProperty("4. close")
  private Double close;
  @JsonProperty("2. high")
  private Double high;
  @JsonProperty("3. low")
  private Double low;
  @JsonProperty("timestamp")
  private LocalDate date;

  @Override
  public Double getOpen() {
    return open;
  }

  public void setOpen(Double open) {
    this.open = open;
  }

  @Override
  public Double getClose() {
    return close;
  }

  public void setClose(Double close) {
    this.close = close;
  }

  @Override
  public Double getHigh() {
    return high;
  }

  public void setHigh(Double high) {
    this.high = high;
  }

  @Override
  public Double getLow() {
    return low;
  }

  public void setLow(Double low) {
    this.low = low;
  }

  @Override
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }
}

