package br.com.houzelcompiler.houzelcompiler.controller;

import br.com.houzelcompiler.houzelcompiler.model.EvaluationResponse;
import br.com.houzelcompiler.houzelcompiler.service.EvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/evaluate")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<?> evaluateEssay(
            @RequestParam("images") MultipartFile[] images,
            @RequestParam(value = "aux_prompt", required = false) String auxPrompt,
            @RequestParam(value = "ai_detection", required = false, defaultValue = "false") boolean aiDetection) {
        try {
            EvaluationResponse response = evaluationService.evaluateEssay(images, auxPrompt, aiDetection);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal API Error: " + e.getMessage());
        }
    }
}
