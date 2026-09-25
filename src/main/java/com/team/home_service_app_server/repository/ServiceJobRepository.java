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
			  AND NOT EXISTS (
			      SELECT decline.id FROM TechnicianJobDecline decline
			      WHERE decline.job.id = job.id
			        AND decline.technician.userId = :technicianId
			  )
			""")
	long countWaitingAcceptByServiceIds(
			@Param("serviceIds") Collection<Long> serviceIds,
			@Param("technicianId") Long technicianId);

	@Query("""
			SELECT job FROM ServiceJob job
			JOIN FETCH job.service
			JOIN FETCH job.customer
			WHERE job.status = com.team.home_service_app_server.entity.JobStatus.WAITING_ACCEPT
			  AND job.technician IS NULL
			  AND job.service.id IN :serviceIds
			  AND NOT EXISTS (
			      SELECT decline.id FROM TechnicianJobDecline decline
			      WHERE decline.job.id = job.id
			        AND decline.technician.userId = :technicianId
			  )
			ORDER BY job.createdAt DESC
			""")
	List<ServiceJob> findWaitingAcceptByServiceIds(
			@Param("serviceIds") Collection<Long> serviceIds,
			@Param("technicianId") Long technicianId);

	@Query("""
			SELECT job FROM ServiceJob job
			JOIN FETCH job.service s
			JOIN FETCH s.category
			JOIN FETCH job.customer
			WHERE job.status = com.team.home_service_app_server.entity.JobStatus.ACCEPTED
			  AND job.technician.userId = :technicianId
			ORDER BY 
				CASE WHEN :sort = 'soonest' THEN job.scheduledAt END ASC NULLS LAST,
				CASE WHEN :sort != 'soonest' THEN job.createdAt END DESC
			""")
	List<ServiceJob> findPendingByTechnician(
			@Param("technicianId") Long technicianId,
			@Param("sort") String sort);

	@Query("""
			SELECT job FROM ServiceJob job
			JOIN FETCH job.service s
			JOIN FETCH s.category
			JOIN FETCH job.customer
			WHERE job.status = com.team.home_service_app_server.entity.JobStatus.COMPLETED
			  AND job.technician.userId = :technicianId
			ORDER BY job.scheduledAt DESC NULLS LAST, job.updatedAt DESC
			""")
	List<ServiceJob> findHistoryByTechnician(@Param("technicianId") Long technicianId);

	@Query("""
			SELECT job FROM ServiceJob job
			JOIN FETCH job.service s
			JOIN FETCH s.category
			JOIN FETCH job.customer
			WHERE job.id = :jobId
			  AND job.technician.userId = :technicianId
			""")
	java.util.Optional<ServiceJob> findByIdAndTechnician(
			@Param("jobId") Long jobId,
			@Param("technicianId") Long technicianId);
}
