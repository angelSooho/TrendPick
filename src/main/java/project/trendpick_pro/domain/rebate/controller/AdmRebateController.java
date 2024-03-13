package project.trendpick_pro.domain.rebate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.trendpick_pro.domain.member.controller.annotation.MemberEmail;
import project.trendpick_pro.domain.rebate.entity.RebateOrderItem;
import project.trendpick_pro.domain.rebate.service.RebateService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdmRebateController {

    private final RebateService rebateService;

    @PostMapping("/makeData")
    @PreAuthorize("hasAuthority({'BRAND_ADMIN'})")
    public ResponseEntity<Void> makeData(@MemberEmail String email, String yearMonth) {
        rebateService.makeDate(email, yearMonth);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rebateOrderItemList")
    @PreAuthorize("hasAuthority({'BRAND_ADMIN'})")
    public ResponseEntity<List<RebateOrderItem>> showRebateOrderItemList(@MemberEmail String email) {
        String yearMonth = LocalDateTime.now().getMonth().toString();
        return ResponseEntity.ok().body(rebateService.getRebateOrderItemsByCreatedDateIn(email, yearMonth));
    }

//    @PostMapping("/rebateOne/{orderItemId}")
//    @PreAuthorize("hasAuthority({'BRAND_ADMIN'})")
//    public ResponseEntity<Void> rebateOne(@PathVariable long orderItemId, HttpServletRequest req) {
//        rebateService.rebate(orderItemId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/rebate")
//    @PreAuthorize("hasAuthority({'BRAND_ADMIN'})")
//    public ResponseEntity<Void> rebate(String ids, HttpServletRequest req) {
//        String[] idsArr = ids.split(",");
//        Arrays.stream(idsArr)
//                .mapToLong(Long::parseLong)
//                .forEach(rebateService::rebate);
//        return ResponseEntity.ok().build();
//    }
}
