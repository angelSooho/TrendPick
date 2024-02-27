package project.trendpick_pro.domain.member.controller.annotation;

import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.security.core.context.SecurityContextHolder;

public class MemberEmailResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean annotation = parameter.hasParameterAnnotation(MemberEmail.class);
        boolean isString = String.class.isAssignableFrom(parameter.getParameterType());

        return annotation && isString;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
