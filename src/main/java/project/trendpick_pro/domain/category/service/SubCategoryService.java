package project.trendpick_pro.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.domain.category.entity.MainCategory;
import project.trendpick_pro.domain.category.entity.SubCategory;
import project.trendpick_pro.domain.category.repository.SubCategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;

    @Transactional
    public void save(String name, MainCategory mainCategory){
        subCategoryRepository.save(new SubCategory(name, mainCategory));
    }

    @Transactional
    public void delete(Long id){
        SubCategory subCategory = subCategoryRepository.findById(id).orElseThrow();
        subCategoryRepository.delete(subCategory);
    }

    public List<String> getAll(String mainCategoryName) {
        List<SubCategory> categories;
        if (mainCategoryName.equals("전체")){
            categories = subCategoryRepository.findAllBy();
        } else {
            categories = subCategoryRepository.findAllByMainCategory(mainCategoryName);
        }
        return categories.stream().map(SubCategory::getName).toList();
    }

    public String findById(Long id){
        return subCategoryRepository.findById(id).orElseThrow().getName();
    }

    public SubCategory findByName(String username){
        return subCategoryRepository.findByName(username);
    }
}
