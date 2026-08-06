package com.taxin60sec.backend.controller;
import com.taxin60sec.backend.common.ApiResponse; import com.taxin60sec.backend.dto.business.*; import com.taxin60sec.backend.security.UserPrincipal; import com.taxin60sec.backend.service.BusinessProfileService; import io.swagger.v3.oas.annotations.Operation; import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.web.bind.annotation.*; import java.util.List;

@RestController @RequestMapping("/api/v1/businesses") @Tag(name="Business Profiles",description="Client business management")
public class BusinessProfileController {

    private final BusinessProfileService service;
    public BusinessProfileController(BusinessProfileService service){this.service=service;}

    @PostMapping @PreAuthorize("hasAnyRole('CA','ADMIN')") @Operation(summary="Create a business (staff)")
    public ApiResponse<BusinessProfileResponse> create(@Valid @RequestBody BusinessProfileRequest body,HttpServletRequest request){return ApiResponse.success("Business created",service.createBusiness(body),request.getRequestURI());}

    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('CA','ADMIN')") @Operation(summary="Get a business (staff)")
    public ApiResponse<BusinessProfileResponse> get(@PathVariable Long id,HttpServletRequest request){return ApiResponse.success("Business retrieved",service.getBusiness(id),request.getRequestURI());}

    @GetMapping("/client/{clientId}") @PreAuthorize("hasAnyRole('CA','ADMIN')") @Operation(summary="List a client's businesses (staff)")
    public ApiResponse<List<BusinessProfileResponse>> list(@PathVariable Long clientId,HttpServletRequest request){return ApiResponse.success("Businesses retrieved",service.getBusinessesForClient(clientId),request.getRequestURI());}

    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('CA','ADMIN')") @Operation(summary="Update a business (staff)")
    public ApiResponse<BusinessProfileResponse> update(@PathVariable Long id,@Valid @RequestBody BusinessProfileRequest body,HttpServletRequest request){return ApiResponse.success("Business updated",service.updateBusiness(id,body),request.getRequestURI());}

    @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('CA','ADMIN')") @Operation(summary="Delete a business (staff)")
    public ApiResponse<Void> delete(@PathVariable Long id,HttpServletRequest request){service.deleteBusiness(id);return ApiResponse.success("Business deleted",null,request.getRequestURI());}

    @PostMapping("/me") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="Create my own business")
    public ApiResponse<BusinessProfileResponse> createOwn(@Valid @RequestBody BusinessProfileRequest body, @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        return ApiResponse.success("Business created",service.createOwnBusiness(principal.getId(), body),request.getRequestURI());
    }

    @GetMapping("/me") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="List my own businesses")
    public ApiResponse<List<BusinessProfileResponse>> listOwn(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        return ApiResponse.success("Businesses retrieved",service.getOwnBusinesses(principal.getId()),request.getRequestURI());
    }

    @GetMapping("/me/{id}") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="Get one of my own businesses")
    public ApiResponse<BusinessProfileResponse> getOwn(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        return ApiResponse.success("Business retrieved",service.getOwnBusiness(principal.getId(), id),request.getRequestURI());
    }

    @PutMapping("/me/{id}") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="Update one of my own businesses")
    public ApiResponse<BusinessProfileResponse> updateOwn(@PathVariable Long id,@Valid @RequestBody BusinessProfileRequest body, @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        return ApiResponse.success("Business updated",service.updateOwnBusiness(principal.getId(), id, body),request.getRequestURI());
    }

    @DeleteMapping("/me/{id}") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="Delete one of my own businesses")
    public ApiResponse<Void> deleteOwn(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        service.deleteOwnBusiness(principal.getId(), id);
        return ApiResponse.success("Business deleted",null,request.getRequestURI());
    }

    @GetMapping("/me/command-center") @PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')") @Operation(summary="Aggregated view across all my entities")
    public ApiResponse<CommandCenterResponse> commandCenter(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request){
        return ApiResponse.success("Command center",service.commandCenterForUser(principal.getId()),request.getRequestURI());
    }
}