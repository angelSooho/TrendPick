package project.trendpick_pro.domain.answer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.trendpick_pro.domain.answer.entity.dto.AnswerRequest;
import project.trendpick_pro.domain.answer.service.AnswerService;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/save")
    public ResponseEntity<Long> save(@RequestParam("ask") Long askId,
                                     @Valid @RequestBody AnswerRequest answerRequest,
                                     @MemberEmail String email){
        return ResponseEntity.ok().body(answerService.saveAnswer(email, askId, answerRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{answerId}")
    public ResponseEntity<Long> modify(@MemberEmail String email,
                                       @PathVariable Long answerId,
                                       @Valid AnswerRequest answerRequest){
        return ResponseEntity.ok().body(answerService.modifyAnswer(email, answerId, answerRequest));
    }

    @PostMapping("/delete/{answerId}")
    public ResponseEntity<Void> delete(@MemberEmail String email,
                                       @PathVariable Long answerId){
        answerService.deleteAnswer(email, answerId);
        return ResponseEntity.ok().build();
    }
}
