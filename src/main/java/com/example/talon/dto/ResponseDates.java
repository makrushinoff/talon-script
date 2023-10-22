package com.example.talon.dto;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDates {

  private List<Row> rows;
  private List<Object> trows;
  private List<Object> freedatesforoffice;

}
