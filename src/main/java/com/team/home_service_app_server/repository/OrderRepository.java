package com.team.home_service_app_server.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.home_service_app_server.entity.CustomerOrder;
import com.team.home_service_app_server.entity.OrderAssignment;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

	@Query("""
			SELECT DISTINCT orderRow FROM CustomerOrder orderRow
			LEFT JOIN FETCH orderRow.items item
			LEFT JOIN FETCH item.option
			WHERE orderRow.customer.userId = :userId
			ORDER BY orderRow.createdAt DESC
			""")
	List<CustomerOrder> findCardsByCustomerId(@Param("userId") Long userId);

	@Query("""
			SELECT assignment FROM OrderAssignment assignment
			JOIN FETCH assignment.technician technician
			JOIN FETCH technician.user
			JOIN FETCH assignment.customerOrder
			WHERE assignment.customerOrder.id IN :orderIds
			ORDER BY assignment.assignedAt DESC, assignment.id DESC
			""")
	List<OrderAssignment> findAssignmentsByOrderIds(@Param("orderIds") Collection<Long> orderIds);
}
