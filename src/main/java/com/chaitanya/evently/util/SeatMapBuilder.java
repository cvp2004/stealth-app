package com.chaitanya.evently.util;

import com.chaitanya.evently.dto.seat.SeatResponse;
import com.chaitanya.evently.dto.seat.map.SeatMapResponse;
import com.chaitanya.evently.repository.projection.SeatFlatProjection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for building seat map responses from seat projections
 */
public final class SeatMapBuilder {

    private SeatMapBuilder() {
        // Utility class - prevent instantiation
    }

    /**
     * Builds a list of sections from seat projections
     * 
     * @param seats List of seat projections
     * @return List of section DTOs
     */
    public static List<SeatMapResponse.Section> buildSections(List<SeatFlatProjection> seats) {
        Map<String, Map<String, List<SeatFlatProjection>>> sectionsGrouped = groupSeatsBySectionAndRow(seats);
        return sectionsGrouped.entrySet().stream()
                .map(SeatMapBuilder::buildSection)
                .toList();
    }

    /**
     * Groups seats by section and then by row
     * 
     * @param seats List of seat projections
     * @return Nested map: section -> row -> list of seats
     */
    private static Map<String, Map<String, List<SeatFlatProjection>>> groupSeatsBySectionAndRow(
            List<SeatFlatProjection> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(SeatFlatProjection::section,
                        Collectors.groupingBy(SeatFlatProjection::row)));
    }

    /**
     * Builds a section DTO from section entry
     * 
     * @param sectionEntry Map entry containing section data
     * @return Section DTO
     */
    private static SeatMapResponse.Section buildSection(
            Map.Entry<String, Map<String, List<SeatFlatProjection>>> sectionEntry) {
        List<SeatMapResponse.Row> rows = sectionEntry.getValue().entrySet().stream()
                .map(SeatMapBuilder::buildRow)
                .toList();

        return SeatMapResponse.Section.builder()
                .sectionId(sectionEntry.getKey())
                .rows(rows)
                .build();
    }

    /**
     * Builds a row DTO from row entry
     * 
     * @param rowEntry Map entry containing row data
     * @return Row DTO
     */
    private static SeatMapResponse.Row buildRow(Map.Entry<String, List<SeatFlatProjection>> rowEntry) {
        List<SeatResponse> seatDtos = rowEntry.getValue().stream()
                .map(SeatMapBuilder::buildSeatResponse)
                .toList();

        return SeatMapResponse.Row.builder()
                .rowId(rowEntry.getKey())
                .seats(seatDtos)
                .build();
    }

    /**
     * Builds a seat response DTO from seat projection
     * 
     * @param seat Seat projection
     * @return Seat response DTO
     */
    private static SeatResponse buildSeatResponse(SeatFlatProjection seat) {
        return SeatResponse.builder()
                .id(seat.id())
                .section(seat.section())
                .row(seat.row())
                .seatNumber(seat.seatNumber())
                .build();
    }
}
