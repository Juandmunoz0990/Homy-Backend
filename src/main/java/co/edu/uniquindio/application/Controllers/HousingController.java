package co.edu.uniquindio.application.Controllers;


import org.springframework.web.bind.annotation.*;

import co.edu.uniquindio.application.Services.HousingService;


@RestController
@RequestMapping("/housings")
public class HousingController {

    private final HousingService service;

    public HousingController(HousingService service) {
        this.service = service;
    }

    
}