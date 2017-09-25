package com.jmall.service;

import com.jmall.common.ServerResponse;
import com.jmall.pojo.Category;

import java.util.List;
import java.util.Set;


public interface ICategoryService {

    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse selectCategoryAndChildrenById(Integer categoryId);
}
