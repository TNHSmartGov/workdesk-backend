package com.tnh.baseware.core.dtos.basic;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransactionRequestBasicDTO {
    UUID id;
    String billNumber;
    Instant expDate;

}
