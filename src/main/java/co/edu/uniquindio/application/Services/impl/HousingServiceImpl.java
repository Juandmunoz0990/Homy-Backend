package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.AvailabilityCalendarResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Exception.HousingUndeletedException;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.HousingMapper;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class HousingServiceImpl implements HousingService {

    private final HousingRepository housingRepository;
    private final HousingMapper housingMapper;
    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private static final LocalDate CHECK_IN_DEFAULT = LocalDate.now();
    private static final LocalDate CHECK_OUT_DEFAULT = LocalDate.now().plusDays(1);

    @Override
    public EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request) {
        // Validar imágenes: mínimo 1, máximo 10
        validateImages(request.imagesUrls());
        
        Housing housing = housingMapper.toHousing(request);
        housing.setHostId(hostId);
        housingRepository.save(housing);
        return new EntityCreatedResponse("Housing created successfully", Instant.now());
    }
    
    /**
     * Valida que haya entre 1 y 10 imágenes, y que haya una imagen principal
     */
    private void validateImages(List<String> imagesUrls) {
        if (imagesUrls == null || imagesUrls.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos 1 imagen");
        }
        
        if (imagesUrls.size() > 10) {
            throw new IllegalArgumentException("No se pueden subir más de 10 imágenes");
        }
    }

    @Override
    public EntityChangedResponse delete(Long housingId, Long hostId) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }

        if (bookingService.existsFutureBookingsForHousing(housingId)) {
            throw new HousingUndeletedException("Housing with id: " + housingId + "and hostId: " + hostId + " has pending bookings");
        }
        housingRepository.softDeleteByIdAndHostId(housingId, hostId);;
        return new EntityChangedResponse("Housing deleted successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        
        // Validar imágenes: mínimo 1, máximo 10
        validateImages(request.imagesUrls());
        
        Housing housing = housingMapper.toHousing(request);
        housing.setId(housingId);
        housingRepository.save(housing);
        return new EntityChangedResponse("Housing updated succesfully", Instant.now());
    }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
                                                              Double minPrice, Double maxPrice, Integer indexPage) {

         LocalDate dateIn = (checkIn != null) ? checkIn : CHECK_IN_DEFAULT;
         LocalDate dateOut = (checkOut != null) ? checkOut : CHECK_OUT_DEFAULT;
         Double min = (minPrice != null && minPrice >= 0) ? minPrice : 0;
         Double max = (maxPrice != null && maxPrice >= 0 && maxPrice != minPrice) ? maxPrice : 50; 
         Integer index = (indexPage != null && indexPage >= 0) ? indexPage : 0;

         Pageable pageable = PageRequest.of(index, 20);

         Page<Housing> housings = housingRepository.findHousingsByFilters(city, dateIn, dateOut, min, max, pageable);

         return housings.map(housingMapper::toSummaryHousingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SummaryHousingResponse> getHousingsByHost(Long hostId, Integer page, Integer size) {
        if (hostId == null || hostId <= 0) {
            throw new IllegalArgumentException("The hostId must be positive");
        }
        
        Integer pageIndex = (page != null && page >= 0) ? page : 0;
        Integer pageSize = (size != null && size > 0) ? size : 10;
        
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Housing> housings = housingRepository.findByHostId(hostId, pageable);
        
        return housings.map(housingMapper::toSummaryHousingResponse);
    }

    @Override
@Transactional(readOnly = true)
public HousingResponse getHousingDetail(Long housingId) {

    Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));

    HousingResponse response = housingMapper.toHousingResponse(housing);

    User user = userService.findById(housing.getHostId());
    response.setHostName(user.getName());

    return response;
}

    @Override
     @Transactional(readOnly = true)
    public Housing findById(Long id) {
        return housingRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + id + " not found", Housing.class));
    }

    @Override
    public Boolean existsHousing(Long housingId, Long hostId) {
        if (housingId == null || housingId <= 0) {
            throw new IllegalArgumentException("The housingId must be positive");
        }

        if (hostId == null || hostId <= 0) {
            throw new IllegalArgumentException("The hostId must be positive");
        }

        return housingRepository.existsByIdAndHostId(housingId, hostId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public HousingMetricsResponse getHousingMetrics(Long housingId, Long hostId, LocalDate dateFrom, LocalDate dateTo) {
        // Verificar que el alojamiento existe y pertenece al host
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        
        Housing housing = findById(housingId);
        
        // Contar reservas en el rango de fechas
        Long totalBookings = bookingRepository.countBookingsByHousingAndDateRange(housingId, dateFrom, dateTo);
        
        // Calcular promedio de calificaciones en el rango de fechas
        java.time.LocalDateTime dateFromDateTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        java.time.LocalDateTime dateToDateTime = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        Double averageRating = commentRepository.calculateAverageRatingByHousingAndDateRange(
            housingId, dateFromDateTime, dateToDateTime
        );
        
        // Si no hay calificaciones, usar el promedio general del alojamiento
        if (averageRating == null) {
            averageRating = housing.getAverageRating() != null ? housing.getAverageRating() : 0.0;
        }
        
        return new HousingMetricsResponse(
            housingId,
            housing.getTitle(),
            totalBookings != null ? totalBookings : 0L,
            averageRating,
            dateFrom,
            dateTo
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public AvailabilityCalendarResponse getAvailabilityCalendar(Long housingId, LocalDate startDate, LocalDate endDate) {
        Housing housing = findById(housingId);
        
        // Si no se proporcionan fechas, usar el próximo año
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusYears(1);
        }
        
        // Obtener rangos de fechas ocupadas
        List<Object[]> bookedRanges = bookingRepository.findBookedDateRanges(housingId, startDate, endDate);
        
        // Generar lista de todas las fechas ocupadas
        Set<LocalDate> bookedDates = new HashSet<>();
        for (Object[] range : bookedRanges) {
            LocalDate checkIn = (LocalDate) range[0];
            LocalDate checkOut = (LocalDate) range[1];
            
            // Agregar todas las fechas entre checkIn (inclusive) y checkOut (exclusive)
            LocalDate current = checkIn;
            while (current.isBefore(checkOut) && !current.isAfter(endDate) && !current.isBefore(startDate)) {
                bookedDates.add(current);
                current = current.plusDays(1);
            }
        }
        
        // Generar lista de fechas disponibles
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!bookedDates.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }
        
        return new AvailabilityCalendarResponse(
            housingId,
            housing.getTitle(),
            new ArrayList<>(bookedDates),
            availableDates
        );
    }
}


