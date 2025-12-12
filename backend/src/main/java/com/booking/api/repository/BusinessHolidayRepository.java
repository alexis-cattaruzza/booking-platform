package com.booking.api.repository;

import com.booking.api.model.BusinessHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessHolidayRepository extends JpaRepository<BusinessHoliday, UUID> {

    /**
     * Find all holidays for a specific business
     */
    List<BusinessHoliday> findByBusinessIdOrderByStartDateAsc(UUID businessId);

    /**
     * Find holidays for a business that overlap with a specific date range
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.business.id = :businessId " +
           "AND h.endDate >= :startDate AND h.startDate <= :endDate " +
           "ORDER BY h.startDate ASC")
    List<BusinessHoliday> findByBusinessIdAndDateRange(
        @Param("businessId") UUID businessId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Check if a business has any holidays that include a specific date
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
           "FROM BusinessHoliday h WHERE h.business.id = :businessId " +
           "AND :date BETWEEN h.startDate AND h.endDate")
    boolean existsByBusinessIdAndDate(
        @Param("businessId") UUID businessId,
        @Param("date") LocalDate date
    );

    /**
     * Find all upcoming holidays for a business (from today onwards)
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.business.id = :businessId " +
           "AND h.endDate >= :today ORDER BY h.startDate ASC")
    List<BusinessHoliday> findUpcomingHolidaysByBusinessId(
        @Param("businessId") UUID businessId,
        @Param("today") LocalDate today
    );

    /**
     * Find current holiday for a business (if today falls within a holiday period)
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.business.id = :businessId " +
           "AND :today BETWEEN h.startDate AND h.endDate")
    List<BusinessHoliday> findCurrentHolidayByBusinessId(
        @Param("businessId") UUID businessId,
        @Param("today") LocalDate today
    );
}
