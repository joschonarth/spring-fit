package br.com.joschonarth.springfit.controller;

import br.com.joschonarth.springfit.dto.request.PhysicalAssessmentRequestDTO;
import br.com.joschonarth.springfit.dto.projection.PhysicalAssessmentProjection;
import br.com.joschonarth.springfit.exception.NotFoundException;
import br.com.joschonarth.springfit.service.PhysicalAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Physical Assessment", description = "Endpoints for physical assessment management")
@RestController
@RequestMapping("v1/assessment")
@RequiredArgsConstructor
public class PhysicalAssessmentController {

    private final PhysicalAssessmentService physicalAssessmentService;

    @Operation(summary = "Create a new physical assessment", description = "Only ADMIN can create physical assessments")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Physical assessment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can create assessments"),
            @ApiResponse(responseCode = "404", description = "Student not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPhysicalAssessment(@Valid @RequestBody PhysicalAssessmentRequestDTO physicalAssessmentRequestDTO) throws NotFoundException {
        physicalAssessmentService.createPhysicalAssessment(physicalAssessmentRequestDTO);
    }

    @Operation(summary = "List all physical assessments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PhysicalAssessmentProjection> getAllAssessments() {
        return physicalAssessmentService.getAllAssessments();
    }

    @Operation(summary = "List all physical assessments with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("page/{page}/size/{size}")
    @ResponseStatus(HttpStatus.OK)
    public Page<PhysicalAssessmentProjection> getAllAssessmentsPageable(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @PathVariable Integer page,
            @Parameter(description = "Page size", example = "10")
            @PathVariable Integer size) {
        return physicalAssessmentService.getAllAssessmentsPageable(page, size);
    }
}
