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
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.HousingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
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
        // Las imágenes son opcionales, solo validamos el máximo
        if (imagesUrls != null && imagesUrls.size() > 10) {
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
        
        // Validar imágenes: máximo 10
        validateImages(request.imagesUrls());
        
        // Obtener el housing existente para preservar campos que no deben cambiar
        Housing existingHousing = findById(housingId);
        
        // Preservar valores críticos antes de actualizar
        String preservedState = existingHousing.getState();
        if (preservedState == null || preservedState.trim().isEmpty()) {
            preservedState = Housing.STATE_ACTIVE;
        }
        Long preservedHostId = existingHousing.getHostId();
        if (preservedHostId == null) {
            preservedHostId = hostId; // Asegurar que hostId no sea null
        }
        Double preservedAverageRating = existingHousing.getAverageRating();
        
        // Actualizar solo los campos que vienen en el request
        existingHousing.setTitle(request.title());
        existingHousing.setDescription(request.description());
        existingHousing.setCity(request.city());
        existingHousing.setAddress(request.address());
        existingHousing.setLatitude(request.latitude());
        existingHousing.setLength(request.length());
        existingHousing.setNightPrice(request.pricePerNight());
        existingHousing.setMaxCapacity(request.maxCapacity());
        existingHousing.setServices(request.services());
        existingHousing.setImages(request.imagesUrls());
        
        // Actualizar principalImage si hay imágenes
        if (request.imagesUrls() != null && !request.imagesUrls().isEmpty()) {
            existingHousing.setPrincipalImage(request.imagesUrls().get(0));
        }
        
        // Restaurar campos críticos que NO deben cambiar (SIEMPRE)
        existingHousing.setState(preservedState);
        existingHousing.setHostId(preservedHostId);
        if (preservedAverageRating != null) {
            existingHousing.setAverageRating(preservedAverageRating);
        }
        
        // Asegurar que el ID no cambie
        existingHousing.setId(housingId);
        
        log.debug("Updating housing {} for host {} with state: {}", housingId, hostId, preservedState);
        
        housingRepository.save(existingHousing);
        return new EntityChangedResponse("Housing updated succesfully", Instant.now());
    }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getAllActiveHousings(Integer page, Integer size) {
         Integer pageNum = (page != null && page >= 0) ? page : 0;
         Integer pageSize = (size != null && size > 0) ? size : 20;
         Pageable pageable = PageRequest.of(pageNum, pageSize);
         
         log.info("Getting all active housings - page: {}, size: {}", pageNum, pageSize);
         
         // Buscar todas las propiedades activas (state = null, '', o 'active')
         Page<Housing> housings = housingRepository.findAllActive(pageable);
         
         log.info("Found {} active housings (total: {})", housings.getNumberOfElements(), housings.getTotalElements());
         
         // Loggear algunas propiedades para debugging
         if (housings.getTotalElements() > 0) {
             log.info("Sample active housings:");
             housings.getContent().stream()
                 .limit(3)
                 .forEach(h -> log.info("  ID {}: title='{}', city='{}', state='{}'", 
                     h.getId(), h.getTitle(), h.getCity(), h.getState()));
         } else {
             log.warn("⚠️ No active housings found! Check if properties exist and have correct state.");
         }
         
         return housings.map(housingMapper::toSummaryHousingResponse);
     }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
                                                              Double minPrice, Double maxPrice, Integer indexPage) {

         log.info("getHousingsByFilters called with: city={}, checkIn={}, checkOut={}, minPrice={}, maxPrice={}, indexPage={}", 
             city, checkIn, checkOut, minPrice, maxPrice, indexPage);

         LocalDate dateIn = (checkIn != null) ? checkIn : CHECK_IN_DEFAULT;
         LocalDate dateOut = (checkOut != null) ? checkOut : CHECK_OUT_DEFAULT;
         Double min = (minPrice != null && minPrice >= 0) ? minPrice : 0;
         Double max = (maxPrice != null && maxPrice >= 0 && maxPrice != minPrice) ? maxPrice : 50; 
         Integer index = (indexPage != null && indexPage >= 0) ? indexPage : 0;

         log.info("Using filters: city={}, dateIn={}, dateOut={}, min={}, max={}, index={}", 
             city, dateIn, dateOut, min, max, index);

         Pageable pageable = PageRequest.of(index, 20);

         Page<Housing> housings = housingRepository.findHousingsByFilters(city, dateIn, dateOut, min, max, pageable);
         
         log.info("Found {} housings with filters", housings.getTotalElements());

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
        
        try {
            // Intentar primero con query nativa para evitar problemas con relaciones lazy
            try {
                Page<Object[]> nativeResults = housingRepository.findByHostIdNative(hostId, pageable);
                
                if (nativeResults != null && !nativeResults.isEmpty()) {
                    return nativeResults.map(row -> {
                        Long id = ((Number) row[0]).longValue();
                        String title = (String) row[1];
                        String city = (String) row[2];
                        Double nightPrice = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                        String principalImage = (String) row[4];
                        Double averageRating = row[5] != null ? ((Number) row[5]).doubleValue() : null;
                        
                        return new SummaryHousingResponse(
                            id,
                            title != null ? title : "Untitled",
                            city != null ? city : "Unknown",
                            nightPrice,
                            principalImage,
                            averageRating
                        );
                    });
                }
            } catch (Exception nativeEx) {
                log.warn("Native query failed, falling back to JPQL: {}", nativeEx.getMessage());
            }
            
            // Fallback a query JPQL si la nativa falla
            Page<Housing> housings = housingRepository.findByHostId(hostId, pageable);
            
            if (housings == null || housings.isEmpty()) {
                return Page.empty(pageable);
            }
            
            // Mapear de forma segura, creando el DTO manualmente para evitar problemas con relaciones lazy
            return housings.map(housing -> {
                return new SummaryHousingResponse(
                    housing.getId() != null ? housing.getId() : 0L,
                    housing.getTitle() != null ? housing.getTitle() : "Untitled",
                    housing.getCity() != null ? housing.getCity() : "Unknown",
                    housing.getNightPrice() != null ? housing.getNightPrice() : 0.0,
                    housing.getPrincipalImage(),
                    housing.getAverageRating()
                );
            });
        } catch (Exception e) {
            log.error("Error fetching housings for host {}: {}", hostId, e.getMessage(), e);
            // Retornar página vacía en lugar de lanzar excepción
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HousingResponse getHousingDetail(Long housingId) {
        if (housingId == null || housingId <= 0) {
            throw new IllegalArgumentException("Housing ID must be positive");
        }

        log.info("Fetching housing detail for ID: {}", housingId);

        // Buscar la propiedad usando findByIdWithoutRelations para evitar problemas con ElementCollection
        Housing housing = housingRepository.findByIdWithoutRelations(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));
        
        // Verificar estado
        if (housing.getState() != null && housing.getState().equals("deleted")) {
            log.warn("Housing {} is deleted", housingId);
            throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
        }
        
        // Construir respuesta
        HousingResponse response = new HousingResponse();
        response.setId(housing.getId());
        response.setTitle(housing.getTitle() != null ? housing.getTitle() : "");
        response.setDescription(housing.getDescription() != null ? housing.getDescription() : "");
        response.setCity(housing.getCity() != null ? housing.getCity() : "");
        response.setAddress(housing.getAddress() != null ? housing.getAddress() : "");
        response.setLatitude(housing.getLatitude());
        response.setLength(housing.getLength());
        response.setNightPrice(housing.getNightPrice() != null ? housing.getNightPrice() : 0.0);
        response.setMaxCapacity(housing.getMaxCapacity() != null ? housing.getMaxCapacity() : 0);
        response.setAverageRating(housing.getAverageRating());
        
        // Intentar cargar servicios directamente desde la entidad (dentro de la transacción)
        try {
            List<co.edu.uniquindio.application.Models.enums.ServicesEnum> services = housing.getServices();
            if (services != null && !services.isEmpty()) {
                response.setServices(new ArrayList<>(services));
                log.debug("Loaded {} services directly from entity for housing {}", services.size(), housingId);
            } else {
                // Fallback: intentar con query nativa (intentar ambos nombres de tabla)
                try {
                    List<String> serviceStrings = housingRepository.findServicesByHousingId(housingId);
                    List<co.edu.uniquindio.application.Models.enums.ServicesEnum> servicesFromQuery = serviceStrings.stream()
                        .map(s -> {
                            try {
                                return co.edu.uniquindio.application.Models.enums.ServicesEnum.valueOf(s);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(s -> s != null)
                        .collect(java.util.stream.Collectors.toList());
                    response.setServices(servicesFromQuery);
                    log.debug("Loaded {} services from native query for housing {}", servicesFromQuery.size(), housingId);
                } catch (Exception e2) {
                    // Intentar con nombre snake_case
                    try {
                        List<String> serviceStrings = housingRepository.findServicesByHousingIdSnakeCase(housingId);
                        List<co.edu.uniquindio.application.Models.enums.ServicesEnum> servicesFromQuery = serviceStrings.stream()
                            .map(s -> {
                                try {
                                    return co.edu.uniquindio.application.Models.enums.ServicesEnum.valueOf(s);
                                } catch (IllegalArgumentException e) {
                                    return null;
                                }
                            })
                            .filter(s -> s != null)
                            .collect(java.util.stream.Collectors.toList());
                        response.setServices(servicesFromQuery);
                        log.debug("Loaded {} services from snake_case query for housing {}", servicesFromQuery.size(), housingId);
                    } catch (Exception e3) {
                        log.debug("Could not load services from native queries for housing {}: {}", housingId, e3.getMessage());
                        response.setServices(new ArrayList<>());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not load services for housing {}: {}", housingId, e.getMessage());
            response.setServices(new ArrayList<>());
        }
        
        // Intentar cargar imágenes directamente desde la entidad (dentro de la transacción)
        try {
            List<String> images = housing.getImages();
            if (images != null && !images.isEmpty()) {
                response.setImages(new ArrayList<>(images));
                log.debug("Loaded {} images directly from entity for housing {}", images.size(), housingId);
            } else {
                // Fallback: usar principalImage o query nativa
                String principalImage = housing.getPrincipalImage();
                if (principalImage != null && !principalImage.trim().isEmpty()) {
                    response.setImages(new ArrayList<>(List.of(principalImage)));
                    log.debug("Using principalImage for housing {}", housingId);
                } else {
                    // Último fallback: query nativa (intentar ambos nombres de tabla)
                    try {
                        List<String> imagesFromQuery = housingRepository.findImagesByHousingId(housingId);
                        if (!imagesFromQuery.isEmpty()) {
                            response.setImages(new ArrayList<>(imagesFromQuery));
                            log.debug("Loaded {} images from native query for housing {}", imagesFromQuery.size(), housingId);
                        } else {
                            response.setImages(new ArrayList<>());
                        }
                    } catch (Exception e2) {
                        // Intentar con nombre snake_case
                        try {
                            List<String> imagesFromQuery = housingRepository.findImagesByHousingIdSnakeCase(housingId);
                            if (!imagesFromQuery.isEmpty()) {
                                response.setImages(new ArrayList<>(imagesFromQuery));
                                log.debug("Loaded {} images from snake_case query for housing {}", imagesFromQuery.size(), housingId);
                            } else {
                                response.setImages(new ArrayList<>());
                            }
                        } catch (Exception e3) {
                            log.debug("Could not load images from native queries for housing {}: {}", housingId, e3.getMessage());
                            response.setImages(new ArrayList<>());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not load images for housing {}: {}", housingId, e.getMessage());
            String principalImage = housing.getPrincipalImage();
            response.setImages(principalImage != null && !principalImage.trim().isEmpty() 
                ? new ArrayList<>(List.of(principalImage)) 
                : new ArrayList<>());
        }
        
        response.setBookingsList(null);
        response.setCommentsList(null);
        
        // Obtener nombre del host
        try {
            if (housing.getHostId() != null) {
                User user = userService.findById(housing.getHostId());
                response.setHostName(user != null && user.getName() != null ? user.getName() : "Host");
            } else {
                response.setHostName("Host");
            }
        } catch (Exception e) {
            log.debug("Could not load host name for housing {}: {}", housingId, e.getMessage());
            response.setHostName("Host");
        }
        
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
        if (totalBookings == null) {
            totalBookings = 0L;
        }
        
        // Calcular promedio de calificaciones
        Double averageRating = null;
        
        // Si no hay filtros de fecha, usar el promedio del alojamiento directamente
        if (dateFrom == null && dateTo == null) {
            averageRating = housing.getAverageRating() != null ? housing.getAverageRating() : 0.0;
        } else {
            // Si hay filtros de fecha, calcular manualmente filtrando en memoria
            try {
                List<Comment> allComments = commentRepository.findAllByHousingId(housingId);
                if (!allComments.isEmpty()) {
                    java.time.LocalDate dateFromLocal = dateFrom;
                    java.time.LocalDate dateToLocal = dateTo;
                    
                    List<Comment> filteredComments = allComments.stream()
                        .filter(c -> {
                            if (dateFromLocal != null && c.getCreatedAt().toLocalDate().isBefore(dateFromLocal)) {
                                return false;
                            }
                            if (dateToLocal != null && c.getCreatedAt().toLocalDate().isAfter(dateToLocal)) {
                                return false;
                            }
                            return true;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!filteredComments.isEmpty()) {
                        averageRating = filteredComments.stream()
                            .mapToInt(Comment::getRate)
                            .average()
                            .orElse(0.0);
                    }
                }
            } catch (Exception e) {
                // Si hay error, usar el promedio del alojamiento
                averageRating = null;
            }
        }
        
        // Si no hay calificaciones o hubo error, usar el promedio general del alojamiento
        if (averageRating == null || averageRating == 0.0) {
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


