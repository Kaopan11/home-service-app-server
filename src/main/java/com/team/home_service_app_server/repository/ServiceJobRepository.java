package com.team.home_service_app_server.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.home_service_app_server.entity.ServiceJob;

public interface ServiceJobRepository extends JpaRepository<ServiceJob, Long> {

	@Query("""
			SELECT COUNT(job) FROM ServiceJob job
			WHERE job.status = com.team.home_service_app_server.entity.JobStatus.WAITING_ACCEPT
			  AND job.technician IS NULL
			  AND job.service.id IN :serviceIds
			""")
	long countWaitingAcceptByServiceIds(@Param("serviceIds") Collection<Long> serviceIds);

	@Query("""
			SELECT job FROM ServiceJob job
			JOIN FETCH job.service
			JOIN FETCH job.customer
			WHERE job.status = com.team.home_service_app_server.entity.JobStatus.WAITING_ACCEPT
			  AND job.technician IS NULL
			  AND job.service.id IN :serviceIds
			ORDER BY job.createdAt DESC
			""")
	List<ServiceJob> findWaitingAcceptByServiceIds(@Param("serviceIds") Collection<Long> serviceIds);
}
