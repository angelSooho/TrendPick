package project.trendpick_pro.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.category.entity.MainCategory;
import project.trendpick_pro.domain.category.repository.MainCategoryRepository;
import project.trendpick_pro.global.exception.BaseException;
import project.trendpick_pro.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainCategoryService {

    private final MainCategoryRepository mainCategoryRepository;

    @Transactional
    public void save(String name) {
        mainCategoryRepository.save(new MainCategory(name));
    }

    @Transactional
    public void delete(Long id) {
        MainCategory mainCategory = mainCategoryRepository.findById(id).orElseThrow();
        mainCategoryRepository.delete(mainCategory);
    }

    public List<String> findAll() {
        return mainCategoryRepository.findAllByName();
    }

    public String findById(Long id) {
        return mainCategoryRepository.findById(id).orElseThrow().getName();
    }

    public MainCategory findByName(String username) {
        return mainCategoryRepository.findByName(username).orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND, "해당 카테고리가 존재하지 않습니다."));
    }
}
