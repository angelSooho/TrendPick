package project.trendpick_pro.domain.common.file;

import lombok.Getter;

import java.util.List;

public record NickName(List<First> first, List<Last> last) {

    @Getter
    public static class First {
        private String name;
    }

    @Getter
    public static class Last {
        private String name;
    }
}
