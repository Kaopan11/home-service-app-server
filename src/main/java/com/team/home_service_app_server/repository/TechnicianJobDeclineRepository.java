package com.team.home_service_app_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.TechnicianJobDecline;

public interface TechnicianJobDeclineRepository extends JpaRepository<TechnicianJobDecline, Long> {

	boolean existsByTechnician_UserIdAndJob_Id(Long technicianId, Long jobId);
}
