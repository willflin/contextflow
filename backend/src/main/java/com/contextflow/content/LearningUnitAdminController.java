package com.contextflow.content;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.content.dto.AdminSenseUpdateRequest;
import com.contextflow.content.dto.AdminWordCreateRequest;
import com.contextflow.content.dto.AdminWordUpdateRequest;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.content.dto.LearningUnitSenseResponse;
import com.contextflow.content.service.LearningUnitAdminService;
import com.contextflow.content.service.LearningUnitQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/learning-units")
public class LearningUnitAdminController {

    private final LearningUnitQueryService learningUnitQueryService;
    private final LearningUnitAdminService learningUnitAdminService;

    public LearningUnitAdminController(
            LearningUnitQueryService learningUnitQueryService,
            LearningUnitAdminService learningUnitAdminService
    ) {
        this.learningUnitQueryService = learningUnitQueryService;
        this.learningUnitAdminService = learningUnitAdminService;
    }

    @GetMapping("/search")
    public ApiResponse<LearningUnitDetailResponse> search(@RequestParam String text) {
        return ApiResponse.ok(learningUnitQueryService.searchForAdmin(text));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningUnitDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(learningUnitQueryService.detailForAdmin(id));
    }

    @GetMapping("/{id}/senses")
    public ApiResponse<List<LearningUnitSenseResponse>> senses(@PathVariable Long id) {
        return ApiResponse.ok(learningUnitQueryService.sensesForAdmin(id));
    }

    @PostMapping("/words")
    public ApiResponse<LearningUnitDetailResponse> createWord(
            @Valid @RequestBody AdminWordCreateRequest request
    ) {
        return ApiResponse.ok(learningUnitAdminService.createWord(request));
    }

    @PutMapping("/words/{id}")
    public ApiResponse<LearningUnitDetailResponse> updateWord(
            @PathVariable Long id,
            @Valid @RequestBody AdminWordUpdateRequest request
    ) {
        return ApiResponse.ok(learningUnitAdminService.updateWord(id, request));
    }

    @PutMapping("/senses/{senseId}")
    public ApiResponse<LearningUnitDetailResponse> updateSense(
            @PathVariable Long senseId,
            @Valid @RequestBody AdminSenseUpdateRequest request
    ) {
        return ApiResponse.ok(learningUnitAdminService.updateSense(senseId, request));
    }

    @DeleteMapping("/words/{id}")
    public ApiResponse<Void> deleteWord(@PathVariable Long id) {
        learningUnitAdminService.deleteWord(id);
        return ApiResponse.ok(null);
    }
}
