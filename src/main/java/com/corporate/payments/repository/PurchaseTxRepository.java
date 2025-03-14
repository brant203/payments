package com.corporate.payments.repository;

import com.corporate.payments.valueObject.PurchaseTxVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseTxRepository extends JpaRepository<PurchaseTxVO, Long> {
}
