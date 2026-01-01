package com.research.assistant;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class ResearchController {
    @Autowired
    private ResearchService researchService;


    @PostMapping("/process")
    public ResponseEntity<String> ProcessContend(@RequestBody ResearchRequest researchRequest) {
        String result =researchService.processContent(researchRequest);
        return ResponseEntity.ok(result);

    }
}
