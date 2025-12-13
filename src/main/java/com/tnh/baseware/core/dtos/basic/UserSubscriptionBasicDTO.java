package com.tnh.baseware.core.dtos.basic;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSubscriptionBasicDTO {
    UUID id;
    CustomerBasicDTO customer;
    ServicePackageBasicDTO servicePackage;
    ServicePackageOptionBasicDTO servicePackageOption;

    Instant startDate;
    Instant endDate;
    String status;
    BigDecimal basePrice;
    BigDecimal finalPrice;
    BigDecimal totalAmount;
    BigDecimal paidAmount;
    Instant lastPaymentDate;
    String paymentStatus;
    BigDecimal discountAmount;
    BigDecimal discountPercentage;
    BigDecimal commissionAmount;
    BigDecimal commissionPercentage;
    Integer registrationYears;
    Instant renewedAt;
}
