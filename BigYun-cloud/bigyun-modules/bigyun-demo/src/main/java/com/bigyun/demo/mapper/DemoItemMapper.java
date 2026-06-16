package com.bigyun.demo.mapper;

import com.bigyun.demo.domain.DemoItem;
import java.util.List;

public interface DemoItemMapper
{
    List<DemoItem> selectDemoItemList(DemoItem demoItem);

    DemoItem selectDemoItemById(Long itemId);

    DemoItem selectDemoItemByCode(String itemCode);

    int insertDemoItem(DemoItem demoItem);

    int updateDemoItem(DemoItem demoItem);

    int deleteDemoItemById(Long itemId);

    int deleteDemoItemByIds(Long[] itemIds);
}
