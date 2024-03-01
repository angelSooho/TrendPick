package project.trendpick_pro.domain.ask.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.answer.service.AnswerService;
import project.trendpick_pro.domain.ask.entity.dto.form.AskRequest;
import project.trendpick_pro.domain.ask.entity.dto.response.AskResponse;
import project.trendpick_pro.domain.ask.service.AskService;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asks")
public class AskController {

    private final AskService askService;
    private final AnswerService answerService;

    @PostMapping("/save")
    public ResponseEntity<Long> save(@MemberEmail String email,
                               @Valid @RequestBody AskRequest askRequest) {
        return ResponseEntity.ok().body(askService.saveAsk(email, askRequest));
    }

    @PostMapping("/modify/{askId}")
    public ResponseEntity<AskResponse> modify(@MemberEmail String email,
                                @PathVariable Long askId,
                                @Valid AskRequest askRequest) {
        return ResponseEntity.ok().body(askService.getAsk(askId));
    }

    @PostMapping("/delete/{askId}")
    public ResponseEntity<Void> delete(@MemberEmail String email, @PathVariable Long askId) {
        askService.delete(email, askId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{askId}")
    public ResponseEntity<AskResponse> get(
            @MemberEmail String email,
            @PathVariable Long askId) {
        return ResponseEntity.ok().body(askService.getAsk(askId));
    }
}
