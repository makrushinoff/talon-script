package com.example.talon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketReservedDto {

  private String token;
  private String webchsid;
  private String value;
  private String referer;

}
