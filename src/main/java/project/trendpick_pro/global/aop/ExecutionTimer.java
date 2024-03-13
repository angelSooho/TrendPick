package project.trendpick_pro.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class ExecutionTimer {

    // 조인포인트를 어노테이션으로 설정
    @Pointcut("@annotation(project.trendpick_pro.global.aop.ExeTimer)")
    private void timer(){};

    // 메서드 실행 전,후로 시간을 공유해야 하기 때문
    @Around("timer()")
    public void AssumeExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        joinPoint.proceed();
        stopWatch.stop();

        long totalTimeMillis = stopWatch.getTotalTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        log.info("method: {}, running Time = {}ms", methodName, totalTimeMillis);
    }
}
