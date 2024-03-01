package project.trendpick_pro.domain.answer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.answer.entity.Answer;
import project.trendpick_pro.domain.answer.entity.dto.AnswerRequest;
import project.trendpick_pro.domain.answer.repository.AnswerRepository;
import project.trendpick_pro.domain.ask.entity.Ask;
import project.trendpick_pro.domain.ask.repository.AskRepository;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.service.MemberService;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final AskRepository askRepository;

    private final MemberService memberService;

    public Long saveAnswer(String email, Long askId, AnswerRequest answerForm) {
        Member member = memberService.findByEmail(email);
        Ask ask = askRepository.findById(askId).orElseThrow(
                () -> new NoSuchElementException("해당 문의는 없는 문의입니다.")
        );
        if (!member.getBrand().equals(ask.getProduct().getProductOption().getBrand().getName()))
            throw new BaseException(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");

        Answer answer = Answer.of(answerForm.getContent());
        answer.connectAsk(ask);
        answerRepository.save(answer);
        return ask.getId();
    }

    public void deleteAnswer(String email, Long answerId) {
        Member member = memberService.findByEmail(email);
        Answer answer = answerRepository.findById(answerId).orElseThrow(
                () -> new NoSuchElementException("해당 답변은 없는 답변입니다.")
        );

        if(!answer.getAsk().getProduct().getProductOption().getBrand().getName().equals(member.getBrand()))
            throw new BaseException(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");

        Ask ask = answer.getAsk();
        ask.getAnswerList().remove(answer);
        if(ask.getAnswerList().isEmpty())
            ask.updateStatusYet();
    }

    public Long modifyAnswer(String email, Long answerId, AnswerRequest answerForm) {
        Member member = memberService.findByEmail(email);
        Answer answer = answerRepository.findById(answerId).orElseThrow(
                () -> new NoSuchElementException("해당 답변은 없는 답변입니다.")
        );

        if(!Objects.equals(answer.getAsk().getAuthor().getBrand(), member.getBrand())) {
            throw new BaseException(ErrorCode.BAD_REQUEST, "접근 권한이 없습니다.");
        }
        answer.update(answerForm);

        return answer.getAsk().getId();
    }
}
