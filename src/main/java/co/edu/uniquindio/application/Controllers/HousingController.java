package co.edu.uniquindio.application.Controllers;

import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.HousingService;

@RestController
@RequestMapping("/housings")
public class HousingController {

    private final HousingService service;

    public HousingController(HousingService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<EntityCreatedResponse> createHousing(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody CreateOrEditHousingRequest request) {
        Long hostId = user.getId();
        EntityCreatedResponse response = service.create(hostId, request);
        return ResponseEntity.created(URI.create("/housings")).body(response);
    }

    @DeleteMapping("/delete/{housingId}")
    public ResponseEntity<EntityChangedResponse> deleteHousing(@PathVariable Long housingId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long hostId = user.getId();
        EntityChangedResponse response = service.delete(housingId, hostId);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/edit/{housingId}")
    public ResponseEntity<EntityChangedResponse> editHousing(@PathVariable Long housingId,
            @AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateOrEditHousingRequest request) {
        Long hostId = user.getId();
        EntityChangedResponse response = service.edit(housingId, hostId, request);
        return ResponseEntity.ok(response);

    }

    @GetMapping
    public ResponseEntity<Page<SummaryHousingResponse>> getHousings(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LocalDate checkIn, 
            @RequestParam(required = false) LocalDate checkOut,
            @RequestParam(required = false) Double minPrice, 
            @RequestParam(required = false) Double maxPrice, 
            @RequestParam(required = false, defaultValue = "0") Integer indexPage,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        
        // Si no hay filtros, devolver todas las propiedades activas
        if (city == null && checkIn == null && checkOut == null && minPrice == null && maxPrice == null) {
            Page<SummaryHousingResponse> response = service.getAllActiveHousings(indexPage, size);
            return ResponseEntity.ok(response);
        }
        
        // Si hay filtros, usar la búsqueda con filtros
        String cityParam = (city != null && !city.trim().isEmpty()) ? city.trim() : null;
        Page<SummaryHousingResponse> response = service.getHousingsByFilters(cityParam, checkIn, checkOut, minPrice,
                maxPrice, indexPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/host/{hostId}")
    public ResponseEntity<Page<SummaryHousingResponse>> getHousingsByHost(
            @PathVariable("hostId") Long hostId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        Page<SummaryHousingResponse> response = service.getHousingsByHost(hostId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{housingId}")
    public ResponseEntity<HousingResponse> getHousingDetail(@PathVariable Long housingId) {
        HousingResponse response = service.getHousingDetail(housingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{housingId}/metrics")
    public ResponseEntity<co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse> getHousingMetrics(
            @PathVariable Long housingId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) java.time.LocalDate dateFrom,
            @RequestParam(required = false) java.time.LocalDate dateTo) {
        Long hostId = user.getId();
        co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse response = 
            service.getHousingMetrics(housingId, hostId, dateFrom, dateTo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{housingId}/availability")
    public ResponseEntity<co.edu.uniquindio.application.Dtos.Housing.Responses.AvailabilityCalendarResponse> getAvailabilityCalendar(
            @PathVariable Long housingId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate) {
        co.edu.uniquindio.application.Dtos.Housing.Responses.AvailabilityCalendarResponse response = 
            service.getAvailabilityCalendar(housingId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

}