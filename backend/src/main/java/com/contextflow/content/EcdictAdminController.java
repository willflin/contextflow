package com.contextflow.content;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.content.dto.EcdictImportResponse;
import com.contextflow.content.service.EcdictImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ecdict")
public class EcdictAdminController {

    private final EcdictImportService ecdictImportService;

    public EcdictAdminController(EcdictImportService ecdictImportService) {
        this.ecdictImportService = ecdictImportService;
    }

    @PostMapping("/import-local")
    public ApiResponse<EcdictImportResponse> importLocal() {
        return ApiResponse.ok(ecdictImportService.importLocalCsv());
    }
}
