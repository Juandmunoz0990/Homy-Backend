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
      public ResponseEntity<EntityCreatedResponse> createHousing (@AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateOrEditHousingRequest request){
         Long hostId = user.getId();
         EntityCreatedResponse response = service.create(hostId, request);
         return ResponseEntity.created(URI.create("/housings")).body(response);
    }

    @DeleteMapping("/delete/{housingId}")
    public ResponseEntity<EntityChangedResponse> deleteHousing (@PathVariable Long housingId, @AuthenticationPrincipal CustomUserDetails user){
        Long hostId = user.getId();
        EntityChangedResponse response = service.delete(housingId, hostId);
        return ResponseEntity.ok(response);

    }

    @PostMapping("/edit/{housingId}")
    public ResponseEntity<EntityChangedResponse> editHousing (@PathVariable Long housingId, @AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateOrEditHousingRequest request){
       Long hostId = user.getId();
       EntityChangedResponse response = service.edit(housingId, hostId, request);
       return ResponseEntity.ok(response);

    }

      @GetMapping
      public ResponseEntity<Page<SummaryHousingResponse>> getHousings (@RequestParam String city, @RequestParam LocalDate checkIn, @RequestParam LocalDate checkOut,
                                                             @RequestParam Integer minPrice, @RequestParam Integer maxPrice , @RequestParam Integer indexPage){
        Page<SummaryHousingResponse> response = service.getHousingsByFilters(city, checkIn, checkOut, minPrice, maxPrice, indexPage);
        return ResponseEntity.ok(response);
      }
    
}