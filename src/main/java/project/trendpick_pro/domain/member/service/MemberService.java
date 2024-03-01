package project.trendpick_pro.domain.member.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.brand.entity.Brand;
import project.trendpick_pro.domain.brand.service.BrandService;
import project.trendpick_pro.domain.cash.entity.CashLog;
import project.trendpick_pro.domain.cash.entity.dto.CashResponse;
import project.trendpick_pro.domain.cash.service.CashService;
import project.trendpick_pro.domain.common.file.JsonConvertor;
import project.trendpick_pro.domain.common.file.NickName;
import project.trendpick_pro.domain.member.entity.Member;
import project.trendpick_pro.domain.member.entity.MemberRole;
import project.trendpick_pro.domain.member.entity.SocialAuthToken;
import project.trendpick_pro.domain.member.entity.SocialProvider;
import project.trendpick_pro.domain.member.entity.dto.MemberInfoResponse;
import project.trendpick_pro.domain.member.repository.MemberRepository;
import project.trendpick_pro.domain.recommend.service.RecommendService;
import project.trendpick_pro.domain.store.entity.Store;
import project.trendpick_pro.domain.tags.favoritetag.entity.FavoriteTag;
import project.trendpick_pro.domain.tags.tag.entity.dto.request.TagRequest;
import project.trendpick_pro.global.crypto.jwt.JwtToken.JwtTokenService;
import project.trendpick_pro.global.crypto.jwt.JwtTokenResponse;
import project.trendpick_pro.global.crypto.jwt.JwtTokenUtil;
import project.trendpick_pro.global.crypto.oauth2.OAuth2Attribute;
import project.trendpick_pro.global.crypto.oauth2.OAuthClientProperties;
import project.trendpick_pro.global.crypto.oauth2.OAuthService;
import project.trendpick_pro.global.crypto.oauth2.OAuthTokenResponse;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService  {

    private final MemberRepository memberRepository;

    private final OAuthService oAuthService;
    private final JwtTokenService jwtTokenService;

    private final JwtTokenUtil jwtTokenUtil;
    private final OAuthClientProperties oAuthClientProperties;

    private final RecommendService recommendService;
    private final StoreService storeService;
    private final CashService cashService;
    private final BrandService brandService;
    
    private final JsonConvertor jsonConvertor;

    @Transactional
    public Member signUp(String email, String nickName, String phoneNumber, String provider, String role, OAuthTokenResponse response, String brand) {
        Member member = Member.builder()
                .email(email)
                .nickName(nickName)
                .phoneNumber(phoneNumber)
                .provider(SocialProvider.isType(provider))
                .role(MemberRole.isType(role))
                .socialAuthToken(response)
                .brand(brand)
                .build();
        settingMember(member, brand, null);
        return memberRepository.save(member);
    }

    @Transactional
    public void login(String code, String provider, String state,
                      HttpServletResponse response) {
        SocialProvider socialProvider = SocialProvider.isType(provider);
        OAuthTokenResponse socialToken = oAuthService.getSocialToken(code, socialProvider, state);
        Map<String, Object> userInfo = oAuthService.getSocialUserInfo(socialProvider, socialToken, state);
        OAuth2Attribute attribute = OAuth2Attribute.of(socialProvider, userInfo, generatedNickName());

        Optional<Member> member = memberRepository.findByEmail(attribute.getEmail());
        if (member.isPresent()) {
            member.get().updateAuthProfile(member.get().getEmail(), socialToken);
        } else {
            member = Optional.ofNullable(signUp(attribute.getEmail(), attribute.getNickName(),
                    null, provider, MemberRole.MEMBER.getValue(), socialToken, null));
        }

        JwtTokenResponse jwtTokenResponse = jwtTokenUtil.generatedToken(member.get().getEmail(), member.get().getRole().getValue());
        Cookie accessTokenCookie = jwtTokenUtil.generateTokenCookie("accessToken", jwtTokenResponse.accessToken());
        Cookie refreshTokenCookie = jwtTokenUtil.generateTokenCookie("refreshToken", jwtTokenResponse.refreshToken());
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    @Transactional
    public void reissueToken(HttpServletRequest request, HttpServletResponse response, String email) {
        String refreshToken = jwtTokenUtil.getTokenFromCookie("refreshToken", request);
        jwtTokenUtil.verifyToken(refreshToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));
        SocialAuthToken reissueSocialToken = oAuthService.verifyAndReissueSocialToken(member.getSocialAuthToken(), member.getProvider());

        Authentication authentication = jwtTokenUtil.getAuthentication(refreshToken);
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        JwtTokenResponse jwtTokenResponse = jwtTokenUtil.generatedToken(email, authorities);
        Cookie accessTokenCookie = jwtTokenUtil.generateTokenCookie("accessToken", jwtTokenResponse.accessToken());
        Cookie refreshTokenCookie = jwtTokenUtil.generateTokenCookie("refreshToken", jwtTokenResponse.refreshToken());

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    @Transactional
    public void logoutMember(HttpServletRequest request, String email) {
        String accessToken = jwtTokenUtil.getTokenFromCookie("accessToken", request);
        jwtTokenUtil.verifyToken(accessToken);
        jwtTokenService.deleteById(email);
    }

    @Transactional
    public void revokeMember(HttpServletRequest request, String email) {
        String accessToken = jwtTokenUtil.getTokenFromCookie("accessToken", request);
        jwtTokenUtil.verifyToken(accessToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));
        oAuthService.sendRevokeRequest(member.getSocialAuthToken(), member.getProvider());
        deleteMember(member);
        jwtTokenService.deleteById(email);
    }

    @Transactional
    public void deleteMember(Member member) {
        memberRepository.delete(member);
    }

    @Transactional
    public void modifyTag(Member member, TagRequest tagRequest){
        Set<FavoriteTag> tags = tagRequest.getTags().stream()
                .map(FavoriteTag::new)
                .collect(Collectors.toSet());
        member.changeTags(tags);
    }

    public MemberInfoResponse getMemberInfo(String email){
        Member member = findByEmail(email);
        return MemberInfoResponse.of(member);
    }

    @Transactional
    public MemberInfoResponse modifyAddress(String address, String email){
        Member member = findByEmail(email);
        member.connectAddress(address);
        return MemberInfoResponse.of(member);
    }

    public Member findByEmail(String email){
        return memberRepository.findByEmail(email).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "해당 이메일로 가입된 회원이 없습니다."));
    }

    public String generatedNickName() {
        String jsonContent = new File(oAuthClientProperties.getNameJson()).toString();
        NickName nickName = jsonConvertor.readValue(jsonContent, NickName.class);
        NickName.First first = nickName.first().get(ThreadLocalRandom.current().nextInt(nickName.first().size()));
        NickName.Last last = nickName.last().get(ThreadLocalRandom.current().nextInt(nickName.first().size()));
        return first.getName() + " " + last.getName();
    }

    @Transactional
    public CashResponse addCash(String brand, long price, Brand relEntity, CashLog.EvenType eventType) {
        Member member= memberRepository.findByBrand(brand).orElseThrow(() -> new BaseException(ErrorCode.MEMBER_NOT_FOUND, "해당 브랜드로 가입된 회원이 없습니다."));

        long newRestCash = getRestCash(member) + price;
        member.connectCash(newRestCash);
        return new CashResponse(new CashLog());
    }

    public long getRestCash(Member member) {
        return memberRepository.findById(member.getId()).get().getRestCash();
    }

    private void checkingMemberType(Member member) {
        if (member.getRole() == MemberRole.MEMBER) {
            recommendService.rankRecommend(member);
        }
    }

    private Member settingMember(Member member, String brand, List<String> tags) {
        if (member.getRole() == MemberRole.BRAND_ADMIN) {
            member.connectBrand(brand);
            saveBrandAndStoreIfNotExists(brand);
        }
        if (tags != null) {
            member.changeTags(createFavoriteTags(tags));
        }
        return member;
    }

    private void saveBrandAndStoreIfNotExists(String brand) {
        if (!brandService.isPresent(brand)) {
            brandService.save(brand);
            storeService.save(new Store(brand));
        }
    }

    private Set<FavoriteTag> createFavoriteTags(List<String> tagList) {
        return tagList.stream()
                .map(FavoriteTag::new)
                .collect(Collectors.toSet());
    }
}
