package com.example.talon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuccessFindTicketDto {

  private Long ticketId;
  private String webchsid;
  private LocalDate date;

}
