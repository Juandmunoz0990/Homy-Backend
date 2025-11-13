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
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Security.CustomUserDetails;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.Models.enums.ServicesEnum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/housings")
public class HousingController {

    private final HousingService service;

    public HousingController(HousingService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<EntityCreatedResponse> createHousing(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateOrEditHousingRequest request) {
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
            @AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody CreateOrEditHousingRequest request) {
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
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) java.util.List<ServicesEnum> services,
            @RequestParam(required = false, name = "page") Integer page) {
        Page<SummaryHousingResponse> response = service.getHousingsByFilters(city, checkIn, checkOut, minPrice,
                maxPrice, guests, services, page);
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
    public ResponseEntity<HousingMetricsResponse> getHousingMetrics(
            @PathVariable Long housingId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        HousingMetricsResponse response = service.getHousingMetrics(user.getId(), housingId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

}
