package com.bigyun.demo.service;

import com.bigyun.demo.domain.DemoItem;
import java.util.List;

public interface IDemoItemService
{
    List<DemoItem> selectDemoItemList(DemoItem demoItem);

    DemoItem selectDemoItemById(Long itemId);

    int insertDemoItem(DemoItem demoItem);

    int updateDemoItem(DemoItem demoItem);

    int deleteDemoItemByIds(Long[] itemIds);
}
