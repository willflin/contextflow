package com.contextflow.content;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.content.dto.LearningUnitDetailResponse;
import com.contextflow.content.dto.LearningUnitSenseResponse;
import com.contextflow.content.service.LearningUnitQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/learning-units")
public class LearningUnitAdminController {

    private final LearningUnitQueryService learningUnitQueryService;

    public LearningUnitAdminController(LearningUnitQueryService learningUnitQueryService) {
        this.learningUnitQueryService = learningUnitQueryService;
    }

    @GetMapping("/search")
    public ApiResponse<LearningUnitDetailResponse> search(@RequestParam String text) {
        return ApiResponse.ok(learningUnitQueryService.search(text));
    }

    @GetMapping("/{id}")
    public ApiResponse<LearningUnitDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(learningUnitQueryService.detail(id));
    }

    @GetMapping("/{id}/senses")
    public ApiResponse<List<LearningUnitSenseResponse>> senses(@PathVariable Long id) {
        return ApiResponse.ok(learningUnitQueryService.senses(id));
    }
}
