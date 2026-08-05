package com.workbridge.server.mapper.sales;

import com.workbridge.server.domain.sales.CustomerRecord;
import com.workbridge.server.domain.sales.CustomerStatus;
import com.workbridge.server.domain.sales.CustomerUpdateCommand;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CustomerMapper {

    List<CustomerRecord> selectPage(
            @Param("customerName") String customerName,
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("contactName") String contactName,
            @Param("assignedSalesId") Long assignedSalesId,
            @Param("status") CustomerStatus status,
            @Param("active") Boolean active,
            @Param("offset") int offset,
            @Param("size") int size
    );

    long countPage(
            @Param("customerName") String customerName,
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("contactName") String contactName,
            @Param("assignedSalesId") Long assignedSalesId,
            @Param("status") CustomerStatus status,
            @Param("active") Boolean active
    );

    CustomerRecord selectById(@Param("customerId") Long customerId);

    List<CustomerRecord> selectOptions(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    int countByBusinessRegistrationNo(@Param("businessRegistrationNo") String businessRegistrationNo);

    int countByBusinessRegistrationNoExcludingCustomer(
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("customerId") Long customerId
    );

    int insert(@Param("customer") CustomerRecord customer, @Param("actorId") Long actorId);

    int update(CustomerUpdateCommand command);
}
