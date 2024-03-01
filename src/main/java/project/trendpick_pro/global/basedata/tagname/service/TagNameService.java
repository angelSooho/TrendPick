package project.trendpick_pro.global.basedata.tagname.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.trendpick_pro.global.basedata.tagname.entity.TagName;
import project.trendpick_pro.global.basedata.tagname.repository.TagNameRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagNameService {

    private final TagNameRepository tagNameRepository;

    @Transactional
    public void save(String name) {
        tagNameRepository.save(new TagName(name));
    }

    public TagName findByName(String name) {
        return tagNameRepository.findByName(name);
    }

    public List<String> findAll() {
        List<TagName> tags = tagNameRepository.findAllBy();
        return tags.stream().map(TagName::getName).toList();
    }

    public TagName findById(Long id) {
        return tagNameRepository.findById(id).get();
    }
}
